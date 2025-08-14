package me.rgunny.marketpulse.event.marketdata.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.event.marketdata.application.port.in.StockMasterSyncUseCase;
import me.rgunny.marketpulse.event.marketdata.application.port.out.StockMasterPort;
import me.rgunny.marketpulse.event.marketdata.application.port.out.SyncConfigPort;
import me.rgunny.marketpulse.event.marketdata.application.port.out.SyncLockPort;
import me.rgunny.marketpulse.event.marketdata.application.port.out.shared.StockPort;
import me.rgunny.marketpulse.event.marketdata.domain.model.Stock;
import me.rgunny.marketpulse.event.marketdata.domain.model.SyncMode;
import me.rgunny.marketpulse.event.marketdata.domain.model.SyncResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import me.rgunny.marketpulse.event.marketdata.application.util.BoundedErrorCollector;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 종목 마스터 데이터 동기화 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockMasterSyncService implements StockMasterSyncUseCase {
    
    private final StockMasterPort stockMasterPort;
    private final StockPort stockPort;
    private final SyncLockPort syncLockPort;
    private final SyncConfigPort syncConfigPort;
    
    private static final String SYNC_LOCK_KEY = "stock_master_sync";
    private static final Duration SYNC_LOCK_TTL = Duration.ofMinutes(30);
    private static final int MAX_ERRORS_TO_COLLECT = 100;  // 메모리 보호
    
    @Override
    public Mono<SyncResult> syncStockMaster(SyncMode syncMode) {
        log.info("종목 마스터 동기화 요청: mode={}", syncMode);
        Instant startTime = Instant.now();
        
        // 분산 락 획득 시도
        return syncLockPort.tryLock(SYNC_LOCK_KEY, SYNC_LOCK_TTL)
                .flatMap(locked -> {
                    if (!locked) {
                        log.warn("종목 마스터 동기화가 이미 진행 중입니다. (분산 락 획득 실패)");
                        return Mono.just(SyncResult.failure(
                                syncMode,
                                startTime,
                                List.of("다른 인스턴스에서 동기화가 진행 중입니다.")
                        ));
                    }
                    
                    log.info("종목 마스터 동기화 시작: mode={}", syncMode);
                    return executeSync(syncMode, startTime)
                            .doFinally(signal -> releaseLockSafely(SYNC_LOCK_KEY, signal));
                });
    }
    
    @Override
    public Mono<SyncResult> syncStockMasterByMarket(String market, SyncMode syncMode) {
        log.info("시장별 종목 마스터 동기화 요청: market={}, mode={}", market, syncMode);
        Instant startTime = Instant.now();
        String lockKey = SYNC_LOCK_KEY + ":" + market;
        
        return syncLockPort.tryLock(lockKey, SYNC_LOCK_TTL)
                .flatMap(locked -> {
                    if (!locked) {
                        log.warn("{} 시장 동기화가 이미 진행 중입니다.", market);
                        return Mono.just(SyncResult.failure(
                                syncMode,
                                startTime,
                                List.of(market + " 시장 동기화가 진행 중입니다.")
                        ));
                    }
                    
                    log.info("종목 마스터 동기화 시작: market={}, mode={}", market, syncMode);
                    return executeSyncByMarket(market, syncMode, startTime)
                            .doFinally(signal -> releaseLockSafely(lockKey, signal));
                });
    }
    
    @Override
    public Mono<Boolean> isSyncing() {
        return syncLockPort.isLocked(SYNC_LOCK_KEY);
    }
    
    private Mono<SyncResult> executeSync(SyncMode syncMode, Instant startTime) {
        Flux<Stock> stockFlux = (syncMode == SyncMode.FULL) 
                ? handleFullSync() 
                : handleIncrementalSync();
        
        AtomicInteger totalCount = new AtomicInteger(0);
        AtomicInteger newCount = new AtomicInteger(0);
        AtomicInteger updatedCount = new AtomicInteger(0);
        AtomicInteger failedCount = new AtomicInteger(0);
        BoundedErrorCollector errorCollector = new BoundedErrorCollector(MAX_ERRORS_TO_COLLECT);
        
        return stockFlux
                .buffer(syncConfigPort.getBatchSize())
                .delayElements(Duration.ofMillis(syncConfigPort.getBatchDelayMs()))
                .flatMap(batch -> processBatch(batch, newCount, updatedCount, failedCount, errorCollector)
                        .doOnNext(count -> totalCount.addAndGet(count)))
                .then(Mono.defer(() -> createSyncResult(
                        syncMode, startTime, totalCount.get(), 
                        newCount.get(), updatedCount.get(), 
                        failedCount.get(), errorCollector
                )))
                .doOnError(error -> log.error("종목 마스터 동기화 실패", error))
                .onErrorReturn(SyncResult.failure(
                        syncMode,
                        startTime,
                        List.of("동기화 중 오류 발생")
                ));
    }
    
    private Mono<SyncResult> executeSyncByMarket(String market, SyncMode syncMode, Instant startTime) {
        Flux<Stock> stockFlux = stockMasterPort.fetchStocksByMarket(market);
        
        AtomicInteger totalCount = new AtomicInteger(0);
        AtomicInteger newCount = new AtomicInteger(0);
        AtomicInteger updatedCount = new AtomicInteger(0);
        AtomicInteger failedCount = new AtomicInteger(0);
        BoundedErrorCollector errorCollector = new BoundedErrorCollector(MAX_ERRORS_TO_COLLECT);
        
        return stockFlux
                .buffer(syncConfigPort.getBatchSize())
                .delayElements(Duration.ofMillis(syncConfigPort.getBatchDelayMs()))
                .flatMap(batch -> processBatch(batch, newCount, updatedCount, failedCount, errorCollector)
                        .doOnNext(count -> totalCount.addAndGet(count)))
                .then(Mono.defer(() -> createSyncResult(
                        syncMode, startTime, totalCount.get(),
                        newCount.get(), updatedCount.get(),
                        failedCount.get(), errorCollector
                )))
                .doOnError(error -> log.error("종목 마스터 동기화 실패: market={}", market, error))
                .onErrorReturn(SyncResult.failure(
                        syncMode,
                        startTime,
                        List.of("동기화 중 오류 발생: " + market)
                ));
    }
    
    private Flux<Stock> handleFullSync() {
        log.info("전체 동기화 모드: 기존 데이터 삭제 후 재구축");
        return stockPort.deleteAll()
                .thenMany(stockMasterPort.fetchAllStocks());
    }
    
    private Flux<Stock> handleIncrementalSync() {
        log.info("증분 동기화 모드: 변경된 종목만 업데이트");
        return stockMasterPort.fetchAllStocks();
    }
    
    private Mono<Integer> processBatch(
            List<Stock> batch,
            AtomicInteger newCount,
            AtomicInteger updatedCount,
            AtomicInteger failedCount,
            BoundedErrorCollector errorCollector
    ) {
        return Flux.fromStream(batch.stream())
                .flatMap(stock -> saveOrUpdateStock(stock, newCount, updatedCount)
                        .onErrorResume(error -> {
                            failedCount.incrementAndGet();
                            errorCollector.addError(stock.getSymbol(), error.getMessage());
                            
                            log.error("Stock save failed - symbol: {}, error: {}",
                                    stock.getSymbol(), error.getMessage());
                            return Mono.empty();
                        }))
                .count()
                .map(Long::intValue)
                .defaultIfEmpty(0)
                .subscribeOn(Schedulers.parallel()); // CPU 바운드 작업은 parallel
    }
    
    private Mono<Stock> saveOrUpdateStock(Stock stock, AtomicInteger newCount, AtomicInteger updatedCount) {
        return stockPort.findBySymbol(stock.getSymbol())
                .flatMap(existing -> {
                    // 업데이트 필요 여부 판단 (간단한 비교 로직)
                    if (!existing.equals(stock)) {
                        updatedCount.incrementAndGet();
                        return stockPort.save(stock);
                    }
                    return Mono.just(existing);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    newCount.incrementAndGet();
                    return stockPort.save(stock);
                }));
    }
    
    private Mono<SyncResult> createSyncResult(
            SyncMode syncMode,
            Instant startTime,
            int totalCount,
            int newCount,
            int updatedCount,
            int failedCount,
            BoundedErrorCollector errorCollector
    ) {
        List<String> errors = errorCollector.getErrors();
        
        // 에러 통계 로깅 (MDC 활용)
        if (errorCollector.getStats().droppedCount() > 0) {
            log.warn("Error collection overflow - dropped: {}, rate: {:.2f}%",
                    errorCollector.getStats().droppedCount(),
                    errorCollector.getStats().getDropRate());
        }
        
        if (failedCount == 0) {
            return Mono.just(SyncResult.success(
                    syncMode, startTime, totalCount, newCount, updatedCount
            ));
        } else if (failedCount < totalCount) {
            return Mono.just(SyncResult.partial(
                    syncMode, startTime, totalCount,
                    totalCount - failedCount, failedCount,
                    newCount, updatedCount,
                    errors
            ));
        } else {
            return Mono.just(SyncResult.failure(
                    syncMode, startTime,
                    errors
            ));
        }
    }
    
    /**
     * 분산 락 안전 해제
     */
    private void releaseLockSafely(String lockKey, reactor.core.publisher.SignalType signal) {
        syncLockPort.unlock(lockKey)
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(unused -> log.debug("Sync lock released: key={}, signal={}", lockKey, signal))
                .doOnError(error -> {
                    log.error("CRITICAL: Failed to release sync lock: key={}", lockKey, error);
                    // TODO: Notification 연동하여 알람발송
                    notifyLockReleaseFailure(lockKey, error);
                })
                .subscribe();
    }
    
    /**
     * 락 해제 실패 알림
     */
    private void notifyLockReleaseFailure(String lockKey, Throwable error) {
        // TODO: Notification 연동하여 알람발송
        log.error("[ALERT] Lock release failure requires immediate attention: key={}", lockKey);
    }
}
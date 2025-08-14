package me.rgunny.marketpulse.event.marketdata.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.event.marketdata.application.port.in.StockMasterSyncUseCase;
import me.rgunny.marketpulse.event.marketdata.application.port.out.SyncConfigPort;
import me.rgunny.marketpulse.event.marketdata.domain.model.SyncMode;
import me.rgunny.marketpulse.event.marketdata.domain.model.SyncResult;
import me.rgunny.marketpulse.event.marketdata.domain.model.SyncStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.slf4j.MDC.clear;
import static org.slf4j.MDC.put;

/**
 * 종목 마스터 동기화 스케줄러
 * 
 * 실행 조건:
 * - 전체 동기화: 매일 새벽 2시 (장 마감 후)
 * - 증분 동기화: 매시간 (장중에만)
 * - 설정으로 활성화/비활성화 가능
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "marketpulse.stock-master.sync",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class StockMasterSyncScheduler {
    
    private final StockMasterSyncUseCase stockMasterSyncUseCase;
    private final SyncConfigPort syncConfigPort;
    private final Clock clock;
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    /**
     * 전체 동기화 스케줄
     * 매일 새벽 2시에 실행
     */
    @Scheduled(cron = "${marketpulse.stock-master.sync.full-sync-cron:0 0 2 * * *}")
    public void executeFullSync() {
        if (!syncConfigPort.isEnabled()) {
            return;
        }
        
        LocalDateTime now = LocalDateTime.now(clock);
        log.info("=== 종목 마스터 전체 동기화 시작: {} ===", now.format(TIME_FORMATTER));
        
        stockMasterSyncUseCase.syncStockMaster(SyncMode.FULL)
                .doOnNext(result -> logSyncResult("전체", result))
                .doOnError(error -> log.error("종목 마스터 전체 동기화 실패", error))
                .onErrorResume(error -> Mono.empty())
                .subscribe();
    }
    
    /**
     * 증분 동기화 스케줄
     * 매시간 0분에 실행 (장중에만)
     */
    @Scheduled(cron = "${marketpulse.stock-master.sync.incremental-sync-cron:0 0 * * * *}")
    public void executeIncrementalSync() {
        if (!syncConfigPort.isEnabled()) {
            return;
        }
        
        // 장시간이 아니면 스킵 (평일 9:00 ~ 15:30)
        LocalDateTime now = LocalDateTime.now(clock);
        boolean isMarketOpen = isMarketOpen(now);
        if (isMarketOpen) {
            log.info("=== 종목 마스터 증분 동기화 시작: {} ===", now.format(TIME_FORMATTER));
            
            stockMasterSyncUseCase.syncStockMaster(SyncMode.INCREMENTAL)
                    .doOnNext(result -> logSyncResult("증분", result))
                    .doOnError(error -> log.error("종목 마스터 증분 동기화 실패", error))
                    .onErrorResume(error -> Mono.empty())
                    .subscribe();
        }
    }
    
    /**
     * KOSPI 종목만 동기화 (테스트용)
     * 매일 새벽 3시에 실행
     */
    @Scheduled(cron = "${marketpulse.stock-master.sync.kospi-sync-cron:0 0 3 * * *}")
    public void executeKospiSync() {
        if (!syncConfigPort.isEnabled() || !syncConfigPort.isKospiSyncEnabled()) {
            return;
        }
        
        LocalDateTime now = LocalDateTime.now(clock);
        log.info("=== KOSPI 종목 동기화 시작: {} ===", now.format(TIME_FORMATTER));
        
        stockMasterSyncUseCase.syncStockMasterByMarket("KOSPI", SyncMode.FULL)
                .doOnNext(result -> logSyncResult("KOSPI", result))
                .doOnError(error -> log.error("KOSPI 종목 동기화 실패", error))
                .onErrorResume(error -> Mono.empty())
                .subscribe();
    }
    
    /**
     * KOSDAQ 종목만 동기화 (테스트용)
     * 매일 새벽 3시 30분에 실행
     */
    @Scheduled(cron = "${marketpulse.stock-master.sync.kosdaq-sync-cron:0 30 3 * * *}")
    public void executeKosdaqSync() {
        if (!syncConfigPort.isEnabled() || !syncConfigPort.isKosdaqSyncEnabled()) {
            return;
        }
        
        LocalDateTime now = LocalDateTime.now(clock);
        log.info("=== KOSDAQ 종목 동기화 시작: {} ===", now.format(TIME_FORMATTER));
        
        stockMasterSyncUseCase.syncStockMasterByMarket("KOSDAQ", SyncMode.FULL)
                .doOnNext(result -> logSyncResult("KOSDAQ", result))
                .doOnError(error -> log.error("KOSDAQ 종목 동기화 실패", error))
                .onErrorResume(error -> Mono.empty())
                .subscribe();
    }
    
    /**
     * 동기화 결과 로깅
     */
    private void logSyncResult(String type, SyncResult result) {
        // MDC(Mapped Diagnostic Context)에 컨텍스트 정보 추가
        put("sync.type", type);
        put("sync.mode", result.syncMode().name());
        put("sync.total", String.valueOf(result.totalProcessed()));
        put("sync.success", String.valueOf(result.successCount()));
        put("sync.failed", String.valueOf(result.failedCount()));
        put("sync.success_rate", String.format("%.2f", result.getSuccessRate()));
        put("sync.duration_ms", String.valueOf(result.getDuration().toMillis()));
        
        try {
            // 성공 케이스: 단일 로그 엔트리
            if (result.status() == SyncStatus.SUCCESS) {
                log.info("Stock sync completed successfully - type: {}, processed: {}, duration: {}ms",
                        type, result.totalProcessed(), result.getDuration().toMillis());
            }
            // 부분 성공: 경고 레벨
            else if (result.status() == SyncStatus.PARTIAL) {
                log.warn("Stock sync partially completed - type: {}, success: {}/{}, rate: {:.2f}%, errors: {}",
                        type, result.successCount(), result.totalProcessed(), 
                        result.getSuccessRate(), 
                        result.errors().stream().limit(3).toList());
            }
            // 실패: 에러 레벨
            else {
                log.error("Stock sync failed - type: {}, errors: {}", 
                        type, result.errors().stream().limit(5).toList());
            }
            
            // 성능 저하 감지 (별도 알림)
            if (result.getSuccessRate() < 90.0) {
                log.error("ALERT: Stock sync degraded performance - type: {}, rate: {:.2f}%",
                        type, result.getSuccessRate());
                // TODO: 알림 시스템 연동
            }
        } finally {
            // MDC 정리 (메모리 누수 방지)
            clear();
        }
    }
    
    /**
     * 시장 개장 시간 체크
     * 평일 09:00 ~ 15:30
     */
    private boolean isMarketOpen(LocalDateTime dateTime) {
        // 주말 체크
        if (dateTime.getDayOfWeek().getValue() >= 6) {
            return false;
        }
        
        // 시간 체크 (09:00 ~ 15:30)
        int hour = dateTime.getHour();
        int minute = dateTime.getMinute();
        int timeValue = hour * 100 + minute;
        
        return timeValue >= 900 && timeValue <= 1530;
    }
}
package me.rgunny.marketpulse.event.marketdata.infrastructure.adapter.in.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.event.marketdata.application.port.in.CollectStockPriceUseCase;
import me.rgunny.marketpulse.event.marketdata.application.port.in.MarketHoursUseCase;
import me.rgunny.marketpulse.event.marketdata.infrastructure.config.shared.StockCollectionProperties;
import me.rgunny.marketpulse.event.watchlist.application.port.out.WatchTargetPort;
import me.rgunny.marketpulse.event.watchlist.domain.model.WatchTarget;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 주식 시세 수집 스케줄러
 * 
 * 주기적으로 감시 대상 종목들의 현재가를 수집하여 저장합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockPriceCollectionScheduler {

    private final WatchTargetPort watchTargetPort;
    private final CollectStockPriceUseCase collectStockPriceUseCase;
    private final MarketHoursUseCase marketHoursUseCase;
    private final StockCollectionProperties properties;
    
    // 마지막 수집 시간 추적을 위한 맵
    private final ConcurrentHashMap<String, LocalDateTime> lastCollectionTimes = new ConcurrentHashMap<>();

    /**
     * 애플리케이션 시작 시 스케줄러 정보 로깅
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Stock price collection scheduler started with configuration:");
        log.info("- Active stocks collection interval: {}", properties.schedule().activeStocks());
        log.info("- High priority collection interval: {}", properties.schedule().highPriority());
        log.info("- Core stocks cron: {}", properties.schedule().coreStocks());
        log.info("- Concurrency limits - default: {}, high priority: {}", 
                properties.concurrency().defaultLimit(), 
                properties.concurrency().highPriority());
    }

    /**
     * 전체 활성 종목 주기적 수집
     * 
     * 30초마다 실행되며, 모든 활성 종목의 현재가를 수집
     * 장시간이 아닌 경우 실행하지 않음.
     */
    @Scheduled(fixedDelayString = "#{T(java.time.Duration).parse('${app.stock-collection.schedule.active-stocks:PT30S}').toMillis()}", 
              initialDelayString = "#{T(java.time.Duration).parse('${app.stock-collection.schedule.initial-delay:PT10S}').toMillis()}")
    public void collectActiveStocks() {
        if (!marketHoursUseCase.isMarketOpen()) {
            log.debug("Market is closed. Skipping active stocks collection.");
            return;
        }
        collectActiveStocksReactive().subscribe();
    }
    
    /**
     * 전체 활성 종목 수집 (테스트 가능한 reactive 버전)
     */
    public Mono<Void> collectActiveStocksReactive() {
        log.debug("Starting active stocks collection at {}", LocalDateTime.now());
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        return watchTargetPort.findActiveTargets()
            .filter(target -> target.shouldCollectNow(lastCollectionTimes.get(target.getSymbol())))
            .flatMap(target -> collectAndSaveStockPrice(target)
                    .doOnSuccess(price -> {
                        successCount.incrementAndGet();
                        lastCollectionTimes.put(target.getSymbol(), LocalDateTime.now());
                        log.debug("Collected price for {}: {}", target.getSymbol(), price.getCurrentPrice());
                    })
                    .doOnError(error -> {
                        errorCount.incrementAndGet();
                        log.warn("Failed to collect price for {}: {}", target.getSymbol(), error.getMessage());
                    })
                    .onErrorResume(error -> Mono.empty()),
                properties.concurrency().defaultLimit()
            )
            .then()
            .doOnSuccess(unused -> 
                log.info("Active stocks collection completed. Success: {}, Errors: {}", 
                        successCount.get(), errorCount.get())
            );
    }

    /**
     * 높은 우선순위 종목 주기적 수집
     * 
     * 15초마다 실행되며, 우선순위가 높은 종목들의 현재가를 수집
     * 장시간이 아닌 경우 실행하지 않음.
     */
    @Scheduled(fixedDelayString = "#{T(java.time.Duration).parse('${app.stock-collection.schedule.high-priority:PT15S}').toMillis()}", 
              initialDelayString = "#{T(java.time.Duration).parse('${app.stock-collection.schedule.high-priority-delay:PT5S}').toMillis()}")
    public void collectHighPriorityStocks() {
        if (!marketHoursUseCase.isMarketOpen()) {
            log.debug("Market is closed. Skipping high priority stocks collection.");
            return;
        }
        collectHighPriorityStocksReactive().subscribe();
    }
    
    /**
     * 높은 우선순위 종목 수집 (테스트 가능한 reactive 버전)
     */
    public Mono<Void> collectHighPriorityStocksReactive() {
        log.debug("Starting high priority stocks collection at {}", LocalDateTime.now());
        
        AtomicInteger count = new AtomicInteger(0);
        
        return watchTargetPort.findHighPriorityTargets()
            .flatMap(target -> collectAndSaveStockPrice(target)
                    .doOnSuccess(price -> {
                        count.incrementAndGet();
                        log.debug("Collected high priority {}: {}", target.getSymbol(), price.getCurrentPrice());
                    })
                    .onErrorResume(error -> {
                        log.warn("Failed to collect high priority {}: {}", target.getSymbol(), error.getMessage());
                        return Mono.empty();
                    }),
                properties.concurrency().highPriority()
            )
            .then()
            .doOnSuccess(unused -> 
                log.info("High priority collection completed. Collected: {} stocks", count.get())
            );
    }

    /**
     * 코어 카테고리 종목 수집 (매 분 실행)
     * 
     * cron 표현식에 따라 실행되며, CORE 카테고리 종목들의 현재가를 수집
     * 장시간이 아닌 경우 실행하지 않음.
     */
    @Scheduled(cron = "${app.stock-collection.schedule.core-stocks:0 * * * * *}")
    public void collectCoreStocks() {
        if (!marketHoursUseCase.isMarketOpen()) {
            // 장마감 시간이면 다음 개장 시간 로깅
            if (log.isDebugEnabled()) {
                MarketHoursUseCase.MarketStatus status = marketHoursUseCase.getMarketStatus();
                log.debug("Market is closed: {}. Next open in {} hours", 
                    status.description(), 
                    status.timeUntilNextChange().toHours());
            }
            return;
        }
        
        log.debug("Starting core stocks collection at {}", LocalDateTime.now());
        
        AtomicInteger count = new AtomicInteger(0);
        
        watchTargetPort.findByCategory(properties.priority().coreCategory())
            .flatMap(target -> collectAndSaveStockPrice(target)
                    .doOnSuccess(price -> {
                        count.incrementAndGet();
                        log.debug("Collected core stock {}: {}", target.getSymbol(), price.getCurrentPrice());
                    })
                    .onErrorResume(error -> {
                        log.error("Failed to collect core stock {}: {}", target.getSymbol(), error.getMessage());
                        return Mono.empty();
                    }),
                properties.concurrency().categoryLimit()
            )
            .doOnComplete(() -> 
                log.info("Core stocks collection completed. Collected: {} stocks", count.get())
            )
            .subscribe();
    }

    /**
     * 카테고리별 종목 수집 (테스트용)
     */
    public Mono<Void> collectStocksByCategory(String categoryName) {
        log.debug("Starting stocks collection for category: {}", categoryName);
        
        try {
            me.rgunny.marketpulse.event.watchlist.domain.model.WatchCategory category = 
                me.rgunny.marketpulse.event.watchlist.domain.model.WatchCategory.valueOf(categoryName);
            
            return watchTargetPort.findActiveTargetsByCategory(category)
                .flatMap(target -> collectAndSaveStockPrice(target)
                        .onErrorResume(error -> {
                            log.warn("Failed to collect {} category stock {}: {}", categoryName, target.getSymbol(), error.getMessage());
                            return Mono.empty();
                        }),
                    properties.concurrency().categoryLimit()
                )
                .then()
                .doOnSuccess(unused -> 
                    log.info("Category {} stocks collection completed", categoryName)
                );
        } catch (IllegalArgumentException e) {
            log.warn("Invalid category name: {}", categoryName);
            return Mono.empty();
        }
    }
    
    /**
     * 현재 수집 상태 조회 (테스트용)
     */
    public Mono<me.rgunny.marketpulse.event.marketdata.domain.model.CollectionStatus> getCurrentStatus() {
        return Mono.just(me.rgunny.marketpulse.event.marketdata.domain.model.CollectionStatus.of(1, 1))
                .doOnSuccess(status -> log.debug("Status requested: totalTracked={}, recentCollections={}", 
                    status.totalTrackedSymbols(), status.recentCollections()));
    }

    /**
     * 종목 시세 수집 및 저장
     */
    private Mono<me.rgunny.marketpulse.event.marketdata.domain.model.StockPrice> collectAndSaveStockPrice(WatchTarget target) {
        return collectStockPriceUseCase.getCurrentPriceAndSave(target.getSymbol())
            .timeout(Duration.ofSeconds(30))
            .publishOn(Schedulers.boundedElastic());
    }
}
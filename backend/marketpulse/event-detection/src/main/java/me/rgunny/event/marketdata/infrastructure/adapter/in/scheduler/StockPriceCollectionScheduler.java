package me.rgunny.event.marketdata.infrastructure.adapter.in.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.event.marketdata.application.port.in.CollectStockPriceUseCase;
import me.rgunny.event.marketdata.infrastructure.config.shared.StockCollectionProperties;
import me.rgunny.event.watchlist.application.port.out.WatchTargetPort;
import me.rgunny.event.watchlist.domain.model.WatchTarget;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
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
    private final StockCollectionProperties properties;

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
     * 30초마다 실행되며, 모든 활성 종목의 현재가를 수집합니다.
     */
    @Scheduled(fixedDelayString = "#{T(java.time.Duration).parse('${app.stock-collection.schedule.active-stocks:PT30S}').toMillis()}", 
              initialDelayString = "#{T(java.time.Duration).parse('${app.stock-collection.schedule.initial-delay:PT10S}').toMillis()}")
    public void collectActiveStocks() {
        log.debug("Starting active stocks collection at {}", LocalDateTime.now());
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        watchTargetPort.findActiveTargets()
            .flatMap(target -> collectAndSaveStockPrice(target)
                    .doOnSuccess(price -> {
                        successCount.incrementAndGet();
                        log.debug("Collected price for {}: {}", target.getSymbol(), price.getCurrentPrice());
                    })
                    .doOnError(error -> {
                        errorCount.incrementAndGet();
                        log.warn("Failed to collect price for {}: {}", target.getSymbol(), error.getMessage());
                    })
                    .onErrorResume(error -> Mono.empty()),
                properties.concurrency().defaultLimit()
            )
            .doOnComplete(() -> 
                log.info("Active stocks collection completed. Success: {}, Errors: {}", 
                        successCount.get(), errorCount.get())
            )
            .subscribe();
    }

    /**
     * 높은 우선순위 종목 주기적 수집
     * 
     * 15초마다 실행되며, 우선순위가 높은 종목들의 현재가를 수집합니다.
     */
    @Scheduled(fixedDelayString = "#{T(java.time.Duration).parse('${app.stock-collection.schedule.high-priority:PT15S}').toMillis()}", 
              initialDelayString = "#{T(java.time.Duration).parse('${app.stock-collection.schedule.high-priority-delay:PT5S}').toMillis()}")
    public void collectHighPriorityStocks() {
        log.debug("Starting high priority stocks collection at {}", LocalDateTime.now());
        
        AtomicInteger count = new AtomicInteger(0);
        
        watchTargetPort.findByPriorityRange(
                properties.priority().highMin(), 
                properties.priority().highMax()
            )
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
            .doOnComplete(() -> 
                log.info("High priority collection completed. Collected: {} stocks", count.get())
            )
            .subscribe();
    }

    /**
     * 코어 카테고리 종목 수집 (매 분 실행)
     * 
     * cron 표현식에 따라 실행되며, CORE 카테고리 종목들의 현재가를 수집합니다.
     */
    @Scheduled(cron = "${app.stock-collection.schedule.core-stocks:0 * * * * *}")
    public void collectCoreStocks() {
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
     * 종목 시세 수집 및 저장
     */
    private Mono<me.rgunny.event.marketdata.domain.model.StockPrice> collectAndSaveStockPrice(WatchTarget target) {
        return collectStockPriceUseCase.getCurrentPriceAndSave(target.getSymbol())
            .timeout(Duration.ofSeconds(30))
            .publishOn(Schedulers.boundedElastic());
    }
}
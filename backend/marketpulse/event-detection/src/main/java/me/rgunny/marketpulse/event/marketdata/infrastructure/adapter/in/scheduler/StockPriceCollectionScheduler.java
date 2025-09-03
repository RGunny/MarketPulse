package me.rgunny.marketpulse.event.marketdata.infrastructure.adapter.in.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.event.marketdata.application.port.in.CollectStockPriceUseCase;
import me.rgunny.marketpulse.event.marketdata.application.port.in.MarketHoursUseCase;
import me.rgunny.marketpulse.event.marketdata.infrastructure.config.shared.StockCollectionProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

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
        log.info("===============================================================");
        log.info("Stock price collection scheduler started with configuration:");
        log.info("- Active stocks collection interval: {}", properties.schedule().activeStocks());
        log.info("- High priority collection interval: {}", properties.schedule().highPriority());
        log.info("- Core stocks cron: {}", properties.schedule().coreStocks());
        log.info("- Concurrency limits - default: {}, high priority: {}",
                properties.concurrency().defaultLimit(),
                properties.concurrency().highPriority());
        log.info("===============================================================");
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
     * 전체 활성 종목 수집
     */
    public Mono<Void> collectActiveStocksReactive() {
        log.debug("Starting active stocks collection at {}", LocalDateTime.now());

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        return null;
//        return watchTargetPort.findActiveTargets()
//            .filter(target -> target.shouldCollectNow(lastCollectionTimes.get(target.getSymbol())))
//            .flatMap(target -> collectAndSaveStockPrice(target)
//                    .doOnSuccess(price -> {
//                        successCount.incrementAndGet();
//                        lastCollectionTimes.put(target.getSymbol(), LocalDateTime.now());
//                        log.debug("Collected price for {}: {}", target.getSymbol(), price.getCurrentPrice());
//                    })
//                    .doOnError(error -> {
//                        errorCount.incrementAndGet();
//                        log.warn("Failed to collect price for {}: {}", target.getSymbol(), error.getMessage());
//                    })
//                    .onErrorResume(error -> Mono.empty()),
//                properties.concurrency().defaultLimit()
//            )
//            .then()
//            .doOnSuccess(unused ->
//                log.info("Active stocks collection completed. Success: {}, Errors: {}",
//                        successCount.get(), errorCount.get())
//            );
    }

    /**
     * 종목 시세 수집 및 저장
     */
//    private Mono<me.rgunny.marketpulse.event.marketdata.domain.model.StockPrice> collectAndSaveStockPrice(WatchTarget target) {
//        return collectStockPriceUseCase.getCurrentPriceAndSave(target.getSymbol())
//            .timeout(Duration.ofSeconds(30))
//            .publishOn(Schedulers.boundedElastic());
//    }
}
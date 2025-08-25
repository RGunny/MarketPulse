package me.rgunny.marketpulse.event.marketdata.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.event.marketdata.application.port.in.MarketHoursUseCase;
import me.rgunny.marketpulse.event.marketdata.application.port.in.MarketRankingUseCase;
import me.rgunny.marketpulse.event.marketdata.domain.model.MarketType;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * 시장 순위 수집 스케줄러
 * 
 * 주기적으로 시장 전체 급등/급락/거래량 상위 종목을 수집하고
 * 이상 종목을 자동으로 WatchTarget에 등록
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MarketRankingScheduler {
    
    private final MarketRankingUseCase marketRankingUseCase;
    private final MarketHoursUseCase marketHoursUseCase;
    
    /**
     * 애플리케이션 시작 시 스케줄러 정보 로깅
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Market ranking scheduler started");
        log.info("- KOSPI rankings: every 10 minutes during market hours");
        log.info("- KOSDAQ rankings: every 10 minutes during market hours");
        log.info("- Anomaly detection: every 5 minutes during market hours");
        log.info("- Data cleanup: daily at 3 AM");
    }
    
    /**
     * KOSPI 시장 순위 수집 (장중 10분마다)
     */
    @Scheduled(cron = "0 */10 9-15 * * MON-FRI")
    public void collectKospiRankings() {
        if (!marketHoursUseCase.isMarketOpen()) {
            log.debug("Market is closed. Skipping KOSPI rankings collection");
            return;
        }
        
        log.info("Starting KOSPI rankings collection at {}", LocalDateTime.now());
        marketRankingUseCase.collectMarketRankings(MarketType.KOSPI)
                .doOnSuccess(result -> log.info("KOSPI rankings collected: {}", result))
                .doOnError(error -> log.error("Failed to collect KOSPI rankings", error))
                .onErrorResume(error -> Mono.empty())
                .subscribe();
    }
    
    /**
     * KOSDAQ 시장 순위 수집 (장중 10분마다, KOSPI와 5분 간격)
     */
    @Scheduled(cron = "0 5-55/10 9-15 * * MON-FRI")
    public void collectKosdaqRankings() {
        if (!marketHoursUseCase.isMarketOpen()) {
            log.debug("Market is closed. Skipping KOSDAQ rankings collection");
            return;
        }
        
        log.info("Starting KOSDAQ rankings collection at {}", LocalDateTime.now());
        marketRankingUseCase.collectMarketRankings(MarketType.KOSDAQ)
                .doOnSuccess(result -> log.info("KOSDAQ rankings collected: {}", result))
                .doOnError(error -> log.error("Failed to collect KOSDAQ rankings", error))
                .onErrorResume(error -> Mono.empty())
                .subscribe();
    }
    
    /**
     * 이상 종목 자동 감지 및 WatchTarget 등록 (장중 5분마다)
     */
    @Scheduled(cron = "0 */5 9-15 * * MON-FRI")
    public void detectAndRegisterAnomalies() {
        if (!marketHoursUseCase.isMarketOpen()) {
            log.debug("Market is closed. Skipping anomaly detection");
            return;
        }
        
        log.info("Starting anomaly detection at {}", LocalDateTime.now());
        marketRankingUseCase.detectAndRegisterAnomalies()
                .doOnSuccess(result -> {
                    if (result.registeredCount() > 0) {
                        log.info("Anomaly detection completed: {} new targets registered", 
                                result.registeredCount());
                    } else {
                        log.debug("Anomaly detection completed: no new targets");
                    }
                })
                .doOnError(error -> log.error("Failed to detect anomalies", error))
                .onErrorResume(error -> Mono.empty())
                .subscribe();
    }
    
    /**
     * 장 시작 시 초기 수집 (오전 9시 1분)
     */
    @Scheduled(cron = "0 1 9 * * MON-FRI")
    public void morningInitialCollection() {
        log.info("Starting morning initial rankings collection");
        
        Mono.when(
                marketRankingUseCase.collectMarketRankings(MarketType.KOSPI),
                marketRankingUseCase.collectMarketRankings(MarketType.KOSDAQ)
        )
        .doOnSuccess(unused -> log.info("Morning initial collection completed"))
        .doOnError(error -> log.error("Failed morning collection", error))
        .subscribe();
    }
    
    /**
     * 오래된 순위 데이터 정리 (매일 새벽 3시)
     * 7일 이상 된 데이터 삭제
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupOldRankings() {
        log.info("Starting old rankings cleanup");
        
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        
        log.info("Cleanup task scheduled - would delete rankings older than {}", sevenDaysAgo);
        
        // TODO: 실제 삭제 로직 구현
        // marketRankingPort.deleteOlderThan(sevenDaysAgo)
        //     .doOnSuccess(count -> log.info("Deleted {} old ranking records", count))
        //     .subscribe();
    }
}
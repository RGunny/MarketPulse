package me.rgunny.event.infrastructure.adapter.input.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.event.application.port.input.StockCollectionUseCase;
import me.rgunny.event.infrastructure.config.StockCollectionProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 주식 시세 수집 스케줄러
 * 
 * - 리액티브 프로그래밍으로 비동기 처리
 * - 헥사고날 아키텍처로 비즈니스 로직 분리  
 * - @ConditionalOnProperty로 환경별 제어
 * - 에러 복구 및 모니터링 로직 포함
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.scheduler.stock-collection.enabled", havingValue = "true", matchIfMissing = true)
public class StockCollectionScheduler {
    
    private final StockCollectionUseCase stockCollectionUseCase;
    private final StockCollectionProperties properties;
    
    /**
     * 활성화된 모든 종목 정기 수집 (30초마다)
     * - 각 종목의 개별 수집 주기(collectInterval)를 고려
     * - 리액티브 스트림으로 비동기 배치 처리
     */
    @Scheduled(fixedDelayString = "#{T(java.time.Duration).ofSeconds(30).toMillis()}", 
               initialDelayString = "#{T(java.time.Duration).ofSeconds(10).toMillis()}")
    public void collectActiveStockPrices() {
        LocalDateTime startTime = LocalDateTime.now();
        log.info("주식 시세 정기 수집 시작: {}", startTime);
        
        stockCollectionUseCase.collectActiveStockPrices()
                .timeout(properties.timeout().activeCollection())
                .doOnSuccess(unused -> {
                    Duration elapsed = Duration.between(startTime, LocalDateTime.now());
                    log.info("주식 시세 정기 수집 완료: 소요시간 {}ms", elapsed.toMillis());
                })
                .doOnError(error -> {
                    Duration elapsed = Duration.between(startTime, LocalDateTime.now());
                    log.error("주식 시세 정기 수집 실패: 소요시간 {}ms, 에러: {}", 
                            elapsed.toMillis(), error.getMessage(), error);
                })
                .onErrorResume(error -> {
                    // 스케줄러가 중단되지 않도록 에러 무시
                    log.warn("스케줄러 에러 복구: 다음 수집 주기에 재시도");
                    return Mono.empty();
                })
                .subscribe(); // 비동기 실행
    }
    
    /**
     * 높은 우선순위 종목 즉시 수집 (15초마다)
     * - 우선순위 1-3 범위의 중요 종목만 대상
     * - 빠른 반응이 필요한 핵심 종목들
     */
    @Scheduled(fixedDelayString = "#{T(java.time.Duration).ofSeconds(15).toMillis()}", 
               initialDelayString = "#{T(java.time.Duration).ofSeconds(5).toMillis()}")
    public void collectHighPriorityStocks() {
        LocalDateTime startTime = LocalDateTime.now();
        log.debug("높은 우선순위 종목 수집 시작");
        
        stockCollectionUseCase.collectHighPriorityStocks()
                .timeout(properties.timeout().highPriority())
                .doOnSuccess(unused -> {
                    Duration elapsed = Duration.between(startTime, LocalDateTime.now());
                    log.debug("높은 우선순위 종목 수집 완료: 소요시간 {}ms", elapsed.toMillis());
                })
                .doOnError(error -> {
                    Duration elapsed = Duration.between(startTime, LocalDateTime.now());
                    log.warn("높은 우선순위 종목 수집 실패: 소요시간 {}ms, 에러: {}", 
                            elapsed.toMillis(), error.getMessage());
                })
                .onErrorResume(error -> {
                    log.debug("높은 우선순위 수집 에러 복구: 다음 주기에 재시도");
                    return Mono.empty();
                })
                .subscribe(); // 비동기 실행
    }
    
    /**
     * 코어 종목 집중 수집 (매 분마다)
     * - CORE 카테고리 종목들만 대상
     * - 가장 중요한 종목들에 대한 안정적 수집 보장
     */
    @Scheduled(cron = "0 * * * * *")
    public void collectCoreStocks() {
        LocalDateTime startTime = LocalDateTime.now();
        log.debug("코어 종목 집중 수집 시작");
        
        stockCollectionUseCase.collectStocksByCategory(properties.priority().coreCategory())
                .timeout(properties.timeout().coreStocks())
                .doOnSuccess(unused -> {
                    Duration elapsed = Duration.between(startTime, LocalDateTime.now());
                    log.debug("코어 종목 집중 수집 완료: 소요시간 {}ms", elapsed.toMillis());
                })
                .doOnError(error -> {
                    Duration elapsed = Duration.between(startTime, LocalDateTime.now());
                    log.warn("코어 종목 집중 수집 실패: 소요시간 {}ms, 에러: {}", 
                            elapsed.toMillis(), error.getMessage());
                })
                .onErrorResume(error -> {
                    log.debug("코어 종목 수집 에러 복구: 다음 주기에 재시도");
                    return Mono.empty();
                })
                .subscribe(); // 비동기 실행
    }
}
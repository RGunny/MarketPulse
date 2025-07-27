package me.rgunny.event.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.event.application.port.input.GetStockPriceUseCase;
import me.rgunny.event.application.port.input.StockCollectionUseCase;
import me.rgunny.event.application.port.output.WatchTargetPort;
import me.rgunny.event.domain.stock.WatchCategory;
import me.rgunny.event.domain.stock.WatchTarget;
import me.rgunny.event.infrastructure.config.StockCollectionProperties;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 주식 시세 배치 수집 서비스
 * 리액티브 스트림으로 비동기 배치 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockCollectionService implements StockCollectionUseCase {
    
    private final WatchTargetPort watchTargetPort;
    private final GetStockPriceUseCase stockPriceUseCase;
    private final StockCollectionProperties properties;
    
    // 각 종목의 마지막 수집 시간을 메모리에 캐싱
    private final Map<String, LocalDateTime> lastCollectionTimes = new ConcurrentHashMap<>();
    
    @Override
    public Mono<Void> collectActiveStockPrices() {
        log.info("활성화된 모든 종목 시세 수집 시작");
        
        return watchTargetPort.findActiveTargets()
                .filter(this::shouldCollectNow)
                .flatMap(this::collectStockPrice, properties.concurrency().defaultLimit())
                .doOnNext(symbol -> updateLastCollectionTime(symbol))
                .doOnComplete(() -> log.info("활성화된 종목 시세 수집 완료"))
                .doOnError(error -> log.error("활성화된 종목 시세 수집 중 오류 발생", error))
                .then();
    }
    
    @Override
    public Mono<Void> collectHighPriorityStocks() {
        log.info("높은 우선순위 종목 시세 즉시 수집 시작");
        
        return watchTargetPort.findHighPriorityTargets()
                .flatMap(this::collectStockPrice, properties.concurrency().highPriority())
                .doOnNext(symbol -> updateLastCollectionTime(symbol))
                .doOnComplete(() -> log.info("높은 우선순위 종목 시세 수집 완료"))
                .doOnError(error -> log.error("높은 우선순위 종목 시세 수집 중 오류 발생", error))
                .then();
    }
    
    @Override
    public Mono<Void> collectStocksByCategory(String category) {
        try {
            WatchCategory watchCategory = WatchCategory.valueOf(category.toUpperCase());
            log.info("카테고리 {} 종목 시세 수집 시작", watchCategory);
            
            return watchTargetPort.findActiveTargetsByCategory(watchCategory)
                    .filter(this::shouldCollectNow)
                    .flatMap(this::collectStockPrice, properties.concurrency().categoryLimit())
                    .doOnNext(symbol -> updateLastCollectionTime(symbol))
                    .doOnComplete(() -> log.info("카테고리 {} 종목 시세 수집 완료", watchCategory))
                    .doOnError(error -> log.error("카테고리 {} 종목 시세 수집 중 오류 발생", watchCategory, error))
                    .then();
        } catch (IllegalArgumentException e) {
            log.warn("잘못된 카테고리: {}", category);
            return Mono.empty();
        }
    }
    
    /**
     * 개별 종목 시세 수집
     * 에러 발생 시 로그만 남기고 다른 종목 수집은 계속 진행
     */
    private Mono<String> collectStockPrice(WatchTarget target) {
        log.debug("종목 시세 수집 시작: {} ({})", target.getName(), target.getSymbol());
        
        return stockPriceUseCase.getCurrentPriceAndSave(target.getSymbol())
                .map(stockPrice -> target.getSymbol())
                .doOnSuccess(symbol -> log.debug("종목 시세 수집 성공: {} ({})", target.getName(), symbol))
                .doOnError(error -> log.warn("종목 시세 수집 실패: {} ({}) - {}", 
                        target.getName(), target.getSymbol(), error.getMessage()))
                .onErrorReturn(target.getSymbol()) // 에러 발생해도 다른 종목 처리 계속
                .subscribeOn(Schedulers.boundedElastic()); // I/O 집약적 작업은 별도 스레드풀
    }
    
    /**
     * 수집 시기 판단 로직
     * WatchTarget의 collectInterval과 마지막 수집 시간 비교
     */
    private boolean shouldCollectNow(WatchTarget target) {
        LocalDateTime lastTime = lastCollectionTimes.get(target.getSymbol());
        boolean shouldCollect = target.shouldCollectNow(lastTime);
        
        if (shouldCollect) {
            log.debug("수집 대상: {} (interval: {}초)", target.getName(), target.getCollectInterval());
        } else {
            log.trace("수집 스킵: {} (아직 수집 주기가 안됨)", target.getName());
        }
        
        return shouldCollect;
    }
    
    /**
     * 마지막 수집 시간 업데이트
     */
    private void updateLastCollectionTime(String symbol) {
        lastCollectionTimes.put(symbol, LocalDateTime.now());
        log.trace("종목 {} 마지막 수집 시간 업데이트", symbol);
    }
    
    /**
     * 수집 통계 조회 (모니터링용)
     */
    public Mono<CollectionStats> getCollectionStats() {
        return Mono.fromCallable(() -> {
            int totalTracked = lastCollectionTimes.size();
            long recentCollections = lastCollectionTimes.values().stream()
                    .mapToLong(time -> Duration.between(time, LocalDateTime.now()).toMinutes())
                    .filter(minutes -> minutes <= 5) // 최근 5분 내 수집
                    .count();
            
            return new CollectionStats(totalTracked, (int) recentCollections);
        });
    }
    
    /**
     * 수집 통계 데이터 클래스
     */
    public record CollectionStats(int totalTrackedSymbols, int recentCollections) {}
}
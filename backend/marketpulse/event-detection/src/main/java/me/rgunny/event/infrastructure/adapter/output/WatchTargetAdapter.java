package me.rgunny.event.infrastructure.adapter.output;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.event.application.port.output.WatchTargetPort;
import me.rgunny.event.domain.stock.WatchCategory;
import me.rgunny.event.domain.stock.WatchTarget;
import me.rgunny.event.infrastructure.config.StockCollectionProperties;
import me.rgunny.event.infrastructure.repository.WatchTargetRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * WatchTarget 데이터 접근을 위한 어댑터
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WatchTargetAdapter implements WatchTargetPort {
    
    private final WatchTargetRepository watchTargetRepository;
    private final StockCollectionProperties properties;
    
    @Override
    public Flux<WatchTarget> findActiveTargets() {
        log.debug("활성화된 모든 감시 대상 조회 시작");
        return watchTargetRepository.findByActiveTrue()
                .doOnNext(target -> log.debug("활성 감시 대상 발견: {} ({})", target.getName(), target.getSymbol()))
                .doOnComplete(() -> log.debug("활성화된 감시 대상 조회 완료"));
    }
    
    @Override
    public Flux<WatchTarget> findHighPriorityTargets() {
        int minPriority = properties.priority().highMin();
        int maxPriority = properties.priority().highMax();
        log.debug("높은 우선순위 감시 대상 조회 시작 (우선순위 {}-{})", minPriority, maxPriority);
        return watchTargetRepository.findByPriorityBetweenAndActiveTrueOrderByPriorityAsc(minPriority, maxPriority)
                .doOnNext(target -> log.debug("높은 우선순위 감시 대상: {} (우선순위: {})", target.getName(), target.getPriority()))
                .doOnComplete(() -> log.debug("높은 우선순위 감시 대상 조회 완료"));
    }
    
    @Override
    public Flux<WatchTarget> findActiveTargetsByCategory(WatchCategory category) {
        log.debug("카테고리별 활성 감시 대상 조회: {}", category);
        return watchTargetRepository.findByCategoryAndActiveTrueOrderByPriorityAsc(category)
                .doOnNext(target -> log.debug("카테고리 {} 감시 대상: {}", category, target.getName()))
                .doOnComplete(() -> log.debug("카테고리 {} 감시 대상 조회 완료", category));
    }
    
    @Override
    public Mono<WatchTarget> findBySymbol(String symbol) {
        log.debug("종목코드로 감시 대상 조회: {}", symbol);
        return watchTargetRepository.findBySymbol(symbol)
                .doOnNext(target -> log.debug("감시 대상 발견: {} ({})", target.getName(), symbol))
                .doFinally(signalType -> log.debug("종목코드 {} 감시 대상 조회 완료", symbol));
    }
}
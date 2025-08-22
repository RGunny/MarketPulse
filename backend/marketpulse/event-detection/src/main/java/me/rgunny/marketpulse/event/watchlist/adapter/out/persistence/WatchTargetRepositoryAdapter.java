package me.rgunny.marketpulse.event.watchlist.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.event.watchlist.application.port.out.WatchTargetPort;
import me.rgunny.marketpulse.event.watchlist.domain.model.WatchCategory;
import me.rgunny.marketpulse.event.watchlist.domain.model.WatchTarget;
import me.rgunny.marketpulse.event.watchlist.adapter.config.WatchlistProperties;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * WatchTarget 데이터 접근을 위한 어댑터
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WatchTargetRepositoryAdapter implements WatchTargetPort {
    
    private final WatchTargetRepository watchTargetRepository;
    private final WatchlistProperties properties;
    
    @Override
    public Flux<WatchTarget> findActiveTargets() {
        log.debug("활성화된 모든 감시 대상 조회 시작");
        return watchTargetRepository.findByActiveTrue()
                .doOnNext(target -> log.debug("활성 감시 대상 발견: {} ({})", target.getName(), target.getSymbol()))
                .doOnComplete(() -> log.debug("활성화된 감시 대상 조회 완료"));
    }
    
    @Override
    public Flux<WatchTarget> findHighPriorityTargets() {
        log.debug("고우선순위 감시 대상 조회 시작");
        return findByPriorityRange(properties.priority().highMin(), properties.priority().highMax())
                .doOnNext(target -> log.debug("고우선순위 감시 대상: {} (우선순위: {})", target.getName(), target.getPriority()));
    }
    
    @Override
    public Flux<WatchTarget> findByPriorityRange(int minPriority, int maxPriority) {
        log.debug("우선순위 범위 감시 대상 조회 시작 (우선순위 {}-{})", minPriority, maxPriority);
        return watchTargetRepository.findByPriorityBetweenAndActiveTrueOrderByPriorityAsc(minPriority, maxPriority)
                .doOnNext(target -> log.debug("우선순위 범위 감시 대상: {} (우선순위: {})", target.getName(), target.getPriority()))
                .doOnComplete(() -> log.debug("우선순위 범위 감시 대상 조회 완료"));
    }
    
    @Override
    public Flux<WatchTarget> findByCategory(String category) {
        log.debug("카테고리별 활성 감시 대상 조회: {}", category);
        WatchCategory watchCategory = WatchCategory.valueOf(category);
        return watchTargetRepository.findByCategoryAndActiveTrueOrderByPriorityAsc(watchCategory)
                .doOnNext(target -> log.debug("카테고리 {} 감시 대상: {}", category, target.getName()))
                .doOnComplete(() -> log.debug("카테고리 {} 감시 대상 조회 완료", category));
    }
    
    @Override
    public Flux<WatchTarget> findActiveTargetsByCategory(WatchCategory category) {
        log.debug("활성화된 카테고리별 감시 대상 조회: {}", category);
        return watchTargetRepository.findByCategoryAndActiveTrueOrderByPriorityAsc(category)
                .doOnNext(target -> log.debug("카테고리 {} 활성 감시 대상: {}", category, target.getName()))
                .doOnComplete(() -> log.debug("카테고리 {} 활성 감시 대상 조회 완료", category));
    }
    
    @Override
    public Mono<WatchTarget> findBySymbol(String symbol) {
        log.debug("종목코드로 감시 대상 조회: {}", symbol);
        return watchTargetRepository.findBySymbol(symbol)
                .doOnNext(target -> log.debug("감시 대상 발견: {} ({})", target.getName(), symbol))
                .doFinally(signalType -> log.debug("종목코드 {} 감시 대상 조회 완료", symbol));
    }
    
    @Override
    public Mono<WatchTarget> save(WatchTarget watchTarget) {
        log.debug("감시 대상 저장: {} ({})", watchTarget.getName(), watchTarget.getSymbol());
        return watchTargetRepository.save(watchTarget)
                .doOnSuccess(saved -> log.debug("감시 대상 저장 완료: {}", saved.getSymbol()));
    }
    
    @Override
    public Mono<Void> deleteBySymbol(String symbol) {
        log.debug("감시 대상 삭제: {}", symbol);
        return watchTargetRepository.deleteBySymbol(symbol)
                .doOnSuccess(v -> log.debug("감시 대상 삭제 완료: {}", symbol));
    }
}
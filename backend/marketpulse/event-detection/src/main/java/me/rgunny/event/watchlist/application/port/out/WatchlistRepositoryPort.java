package me.rgunny.event.watchlist.application.port.out;

import me.rgunny.event.watchlist.domain.model.WatchCategory;
import me.rgunny.event.watchlist.domain.model.WatchTarget;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * WatchTarget 데이터 접근을 위한 출력 포트
 */
public interface WatchlistRepositoryPort {
    
    /**
     * 활성화된 모든 감시 대상 조회
     */
    Flux<WatchTarget> findActiveTargets();
    
    /**
     * 높은 우선순위 감시 대상 조회 (우선순위 1-3)
     */
    Flux<WatchTarget> findHighPriorityTargets();
    
    /**
     * 카테고리별 활성화된 감시 대상 조회
     */
    Flux<WatchTarget> findActiveTargetsByCategory(WatchCategory category);
    
    /**
     * 종목코드로 감시 대상 조회
     */
    Mono<WatchTarget> findBySymbol(String symbol);
}
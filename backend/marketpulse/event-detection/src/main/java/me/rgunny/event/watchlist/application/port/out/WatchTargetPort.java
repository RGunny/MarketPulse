package me.rgunny.event.watchlist.application.port.out;

import me.rgunny.event.watchlist.domain.model.WatchTarget;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 감시 대상 저장소 포트
 */
public interface WatchTargetPort {
    
    /**
     * 활성화된 모든 감시 대상 조회
     * @return 활성 감시 대상 목록
     */
    Flux<WatchTarget> findActiveTargets();
    
    /**
     * 고우선순위 감시 대상 조회 (우선순위 1-3)
     * @return 고우선순위 감시 대상 목록
     */
    Flux<WatchTarget> findHighPriorityTargets();
    
    /**
     * 우선순위 범위로 감시 대상 조회
     * @param minPriority 최소 우선순위
     * @param maxPriority 최대 우선순위
     * @return 해당 범위의 감시 대상 목록
     */
    Flux<WatchTarget> findByPriorityRange(int minPriority, int maxPriority);
    
    /**
     * 카테고리별 감시 대상 조회
     * @param category 카테고리명
     * @return 해당 카테고리의 감시 대상 목록
     */
    Flux<WatchTarget> findByCategory(String category);
    
    /**
     * 활성화된 카테고리별 감시 대상 조회
     * @param category 카테고리
     * @return 해당 카테고리의 활성 감시 대상 목록
     */
    Flux<WatchTarget> findActiveTargetsByCategory(me.rgunny.event.watchlist.domain.model.WatchCategory category);
    
    /**
     * 종목 코드로 감시 대상 조회
     * @param symbol 종목 코드
     * @return 감시 대상 정보
     */
    Mono<WatchTarget> findBySymbol(String symbol);
    
    /**
     * 감시 대상 저장
     * @param watchTarget 저장할 감시 대상
     * @return 저장된 감시 대상
     */
    Mono<WatchTarget> save(WatchTarget watchTarget);
    
    /**
     * 감시 대상 삭제
     * @param symbol 종목 코드
     * @return 삭제 완료 신호
     */
    Mono<Void> deleteBySymbol(String symbol);
}
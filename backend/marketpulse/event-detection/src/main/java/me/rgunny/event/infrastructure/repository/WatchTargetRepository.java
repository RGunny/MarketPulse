package me.rgunny.event.infrastructure.repository;

import me.rgunny.event.domain.stock.WatchCategory;
import me.rgunny.event.domain.stock.WatchTarget;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface WatchTargetRepository extends ReactiveMongoRepository<WatchTarget, String> {
    
    // 활성화된 감시 대상 조회
    Flux<WatchTarget> findByActiveTrue();
    
    // 카테고리별 활성화된 감시 대상 조회
    Flux<WatchTarget> findByCategoryAndActiveTrueOrderByPriorityAsc(WatchCategory category);
    
    // 종목코드로 조회
    Mono<WatchTarget> findBySymbol(String symbol);
    
    // 우선순위 범위로 조회
    Flux<WatchTarget> findByPriorityBetweenAndActiveTrueOrderByPriorityAsc(int minPriority, int maxPriority);
    
    // 수집 주기별 조회 (특정 주기 이하)
    Flux<WatchTarget> findByCollectIntervalLessThanEqualAndActiveTrueOrderByPriorityAsc(int maxInterval);
    
    // 테마별 조회
    Flux<WatchTarget> findByThemeAndActiveTrueOrderByPriorityAsc(String theme);
}
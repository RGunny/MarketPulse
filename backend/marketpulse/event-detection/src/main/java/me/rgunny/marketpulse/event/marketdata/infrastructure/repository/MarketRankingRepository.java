package me.rgunny.marketpulse.event.marketdata.infrastructure.repository;

import me.rgunny.marketpulse.event.marketdata.domain.model.MarketRanking;
import me.rgunny.marketpulse.event.marketdata.domain.model.MarketType;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface MarketRankingRepository extends ReactiveMongoRepository<MarketRanking, String> {
    
    /**
     * 타입별 최신 순위 조회
     */
    @Query(value = "{'rankingType': ?0, 'capturedAt': {$gte: ?1}}", 
           sort = "{'capturedAt': -1, 'rank': 1}")
    Flux<MarketRanking> findByRankingTypeAndCapturedAtAfterOrderByCapturedAtDescRankAsc(
            MarketRanking.RankingType type, LocalDateTime after);
    
    /**
     * 최신 순위 조회 (limit 적용)
     */
    @Query(value = "{'rankingType': ?0}", 
           sort = "{'capturedAt': -1, 'rank': 1}")
    Flux<MarketRanking> findTopByRankingTypeOrderByCapturedAtDescRankAsc(
            MarketRanking.RankingType type);
    
    /**
     * 종목별 최근 순위 이력
     */
    @Query(value = "{'symbol': ?0, 'capturedAt': {$gte: ?1}}", 
           sort = "{'capturedAt': -1}")
    Flux<MarketRanking> findBySymbolAndCapturedAtAfterOrderByCapturedAtDesc(
            String symbol, LocalDateTime after);
    
    /**
     * 시장별 타입별 최신 순위
     */
    @Query(value = "{'marketType': ?0, 'rankingType': ?1}", 
           sort = "{'capturedAt': -1, 'rank': 1}")
    Flux<MarketRanking> findByMarketTypeAndRankingTypeOrderByCapturedAtDescRankAsc(
            MarketType market, MarketRanking.RankingType type);
    
    /**
     * 자동 감시 대상 조회 (상위 10위 이내 등)
     */
    @Query(value = "{$and: [" +
            "{'capturedAt': {$gte: ?0}}," +
            "{$or: [" +
                "{'rankingType': 'PRICE_RISE', 'rank': {$lte: 10}}," +
                "{'rankingType': 'PRICE_FALL', 'rank': {$lte: 10}}," +
                "{'rankingType': 'VOLUME_SURGE', 'rank': {$lte: 5}}," +
                "{'rankingType': {$in: ['LIMIT_UP', 'LIMIT_DOWN']}}" +
            "]}" +
           "]}")
    Flux<MarketRanking> findAutoWatchCandidates(LocalDateTime after);
    
    /**
     * 오래된 데이터 삭제
     */
    Mono<Long> deleteByCapturedAtBefore(LocalDateTime before);
}
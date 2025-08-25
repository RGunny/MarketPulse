package me.rgunny.marketpulse.event.marketdata.application.port.out;

import me.rgunny.marketpulse.event.marketdata.domain.model.MarketRanking;
import me.rgunny.marketpulse.event.marketdata.domain.model.MarketType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * 시장 순위 데이터 관리 Port
 */
public interface MarketRankingPort {
    
    /**
     * 순위 데이터 저장
     */
    Mono<MarketRanking> save(MarketRanking ranking);
    
    /**
     * 여러 순위 데이터 일괄 저장
     */
    Flux<MarketRanking> saveAll(Flux<MarketRanking> rankings);
    
    /**
     * 특정 시점의 순위 조회
     */
    Flux<MarketRanking> findByRankingTypeAndCapturedAtAfter(
            MarketRanking.RankingType type, LocalDateTime after);
    
    /**
     * 최신 순위 조회 (타입별)
     */
    Flux<MarketRanking> findLatestByRankingType(MarketRanking.RankingType type, int limit);
    
    /**
     * 종목별 최근 순위 이력 조회
     */
    Flux<MarketRanking> findBySymbolRecent(String symbol, int days);
    
    /**
     * 자동 감시 대상 조회 (shouldAutoWatch = true)
     */
    Flux<MarketRanking> findAutoWatchCandidates(LocalDateTime after);
    
    /**
     * 시장별 최신 순위 조회
     */
    Flux<MarketRanking> findLatestByMarketAndType(
            MarketType market, MarketRanking.RankingType type, int limit);
    
    /**
     * 오래된 데이터 삭제 (데이터 관리)
     */
    Mono<Long> deleteOlderThan(LocalDateTime before);
}
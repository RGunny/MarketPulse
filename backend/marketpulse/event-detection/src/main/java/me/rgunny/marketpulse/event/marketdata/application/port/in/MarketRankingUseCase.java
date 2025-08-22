package me.rgunny.marketpulse.event.marketdata.application.port.in;

import me.rgunny.marketpulse.event.marketdata.domain.model.MarketRanking;
import me.rgunny.marketpulse.event.marketdata.domain.model.MarketType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 시장 순위 조회 유스케이스
 */
public interface MarketRankingUseCase {
    
    /**
     * 시장 전체 순위 수집 및 저장
     * - 상승률/하락률/거래량 상위 종목 조회
     * - 자동 WatchTarget 등록
     */
    Mono<MarketRankingResult> collectMarketRankings(MarketType marketType);
    
    /**
     * 특정 타입 순위 수집
     */
    Mono<MarketRankingResult> collectRankingByType(
            MarketType marketType, MarketRanking.RankingType rankingType);
    
    /**
     * 최신 순위 조회
     */
    Flux<MarketRanking> getLatestRankings(MarketRanking.RankingType type, int limit);
    
    /**
     * 급등/급락 종목 자동 감지 및 WatchTarget 등록
     */
    Mono<AutoWatchResult> detectAndRegisterAnomalies();
    
    /**
     * 순위 수집 결과
     */
    record MarketRankingResult(
            MarketType marketType,
            MarketRanking.RankingType rankingType,
            int totalCount,
            int savedCount,
            int autoWatchCount,
            long elapsedMillis
    ) {
        public static MarketRankingResult success(
                MarketType marketType,
                MarketRanking.RankingType rankingType,
                int totalCount,
                int savedCount,
                int autoWatchCount,
                long elapsedMillis) {
            return new MarketRankingResult(
                    marketType, rankingType, totalCount, 
                    savedCount, autoWatchCount, elapsedMillis);
        }
    }
    
    /**
     * 자동 감시 등록 결과
     */
    record AutoWatchResult(
            int candidateCount,     // 후보 종목 수
            int registeredCount,    // 신규 등록 수
            int skippedCount,       // 이미 등록된 수
            int failedCount         // 실패 수
    ) {
        public static AutoWatchResult of(
                int candidateCount, int registeredCount, 
                int skippedCount, int failedCount) {
            return new AutoWatchResult(
                    candidateCount, registeredCount, skippedCount, failedCount);
        }
    }
}
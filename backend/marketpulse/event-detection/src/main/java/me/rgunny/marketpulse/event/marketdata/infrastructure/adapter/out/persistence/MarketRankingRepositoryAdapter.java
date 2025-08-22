package me.rgunny.marketpulse.event.marketdata.infrastructure.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.event.marketdata.application.port.out.MarketRankingPort;
import me.rgunny.marketpulse.event.marketdata.domain.model.MarketRanking;
import me.rgunny.marketpulse.event.marketdata.domain.model.MarketType;
import me.rgunny.marketpulse.event.marketdata.infrastructure.repository.MarketRankingRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * MarketRanking 데이터 접근 어댑터
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MarketRankingRepositoryAdapter implements MarketRankingPort {
    
    private final MarketRankingRepository marketRankingRepository;
    
    @Override
    public Mono<MarketRanking> save(MarketRanking ranking) {
        return marketRankingRepository.save(ranking)
                .doOnNext(saved -> log.debug("MarketRanking saved: {} - {} (rank: {})",
                        saved.getSymbol(), saved.getRankingType(), saved.getRank()));
    }
    
    @Override
    public Flux<MarketRanking> saveAll(Flux<MarketRanking> rankings) {
        return marketRankingRepository.saveAll(rankings)
                .doOnComplete(() -> log.debug("Batch MarketRanking save completed"));
    }
    
    @Override
    public Flux<MarketRanking> findByRankingTypeAndCapturedAtAfter(
            MarketRanking.RankingType type, LocalDateTime after) {
        return marketRankingRepository
                .findByRankingTypeAndCapturedAtAfterOrderByCapturedAtDescRankAsc(type, after);
    }
    
    @Override
    public Flux<MarketRanking> findLatestByRankingType(
            MarketRanking.RankingType type, int limit) {
        return marketRankingRepository
                .findTopByRankingTypeOrderByCapturedAtDescRankAsc(type)
                .take(limit);
    }
    
    @Override
    public Flux<MarketRanking> findBySymbolRecent(String symbol, int days) {
        LocalDateTime after = LocalDateTime.now().minusDays(days);
        return marketRankingRepository
                .findBySymbolAndCapturedAtAfterOrderByCapturedAtDesc(symbol, after);
    }
    
    @Override
    public Flux<MarketRanking> findAutoWatchCandidates(LocalDateTime after) {
        return marketRankingRepository.findAutoWatchCandidates(after)
                .doOnNext(ranking -> log.debug("Auto-watch candidate: {} - {} (rank: {})",
                        ranking.getSymbol(), ranking.getRankingType(), ranking.getRank()));
    }
    
    @Override
    public Flux<MarketRanking> findLatestByMarketAndType(
            MarketType market, MarketRanking.RankingType type, int limit) {
        return marketRankingRepository
                .findByMarketTypeAndRankingTypeOrderByCapturedAtDescRankAsc(market, type)
                .take(limit);
    }
    
    @Override
    public Mono<Long> deleteOlderThan(LocalDateTime before) {
        return marketRankingRepository.deleteByCapturedAtBefore(before)
                .doOnNext(count -> log.info("Deleted {} old MarketRanking records", count));
    }
}
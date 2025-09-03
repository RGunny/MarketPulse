package me.rgunny.marketpulse.event.marketdata.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.event.marketdata.application.port.in.MarketRankingUseCase;
import me.rgunny.marketpulse.event.marketdata.application.port.out.MarketRankingPort;
import me.rgunny.marketpulse.event.marketdata.application.port.out.shared.StockPort;
import me.rgunny.marketpulse.event.marketdata.domain.model.MarketRanking;
import me.rgunny.marketpulse.event.marketdata.domain.model.MarketType;
import me.rgunny.marketpulse.event.marketdata.domain.model.Stock;
import me.rgunny.marketpulse.event.marketdata.infrastructure.adapter.out.kis.KISMarketRankingAdapter;
import me.rgunny.marketpulse.event.marketdata.infrastructure.config.MarketRankingProperties;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 시장 순위 조회 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketRankingService implements MarketRankingUseCase {

    private final KISMarketRankingAdapter kisRankingAdapter;
    private final MarketRankingPort marketRankingPort;
//    private final WatchTargetPort watchTargetPort;
    private final StockPort stockPort;
    private final MarketRankingProperties rankingProperties;

    private static final int DEFAULT_RANKING_LIMIT = 30;

    @Override
    public Mono<MarketRankingResult> collectMarketRankings(MarketType marketType) {
        log.info("Starting market rankings collection: {}", marketType);
        long startTime = System.currentTimeMillis();

        AtomicInteger totalCount = new AtomicInteger(0);
        AtomicInteger savedCount = new AtomicInteger(0);
        AtomicInteger autoWatchCount = new AtomicInteger(0);

        // 상승률, 하락률, 거래량 순위 모두 수집
        return Flux.merge(
                        collectAndSaveRankings(marketType, MarketRanking.RankingType.PRICE_RISE),
                        collectAndSaveRankings(marketType, MarketRanking.RankingType.PRICE_FALL),
                        collectAndSaveRankings(marketType, MarketRanking.RankingType.VOLUME_SURGE)
                )
                .doOnNext(ranking -> {
                    totalCount.incrementAndGet();
                    savedCount.incrementAndGet();

                    if (rankingProperties.autoWatch().enabled() && shouldAutoWatch(ranking)) {
                        autoWatchCount.incrementAndGet();
//                        registerAutoWatchTarget(ranking).subscribe();
                    }
                })
                .then(Mono.fromCallable(() -> {
                    long elapsed = System.currentTimeMillis() - startTime;
                    log.info("Market rankings collection completed: market={}, total={}, saved={}, autoWatch={}, elapsed={}ms",
                            marketType, totalCount.get(), savedCount.get(), autoWatchCount.get(), elapsed);

                    return MarketRankingResult.success(
                            marketType,
                            null,
                            totalCount.get(),
                            savedCount.get(),
                            autoWatchCount.get(),
                            elapsed
                    );
                }));
    }

    @Override
    public Mono<MarketRankingResult> collectRankingByType(
            MarketType marketType, MarketRanking.RankingType rankingType) {
//
//        log.info("Collecting rankings: market={}, type={}", marketType, rankingType);
//        long startTime = System.currentTimeMillis();
//
//        AtomicInteger totalCount = new AtomicInteger(0);
//        AtomicInteger savedCount = new AtomicInteger(0);
//        AtomicInteger autoWatchCount = new AtomicInteger(0);
//
//        return collectAndSaveRankings(marketType, rankingType)
//                .doOnNext(ranking -> {
//                    totalCount.incrementAndGet();
//                    savedCount.incrementAndGet();
//
//                    if (ranking.shouldAutoWatch()) {
//                        autoWatchCount.incrementAndGet();
//                        registerAutoWatchTarget(ranking).subscribe();
//                    }
//                })
//                .then(Mono.fromCallable(() -> {
//                    long elapsed = System.currentTimeMillis() - startTime;
//                    return MarketRankingResult.success(
//                            marketType,
//                            rankingType,
//                            totalCount.get(),
//                            savedCount.get(),
//                            autoWatchCount.get(),
//                            elapsed
//                    );
//                }));
        return null;
    }

    @Override
    public Flux<MarketRanking> getLatestRankings(MarketRanking.RankingType type, int limit) {
        return marketRankingPort.findLatestByRankingType(type, limit);
    }

    @Override
    public Mono<AutoWatchResult> detectAndRegisterAnomalies() {
        log.info("Detecting market anomalies for auto-watch registration");

        AtomicInteger candidateCount = new AtomicInteger(0);
        AtomicInteger registeredCount = new AtomicInteger(0);
        AtomicInteger skippedCount = new AtomicInteger(0);
        AtomicInteger failedCount = new AtomicInteger(0);

        // 최근 10분 이내 데이터에서 자동 감시 대상 추출
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);

        return null;
//        return marketRankingPort.findAutoWatchCandidates(tenMinutesAgo)
//                .doOnNext(ranking -> candidateCount.incrementAndGet())
//                .flatMap(ranking -> registerAutoWatchTarget(ranking)
//                        .doOnNext(registered -> {
//                            if (registered) {
//                                registeredCount.incrementAndGet();
//                            } else {
//                                skippedCount.incrementAndGet();
//                            }
//                        })
//                        .onErrorResume(error -> {
//                            log.error("Failed to register auto-watch: {}", ranking.getSymbol(), error);
//                            failedCount.incrementAndGet();
//                            return Mono.just(false);
//                        })
//                )
//                .then(Mono.fromCallable(() -> {
//                    log.info("Auto-watch detection completed: candidates={}, registered={}, skipped={}, failed={}",
//                            candidateCount.get(), registeredCount.get(), skippedCount.get(), failedCount.get());
//
//                    return AutoWatchResult.of(
//                            candidateCount.get(),
//                            registeredCount.get(),
//                            skippedCount.get(),
//                            failedCount.get()
//                    );
//                }));
    }

    /**
     * 순위별 데이터 수집 및 저장
     */
    private Flux<MarketRanking> collectAndSaveRankings(
            MarketType marketType, MarketRanking.RankingType rankingType) {

        Flux<MarketRanking> rankings = switch (rankingType) {
            case PRICE_RISE -> kisRankingAdapter.fetchTopGainers(marketType, DEFAULT_RANKING_LIMIT);
            case PRICE_FALL -> kisRankingAdapter.fetchTopLosers(marketType, DEFAULT_RANKING_LIMIT);
            case VOLUME_SURGE -> kisRankingAdapter.fetchVolumeLeaders(marketType, DEFAULT_RANKING_LIMIT);
            case LIMIT_UP, LIMIT_DOWN, NEW_HIGH, NEW_LOW -> Flux.empty(); // 향후 구현 예정
        };

        return rankings
                .flatMap(ranking -> marketRankingPort.save(ranking))
                .doOnError(error -> log.error("Failed to save ranking data", error))
                .onErrorResume(error -> Flux.empty());
    }

    /**
     * 자동 등록 대상 여부 판단
     */
    private boolean shouldAutoWatch(MarketRanking ranking) {
        return ranking.shouldAutoWatch(
                rankingProperties.autoWatch().priceRiseLimit(),
                rankingProperties.autoWatch().priceFallLimit(),
                rankingProperties.autoWatch().volumeSurgeLimit()
        );
    }

    /**
     * 자동 WatchTarget 등록
     */
//    private Mono<Boolean> registerAutoWatchTarget(MarketRanking ranking) {
//        String symbol = ranking.getSymbol();
//
//        // 이미 등록된 종목인지 확인
//        return watchTargetPort.findBySymbol(symbol)
//                .hasElement()
//                .flatMap(exists -> {
//                    if (exists) {
//                        log.debug("WatchTarget already exists: {}", symbol);
//                        return Mono.just(false);
//                    }
//
//                    // Stock 마스터에 없으면 생성
//                    return ensureStockExists(ranking)
//                            .then(createAutoWatchTarget(ranking))
//                            .map(target -> {
//                                log.info("Auto-registered WatchTarget: {} ({}) - {}",
//                                        target.getName(), target.getSymbol(),
//                                        ranking.getRankingType().getDescription());
//                                return true;
//                            });
//                });
//    }

    /**
     * Stock 마스터 데이터 확인 및 생성
     */
    private Mono<Stock> ensureStockExists(MarketRanking ranking) {
        return stockPort.findBySymbol(ranking.getSymbol())
                .switchIfEmpty(Mono.defer(() -> {
                    // Stock이 없으면 생성
                    Stock newStock = Stock.createStock(
                            ranking.getSymbol(),
                            ranking.getName(),
                            ranking.getName(), // 영문명은 일단 한글명과 동일
                            ranking.getMarketType(),
                            "99", // 업종코드 미상
                            "기타" // 업종명 미상
                    );
                    return stockPort.save(newStock)
                            .doOnNext(saved -> log.info("Created new Stock: {} ({})",
                                    saved.getName(), saved.getSymbol()));
                }));
    }

    /**
     * 자동 WatchTarget 생성
     */
//    private Mono<WatchTarget> createAutoWatchTarget(MarketRanking ranking) {
//        LocalDateTime now = LocalDateTime.now();
//
//        // 순위 타입에 따라 카테고리 결정
//        WatchCategory category = switch (ranking.getRankingType()) {
//            case PRICE_RISE, PRICE_FALL -> WatchCategory.MOMENTUM;
//            case VOLUME_SURGE -> WatchCategory.THEME;
//            case LIMIT_UP, LIMIT_DOWN -> WatchCategory.MOMENTUM;
//            case NEW_HIGH, NEW_LOW -> WatchCategory.THEME;
//        };
//
//        String description = String.format("[자동등록] %s %d위 - %s",
//                ranking.getRankingType().getDescription(),
//                ranking.getRank(),
//                now.toLocalDate());
//
//        WatchTarget target = new WatchTarget(
//                null,
//                ranking.getSymbol(),
//                ranking.getName(),
//                category,
//                null, // tags
//                rankingProperties.autoWatch().priority(),
//                rankingProperties.autoWatch().collectIntervalSeconds(),
//                true, // active
//                description,
//                now,
//                now
//        );
//
//        return watchTargetPort.save(target);
//    }
}
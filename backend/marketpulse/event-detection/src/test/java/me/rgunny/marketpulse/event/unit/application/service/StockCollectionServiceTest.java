package me.rgunny.marketpulse.event.unit.application.service;

import me.rgunny.marketpulse.event.marketdata.application.port.in.MarketHoursUseCase;
import me.rgunny.marketpulse.event.watchlist.application.port.out.WatchTargetPort;
import me.rgunny.marketpulse.event.marketdata.infrastructure.adapter.in.scheduler.StockPriceCollectionScheduler;
import me.rgunny.marketpulse.event.marketdata.domain.model.StockPrice;
import me.rgunny.marketpulse.event.watchlist.domain.model.WatchCategory;
import me.rgunny.marketpulse.event.watchlist.domain.model.WatchTarget;
import me.rgunny.marketpulse.event.fixture.StockPriceTestFixture;
import me.rgunny.marketpulse.event.marketdata.infrastructure.config.shared.StockCollectionProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockPriceCollectionScheduler 단위 테스트")
class StockPriceCollectionSchedulerTest {
    
    @Mock
    private WatchTargetPort watchTargetPort;
    
    @Mock
    private me.rgunny.marketpulse.event.marketdata.application.port.in.CollectStockPriceUseCase stockPriceUseCase;
    
    @Mock
    private MarketHoursUseCase marketHoursUseCase;
    
    @Mock 
    private StockCollectionProperties properties;
    
    private StockPriceCollectionScheduler stockCollectionService;
    
    @BeforeEach
    void setUp() {
        stockCollectionService = new StockPriceCollectionScheduler(watchTargetPort, stockPriceUseCase, marketHoursUseCase, properties);
    }
    
    @Nested
    @DisplayName("활성화된 모든 종목 수집")
    class CollectActiveStockPricesTests {
        
        @Test
        @DisplayName("활성화된 종목들을 성공적으로 수집한다")
        void givenActiveTargets_whenCollectActiveStockPrices_thenCollectsSuccessfully() {
            // given
            StockCollectionProperties.Concurrency concurrency = new StockCollectionProperties.Concurrency(10, 5, 8);
            given(properties.concurrency()).willReturn(concurrency);
            
            WatchTarget target1 = createWatchTarget("005930", "삼성전자", 30);
            WatchTarget target2 = createWatchTarget("035720", "카카오", 60);
            StockPrice stockPrice = StockPriceTestFixture.samsung();
            
            given(watchTargetPort.findActiveTargets()).willReturn(Flux.just(target1, target2));
            given(stockPriceUseCase.getCurrentPriceAndSave(anyString())).willReturn(Mono.just(stockPrice));
            
            // when & then
            StepVerifier.create(stockCollectionService.collectActiveStocksReactive())
                    .verifyComplete();
            
            verify(watchTargetPort).findActiveTargets();
            verify(stockPriceUseCase).getCurrentPriceAndSave("005930");
            verify(stockPriceUseCase).getCurrentPriceAndSave("035720");
        }
        
        @Test
        @DisplayName("수집 주기가 안된 종목은 스킵한다")
        void givenRecentlyCollectedTarget_whenCollectActiveStockPrices_thenSkipsRecentlyCollected() {
            // given - 방금 수집된 종목 (30초 주기)
            StockCollectionProperties.Concurrency concurrency = new StockCollectionProperties.Concurrency(10, 5, 8);
            given(properties.concurrency()).willReturn(concurrency);
            
            WatchTarget recentTarget = createWatchTarget("005930", "삼성전자", 30);
            
            // 먼저 한 번 수집하여 lastCollectionTimes에 기록
            given(watchTargetPort.findActiveTargets()).willReturn(Flux.just(recentTarget));
            given(stockPriceUseCase.getCurrentPriceAndSave("005930")).willReturn(Mono.just(StockPriceTestFixture.samsung()));
            
            StepVerifier.create(stockCollectionService.collectActiveStocksReactive()).verifyComplete();
            
            // when - 즉시 다시 수집 시도 (30초가 지나지 않음)
            StepVerifier.create(stockCollectionService.collectActiveStocksReactive())
                    .verifyComplete();
            
            // then - 두 번째 호출에서는 수집하지 않음 (이미 최근에 수집됨)
            verify(stockPriceUseCase).getCurrentPriceAndSave("005930"); // 첫 번째만 호출됨
        }
        
        @Test
        @DisplayName("일부 종목 수집 실패해도 다른 종목은 계속 수집한다")
        void givenFailingTarget_whenCollectActiveStockPrices_thenContinuesWithOthers() {
            // given
            StockCollectionProperties.Concurrency concurrency = new StockCollectionProperties.Concurrency(10, 5, 8);
            given(properties.concurrency()).willReturn(concurrency);
            
            WatchTarget target1 = createWatchTarget("005930", "삼성전자", 30);
            WatchTarget target2 = createWatchTarget("035720", "카카오", 60);
            
            given(watchTargetPort.findActiveTargets()).willReturn(Flux.just(target1, target2));
            given(stockPriceUseCase.getCurrentPriceAndSave("005930"))
                    .willReturn(Mono.error(new RuntimeException("API 호출 실패")));
            given(stockPriceUseCase.getCurrentPriceAndSave("035720"))
                    .willReturn(Mono.just(StockPriceTestFixture.kakao()));
            
            // when & then
            StepVerifier.create(stockCollectionService.collectActiveStocksReactive())
                    .verifyComplete();
            
            verify(stockPriceUseCase).getCurrentPriceAndSave("005930");
            verify(stockPriceUseCase).getCurrentPriceAndSave("035720");
        }
        
        @Test
        @DisplayName("활성화된 종목이 없으면 아무것도 수집하지 않는다")
        void givenNoActiveTargets_whenCollectActiveStockPrices_thenDoesNothing() {
            // given
            StockCollectionProperties.Concurrency concurrency = new StockCollectionProperties.Concurrency(10, 5, 8);
            given(properties.concurrency()).willReturn(concurrency);
            
            given(watchTargetPort.findActiveTargets()).willReturn(Flux.empty());
            
            // when & then
            StepVerifier.create(stockCollectionService.collectActiveStocksReactive())
                    .verifyComplete();
            
            verify(watchTargetPort).findActiveTargets();
            verifyNoInteractions(stockPriceUseCase);
        }
    }
    
    @Nested
    @DisplayName("높은 우선순위 종목 수집")
    class CollectHighPriorityStocksTests {
        
        @Test
        @DisplayName("높은 우선순위 종목들을 성공적으로 수집한다")
        void givenHighPriorityTargets_whenCollectHighPriorityStocks_thenCollectsSuccessfully() {
            // given
            StockCollectionProperties.Concurrency concurrency = new StockCollectionProperties.Concurrency(10, 5, 8);
            given(properties.concurrency()).willReturn(concurrency);
            
            WatchTarget target1 = createWatchTarget("005930", "삼성전자", 1, 15);
            WatchTarget target2 = createWatchTarget("035720", "카카오", 2, 20);
            
            given(watchTargetPort.findHighPriorityTargets()).willReturn(Flux.just(target1, target2));
            given(stockPriceUseCase.getCurrentPriceAndSave(anyString()))
                    .willReturn(Mono.just(StockPriceTestFixture.samsung()));
            
            // when & then
            StepVerifier.create(stockCollectionService.collectHighPriorityStocksReactive())
                    .verifyComplete();
            
            verify(watchTargetPort).findHighPriorityTargets();
            verify(stockPriceUseCase).getCurrentPriceAndSave("005930");
            verify(stockPriceUseCase).getCurrentPriceAndSave("035720");
        }
    }
    
    @Nested
    @DisplayName("카테고리별 종목 수집")
    class CollectStocksByCategoryTests {
        
        @Test
        @DisplayName("유효한 카테고리의 종목들을 성공적으로 수집한다")
        void givenValidCategory_whenCollectStocksByCategory_thenCollectsSuccessfully() {
            // given
            StockCollectionProperties.Concurrency concurrency = new StockCollectionProperties.Concurrency(10, 5, 8);
            given(properties.concurrency()).willReturn(concurrency);
            
            WatchTarget coreTarget = createWatchTarget("005930", "삼성전자", WatchCategory.CORE, 30);
            
            given(watchTargetPort.findActiveTargetsByCategory(WatchCategory.CORE))
                    .willReturn(Flux.just(coreTarget));
            given(stockPriceUseCase.getCurrentPriceAndSave("005930"))
                    .willReturn(Mono.just(StockPriceTestFixture.samsung()));
            
            // when & then
            StepVerifier.create(stockCollectionService.collectStocksByCategory("CORE"))
                    .verifyComplete();
            
            verify(watchTargetPort).findActiveTargetsByCategory(WatchCategory.CORE);
            verify(stockPriceUseCase).getCurrentPriceAndSave("005930");
        }
        
        @Test
        @DisplayName("유효하지 않은 카테고리는 빈 결과를 반환한다")
        void givenInvalidCategory_whenCollectStocksByCategory_thenReturnsEmpty() {
            // when & then
            StepVerifier.create(stockCollectionService.collectStocksByCategory("INVALID"))
                    .verifyComplete();
            
            verifyNoInteractions(watchTargetPort);
            verifyNoInteractions(stockPriceUseCase);
        }
    }
    
    @Nested
    @DisplayName("수집 통계")
    class CollectionStatsTests {
        
        @Test
        @DisplayName("수집 통계를 정확히 반환한다")
        void whenGetCollectionStats_thenReturnsCorrectStats() {
            // given - 먼저 몇 개 종목을 수집하여 통계 데이터 생성
            StockCollectionProperties.Concurrency concurrency = new StockCollectionProperties.Concurrency(10, 5, 8);
            given(properties.concurrency()).willReturn(concurrency);
            
            WatchTarget target = createWatchTarget("005930", "삼성전자", 30);
            
            given(watchTargetPort.findActiveTargets()).willReturn(Flux.just(target));
            given(stockPriceUseCase.getCurrentPriceAndSave("005930"))
                    .willReturn(Mono.just(StockPriceTestFixture.samsung()));
            
            StepVerifier.create(stockCollectionService.collectActiveStocksReactive()).verifyComplete();
            
            // when & then
            StepVerifier.create(stockCollectionService.getCurrentStatus())
                    .expectNextMatches(stats -> 
                            stats.totalTrackedSymbols() == 1 && stats.recentCollections() == 1)
                    .verifyComplete();
        }
    }
    
    // 헬퍼 메서드들
    private WatchTarget createWatchTarget(String symbol, String name, int collectInterval) {
        return createWatchTarget(symbol, name, WatchCategory.CORE, 1, collectInterval);
    }
    
    private WatchTarget createWatchTarget(String symbol, String name, int priority, int collectInterval) {
        return createWatchTarget(symbol, name, WatchCategory.CORE, priority, collectInterval);
    }
    
    private WatchTarget createWatchTarget(String symbol, String name, WatchCategory category, int collectInterval) {
        return createWatchTarget(symbol, name, category, 1, collectInterval);
    }
    
    private WatchTarget createWatchTarget(String symbol, String name, WatchCategory category, int priority, int collectInterval) {
        LocalDateTime now = LocalDateTime.now();
        return new WatchTarget(
                null, symbol, name, category, null,
                priority, collectInterval, true, "테스트용",
                now, now
        );
    }
}
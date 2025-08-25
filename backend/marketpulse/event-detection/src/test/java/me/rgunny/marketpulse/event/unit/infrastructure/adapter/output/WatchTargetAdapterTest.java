package me.rgunny.marketpulse.event.unit.infrastructure.adapter.output;

import me.rgunny.marketpulse.event.watchlist.domain.model.WatchCategory;
import me.rgunny.marketpulse.event.watchlist.domain.model.WatchTarget;
import me.rgunny.marketpulse.event.watchlist.adapter.out.persistence.WatchTargetRepositoryAdapter;
import me.rgunny.marketpulse.event.watchlist.adapter.config.WatchlistProperties;
import me.rgunny.marketpulse.event.watchlist.adapter.out.persistence.WatchTargetRepository;
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

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("WatchTargetAdapter 단위 테스트")
class WatchTargetAdapterTest {
    
    @Mock
    private WatchTargetRepository watchTargetRepository;
    
    @Mock
    private WatchlistProperties properties;
    
    private WatchTargetRepositoryAdapter watchTargetAdapter;
    
    @BeforeEach
    void setUp() {
        watchTargetAdapter = new WatchTargetRepositoryAdapter(watchTargetRepository, properties);
    }
    
    @Nested
    @DisplayName("활성화된 감시 대상 조회")
    class FindActiveTargetsTests {
        
        @Test
        @DisplayName("활성화된 모든 감시 대상을 성공적으로 조회한다")
        void whenFindActiveTargets_thenReturnsActiveTargets() {
            // given
            WatchTarget target1 = createWatchTarget("005930", "삼성전자", true);
            WatchTarget target2 = createWatchTarget("035720", "카카오", true);
            
            given(watchTargetRepository.findByActiveTrue()).willReturn(Flux.just(target1, target2));
            
            // when & then
            StepVerifier.create(watchTargetAdapter.findActiveTargets())
                    .expectNext(target1)
                    .expectNext(target2)
                    .verifyComplete();
            
            verify(watchTargetRepository).findByActiveTrue();
        }
        
        @Test
        @DisplayName("활성화된 감시 대상이 없으면 빈 결과를 반환한다")
        void whenNoActiveTargets_thenReturnsEmpty() {
            // given
            given(watchTargetRepository.findByActiveTrue()).willReturn(Flux.empty());
            
            // when & then
            StepVerifier.create(watchTargetAdapter.findActiveTargets())
                    .verifyComplete();
            
            verify(watchTargetRepository).findByActiveTrue();
        }
    }
    
    @Nested
    @DisplayName("높은 우선순위 감시 대상 조회")
    class FindHighPriorityTargetsTests {
        
        @Test
        @DisplayName("설정된 우선순위 범위의 감시 대상을 조회한다")
        void whenFindHighPriorityTargets_thenReturnsHighPriorityTargets() {
            // given
            WatchlistProperties.Priority priorityConfig = new WatchlistProperties.Priority(1, 3);
            given(properties.priority()).willReturn(priorityConfig);
            
            WatchTarget target1 = createWatchTargetWithPriority("005930", "삼성전자", 1);
            WatchTarget target2 = createWatchTargetWithPriority("035720", "카카오", 2);
            WatchTarget target3 = createWatchTargetWithPriority("051910", "LG화학", 3);
            
            given(watchTargetRepository.findByPriorityBetweenAndActiveTrueOrderByPriorityAsc(1, 3))
                    .willReturn(Flux.just(target1, target2, target3));
            
            // when & then
            StepVerifier.create(watchTargetAdapter.findHighPriorityTargets())
                    .expectNext(target1)
                    .expectNext(target2)
                    .expectNext(target3)
                    .verifyComplete();
            
            verify(watchTargetRepository).findByPriorityBetweenAndActiveTrueOrderByPriorityAsc(1, 3);
        }
        
        @Test
        @DisplayName("높은 우선순위 감시 대상이 없으면 빈 결과를 반환한다")
        void whenNoHighPriorityTargets_thenReturnsEmpty() {
            // given
            WatchlistProperties.Priority priorityConfig = new WatchlistProperties.Priority(1, 3);
            given(properties.priority()).willReturn(priorityConfig);
            
            given(watchTargetRepository.findByPriorityBetweenAndActiveTrueOrderByPriorityAsc(1, 3))
                    .willReturn(Flux.empty());
            
            // when & then
            StepVerifier.create(watchTargetAdapter.findHighPriorityTargets())
                    .verifyComplete();
            
            verify(watchTargetRepository).findByPriorityBetweenAndActiveTrueOrderByPriorityAsc(1, 3);
        }
        
        @Test
        @DisplayName("Properties 설정값을 사용하여 우선순위 범위를 동적으로 조회한다")
        void givenDifferentPriorityRange_whenFindHighPriorityTargets_thenUsesConfiguredRange() {
            // given - 다른 우선순위 범위 설정
            WatchlistProperties.Priority priorityConfig = new WatchlistProperties.Priority(2, 5);
            given(properties.priority()).willReturn(priorityConfig);
            
            WatchTargetRepositoryAdapter customAdapter = new WatchTargetRepositoryAdapter(watchTargetRepository, properties);
            
            WatchTarget target = createWatchTargetWithPriority("005930", "삼성전자", 3);
            given(watchTargetRepository.findByPriorityBetweenAndActiveTrueOrderByPriorityAsc(2, 5))
                    .willReturn(Flux.just(target));
            
            // when & then
            StepVerifier.create(customAdapter.findHighPriorityTargets())
                    .expectNext(target)
                    .verifyComplete();
            
            verify(watchTargetRepository).findByPriorityBetweenAndActiveTrueOrderByPriorityAsc(2, 5);
        }
    }
    
    @Nested
    @DisplayName("카테고리별 활성 감시 대상 조회")
    class FindActiveTargetsByCategoryTests {
        
        @Test
        @DisplayName("지정된 카테고리의 활성 감시 대상을 조회한다")
        void givenCategory_whenFindActiveTargetsByCategory_thenReturnsCategoryTargets() {
            // given
            WatchTarget coreTarget1 = createWatchTargetWithCategory("005930", "삼성전자", WatchCategory.CORE);
            WatchTarget coreTarget2 = createWatchTargetWithCategory("035720", "카카오", WatchCategory.CORE);
            
            given(watchTargetRepository.findByCategoryAndActiveTrueOrderByPriorityAsc(WatchCategory.CORE))
                    .willReturn(Flux.just(coreTarget1, coreTarget2));
            
            // when & then
            StepVerifier.create(watchTargetAdapter.findActiveTargetsByCategory(WatchCategory.CORE))
                    .expectNext(coreTarget1)
                    .expectNext(coreTarget2)
                    .verifyComplete();
            
            verify(watchTargetRepository).findByCategoryAndActiveTrueOrderByPriorityAsc(WatchCategory.CORE);
        }
        
        @Test
        @DisplayName("해당 카테고리의 감시 대상이 없으면 빈 결과를 반환한다")
        void givenCategoryWithNoTargets_whenFindActiveTargetsByCategory_thenReturnsEmpty() {
            // given
            given(watchTargetRepository.findByCategoryAndActiveTrueOrderByPriorityAsc(WatchCategory.MOMENTUM))
                    .willReturn(Flux.empty());
            
            // when & then
            StepVerifier.create(watchTargetAdapter.findActiveTargetsByCategory(WatchCategory.MOMENTUM))
                    .verifyComplete();
            
            verify(watchTargetRepository).findByCategoryAndActiveTrueOrderByPriorityAsc(WatchCategory.MOMENTUM);
        }
    }
    
    @Nested
    @DisplayName("종목코드로 감시 대상 조회")
    class FindBySymbolTests {
        
        @Test
        @DisplayName("종목코드로 감시 대상을 성공적으로 조회한다")
        void givenSymbol_whenFindBySymbol_thenReturnsTarget() {
            // given
            String symbol = "005930";
            WatchTarget target = createWatchTarget(symbol, "삼성전자", true);
            
            given(watchTargetRepository.findBySymbol(symbol)).willReturn(Mono.just(target));
            
            // when & then
            StepVerifier.create(watchTargetAdapter.findBySymbol(symbol))
                    .expectNext(target)
                    .verifyComplete();
            
            verify(watchTargetRepository).findBySymbol(symbol);
        }
        
        @Test
        @DisplayName("존재하지 않는 종목코드는 빈 결과를 반환한다")
        void givenNonExistentSymbol_whenFindBySymbol_thenReturnsEmpty() {
            // given
            String symbol = "999999";
            given(watchTargetRepository.findBySymbol(symbol)).willReturn(Mono.empty());
            
            // when & then
            StepVerifier.create(watchTargetAdapter.findBySymbol(symbol))
                    .verifyComplete();
            
            verify(watchTargetRepository).findBySymbol(symbol);
        }
    }
    
    // 헬퍼 메서드들
    private WatchTarget createWatchTarget(String symbol, String name, boolean active) {
        LocalDateTime now = LocalDateTime.now();
        return new WatchTarget(
                "id_" + symbol, symbol, name, WatchCategory.CORE, null,
                1, 30, active, "테스트용",
                now, now
        );
    }
    
    private WatchTarget createWatchTargetWithPriority(String symbol, String name, int priority) {
        LocalDateTime now = LocalDateTime.now();
        return new WatchTarget(
                "id_" + symbol, symbol, name, WatchCategory.CORE, null,
                priority, 30, true, "테스트용",
                now, now
        );
    }
    
    private WatchTarget createWatchTargetWithCategory(String symbol, String name, WatchCategory category) {
        LocalDateTime now = LocalDateTime.now();
        return new WatchTarget(
                "id_" + symbol, symbol, name, category, null,
                1, 30, true, "테스트용",
                now, now
        );
    }
}
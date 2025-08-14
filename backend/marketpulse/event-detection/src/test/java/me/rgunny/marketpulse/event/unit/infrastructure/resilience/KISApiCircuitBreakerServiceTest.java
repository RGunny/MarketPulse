package me.rgunny.marketpulse.event.unit.infrastructure.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import me.rgunny.marketpulse.event.marketdata.application.port.out.shared.MarketDataCachePort;
import me.rgunny.marketpulse.event.marketdata.domain.model.StockPrice;
import me.rgunny.marketpulse.event.marketdata.infrastructure.resilience.KISApiCircuitBreakerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * KIS API 서킷브레이커 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("KISApiCircuitBreakerService 단위 테스트")
class KISApiCircuitBreakerServiceTest {
    
    @Mock
    private MarketDataCachePort cachePort;
    
    private KISApiCircuitBreakerService circuitBreakerService;
    private CircuitBreakerRegistry registry;
    
    private static final String TEST_SYMBOL = "005930";
    private static final StockPrice TEST_PRICE = StockPrice.createWithTTL(
            TEST_SYMBOL,
            "삼성전자",
            new BigDecimal("70000"),
            new BigDecimal("69000"),
            new BigDecimal("71000"),
            new BigDecimal("69500"),
            new BigDecimal("70000"),
            1000000L,
            new BigDecimal("70100"),
            new BigDecimal("69900")
    );
    
    @BeforeEach
    void setUp() {
        // 테스트용 서킷브레이커 설정 (빠른 테스트를 위해 작은 값 사용)
        var config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slowCallRateThreshold(50)
                .slowCallDurationThreshold(Duration.ofMillis(100))
                .permittedNumberOfCallsInHalfOpenState(2)
                .slidingWindowSize(4)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .minimumNumberOfCalls(2)
                .waitDurationInOpenState(Duration.ofMillis(500))
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();
        
        registry = CircuitBreakerRegistry.of(config);
        circuitBreakerService = new KISApiCircuitBreakerService(registry, cachePort);
        circuitBreakerService.init();
    }
    
    @Test
    @DisplayName("정상 API 호출 시 서킷브레이커 CLOSED 상태 유지")
    void given_successfulApiCall_when_execute_then_circuitBreakerRemainsClosed() {
        // given
        Mono<StockPrice> successfulApiCall = Mono.just(TEST_PRICE);
        
        // when & then
        StepVerifier.create(
                circuitBreakerService.executeGetCurrentPrice(TEST_SYMBOL, successfulApiCall)
        )
        .assertNext(price -> {
            assertThat(price).isEqualTo(TEST_PRICE);
            assertThat(circuitBreakerService.getCircuitBreakerState()).isEqualTo("CLOSED");
        })
        .verifyComplete();
    }
    
    @Test
    @DisplayName("API 호출 실패 시 캐시 Fallback 실행")
    void given_apiCallFails_when_cacheExists_then_returnCachedData() {
        // given
        Mono<StockPrice> failingApiCall = Mono.error(new TimeoutException("API Timeout"));
        given(cachePort.getStockPrice(TEST_SYMBOL)).willReturn(Mono.just(TEST_PRICE));
        
        // when & then
        StepVerifier.create(
                circuitBreakerService.executeGetCurrentPrice(TEST_SYMBOL, failingApiCall)
        )
        .assertNext(price -> {
            assertThat(price).isEqualTo(TEST_PRICE);
        })
        .verifyComplete();
        
        verify(cachePort, times(1)).getStockPrice(TEST_SYMBOL);
    }
    
    @Test
    @DisplayName("API 호출 실패 및 캐시 미스 시 에러 전파")
    void given_apiCallFailsAndCacheMiss_when_execute_then_propagateError() {
        // given
        Mono<StockPrice> failingApiCall = Mono.error(new TimeoutException("API Timeout"));
        given(cachePort.getStockPrice(TEST_SYMBOL)).willReturn(Mono.empty());
        
        // when & then
        StepVerifier.create(
                circuitBreakerService.executeGetCurrentPrice(TEST_SYMBOL, failingApiCall)
        )
        .expectError(KISApiCircuitBreakerService.KISApiCircuitBreakerException.class)
        .verify();
    }
    
    @Test
    @DisplayName("연속 실패로 서킷브레이커 OPEN 전환")
    void given_consecutiveFailures_when_thresholdExceeded_then_circuitBreakerOpens() {
        // given
        Mono<StockPrice> failingApiCall = Mono.error(new TimeoutException("API Timeout"));
        given(cachePort.getStockPrice(anyString())).willReturn(Mono.empty());
        
        // when - 연속 실패 발생 (KIS API는 20개 윈도우에 5개 최소 호출 필요)
        for (int i = 0; i < 10; i++) {
            StepVerifier.create(
                    circuitBreakerService.executeGetCurrentPrice(TEST_SYMBOL, failingApiCall)
            )
            .expectError()
            .verify();
        }
        
        // then - 서킷브레이커 OPEN 확인
        String state = circuitBreakerService.getCircuitBreakerState();
        assertThat(state).isIn("OPEN", "HALF_OPEN"); // 설정에 따라 OPEN 또는 HALF_OPEN 가능
    }
    
    @Test
    @DisplayName("서킷브레이커 OPEN 상태에서 캐시 데이터 반환")
    void given_circuitBreakerOpen_when_cacheExists_then_returnCachedData() {
        // given - 서킷브레이커를 강제로 OPEN 상태로 만들기
        circuitBreakerService.forceOpenCircuitBreaker().block();
        assertThat(circuitBreakerService.getCircuitBreakerState()).isEqualTo("FORCED_OPEN");
        
        // 캐시 데이터 설정
        given(cachePort.getStockPrice(TEST_SYMBOL)).willReturn(Mono.just(TEST_PRICE));
        
        // when & then - OPEN 상태에서도 캐시에서 데이터 반환
        StepVerifier.create(
                circuitBreakerService.executeGetCurrentPrice(TEST_SYMBOL, Mono.just(TEST_PRICE))
        )
        .assertNext(price -> {
            assertThat(price).isEqualTo(TEST_PRICE);
        })
        .verifyComplete();
        
        // 캐시 호출 검증
        verify(cachePort, times(1)).getStockPrice(TEST_SYMBOL);
    }
    
    @Test
    @DisplayName("validateConnection 실패 시 false 반환")
    void given_connectionFails_when_validateConnection_then_returnFalse() {
        // given
        Mono<Boolean> failingConnection = Mono.error(new RuntimeException("Connection failed"));
        
        // when & then
        StepVerifier.create(
                circuitBreakerService.executeValidateConnection(failingConnection)
        )
        .assertNext(result -> assertThat(result).isFalse())
        .verifyComplete();
    }
    
    @Test
    @DisplayName("서킷브레이커 메트릭 조회")
    void given_someCallsExecuted_when_getMetrics_then_returnCorrectMetrics() {
        // given - 일부 성공, 일부 실패 호출 실행
        Mono<StockPrice> successCall = Mono.just(TEST_PRICE);
        Mono<StockPrice> failCall = Mono.error(new RuntimeException("Failed"));
        given(cachePort.getStockPrice(anyString())).willReturn(Mono.just(TEST_PRICE));
        
        // 성공 호출
        circuitBreakerService.executeGetCurrentPrice(TEST_SYMBOL, successCall).block();
        
        // 실패 호출 (캐시에서 복구)
        circuitBreakerService.executeGetCurrentPrice(TEST_SYMBOL, failCall).block();
        
        // when
        var metrics = circuitBreakerService.getMetrics();
        
        // then
        assertThat(metrics).isNotNull();
        assertThat(metrics.name()).isEqualTo("kis-api");
        assertThat(metrics.state()).isEqualTo("CLOSED");
        assertThat(metrics.totalCalls()).isGreaterThanOrEqualTo(2);
    }
    
    @Test
    @DisplayName("서킷브레이커 수동 리셋")
    void given_circuitBreakerOpen_when_reset_then_circuitBreakerClosed() {
        // given - 서킷브레이커를 OPEN 상태로 만들기
        circuitBreakerService.forceOpenCircuitBreaker().block();
        assertThat(circuitBreakerService.getCircuitBreakerState()).isEqualTo("FORCED_OPEN");
        
        // when
        circuitBreakerService.resetCircuitBreaker().block();
        
        // then
        assertThat(circuitBreakerService.getCircuitBreakerState()).isEqualTo("CLOSED");
    }
    
    @Test
    @DisplayName("토큰 발급 실패 시 에러 전파 (Fallback 없음)")
    void given_tokenApiCallFails_when_execute_then_propagateError() {
        // given
        Mono<String> failingTokenCall = Mono.error(new RuntimeException("Token API Failed"));
        
        // when & then
        StepVerifier.create(
                circuitBreakerService.executeGetAccessToken(failingTokenCall)
        )
        .expectError(RuntimeException.class)
        .verify();
    }
}
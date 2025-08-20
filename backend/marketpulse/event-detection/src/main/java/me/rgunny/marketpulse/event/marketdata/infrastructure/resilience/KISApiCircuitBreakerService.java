package me.rgunny.marketpulse.event.marketdata.infrastructure.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.common.resilience.service.FallbackStrategy;
import me.rgunny.marketpulse.common.resilience.service.ReactiveCircuitBreakerService;
import me.rgunny.marketpulse.event.marketdata.application.port.out.shared.MarketDataCachePort;
import me.rgunny.marketpulse.event.marketdata.domain.model.StockPrice;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * KIS API 전용 서킷브레이커 서비스
 * 
 * KIS API 특성에 맞춘 서킷브레이커 설정:
 * - 빠른 장애 감지 (30% 실패율)
 * - 짧은 복구 시도 주기 (30초)
 * - 캐시 기반 Fallback 우선
 */
@Slf4j
@Service
public class KISApiCircuitBreakerService {
    
    private static final String CIRCUIT_BREAKER_NAME = "kis-api";
    
    private final CircuitBreakerRegistry registry;
    private final MarketDataCachePort cachePort;
    private ReactiveCircuitBreakerService circuitBreakerService;
    
    public KISApiCircuitBreakerService(
            CircuitBreakerRegistry registry,
            MarketDataCachePort cachePort) {
        this.registry = registry;
        this.cachePort = cachePort;
    }
    
    @PostConstruct
    public void init() {
        // KIS API 전용 서킷브레이커 설정
        var config = CircuitBreakerConfig.custom()
                .failureRateThreshold(40)                           // 40% 실패율에서 Open
                .slowCallRateThreshold(60)                          // 60% 느린 호출에서 Open
                .slowCallDurationThreshold(Duration.ofSeconds(8))   // 8초 이상이면 느린 호출
                .permittedNumberOfCallsInHalfOpenState(10)          // Half-Open에서 10개 테스트
                .slidingWindowSize(50)                              // 최근 50개 호출 기준
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .minimumNumberOfCalls(10)                           // 최소 10개 호출 후 평가
                .waitDurationInOpenState(Duration.ofSeconds(20))    // Open 상태 20초 유지
                .automaticTransitionFromOpenToHalfOpenEnabled(true) // 자동 복구 시도
                .recordExceptions(                                  // 기록할 예외 지정
                        io.netty.channel.ConnectTimeoutException.class,
                        java.net.SocketTimeoutException.class,
                        java.net.SocketException.class,
                        java.io.IOException.class,
                        java.util.concurrent.TimeoutException.class,
                        org.springframework.web.reactive.function.client.WebClientRequestException.class,
                        org.springframework.web.reactive.function.client.WebClientResponseException.class
                )
                .ignoreExceptions(
                        IllegalArgumentException.class,
                        IllegalStateException.class
                )
                .build();
        
        // 레지스트리에 등록
        var circuitBreaker = registry.circuitBreaker(CIRCUIT_BREAKER_NAME, config);
        
        // 기본 Fallback 전략 설정 (캐시 우선)
        var defaultFallback = FallbackStrategy.propagateError();
        
        this.circuitBreakerService = new ReactiveCircuitBreakerService(
                circuitBreaker, 
                defaultFallback
        );
        
        // 이벤트 리스너 등록
        registerEventListeners(circuitBreaker);
        
        log.info("KIS API Circuit Breaker initialized with name: {}", CIRCUIT_BREAKER_NAME);
    }
    
    /**
     * 주가 조회에 서킷브레이커 적용
     */
    public Mono<StockPrice> executeGetCurrentPrice(
            String symbol, 
            Mono<StockPrice> apiCall) {
        
        // 캐시 기반 Fallback 전략
        FallbackStrategy<StockPrice> cacheOnlyFallback = (throwable, context) -> {
            log.warn("Circuit breaker OPEN for symbol: {}, using cache fallback", symbol);
            return cachePort.getStockPrice(symbol)
                    .doOnNext(price -> log.info("Cache hit for symbol: {} during circuit breaker OPEN", symbol))
                    .switchIfEmpty(Mono.defer(() -> {
                        log.error("Cache miss for symbol: {} during circuit breaker OPEN", symbol);
                        return Mono.error(new KISApiCircuitBreakerException(
                                "Circuit breaker OPEN and no cached data available for: " + symbol,
                                throwable
                        ));
                    }));
        };
        
        return circuitBreakerService.executeMono(apiCall, cacheOnlyFallback);
    }
    
    /**
     * 토큰 발급에 서킷브레이커 적용
     * 토큰은 캐시 불가능하므로 에러 전파
     */
    public Mono<String> executeGetAccessToken(Mono<String> apiCall) {
        return circuitBreakerService.executeMono(apiCall);
    }
    
    /**
     * 연결 검증에 서킷브레이커 적용
     */
    public Mono<Boolean> executeValidateConnection(Mono<Boolean> apiCall) {
        // 연결 실패 시 false 반환
        FallbackStrategy<Boolean> connectionFallback = (throwable, context) -> {
            log.warn("Circuit breaker OPEN for connection validation, returning false");
            return Mono.just(false);
        };
        
        return circuitBreakerService.executeMono(apiCall, connectionFallback);
    }
    
    /**
     * Flux 타입에 서킷브레이커 적용 (Fallback 전략 포함)
     */
    public <T> reactor.core.publisher.Flux<T> executeWithFallback(
            String contextName,
            java.util.function.Supplier<reactor.core.publisher.Flux<T>> supplier,
            java.util.function.Function<Throwable, reactor.core.publisher.Flux<T>> fallback) {
        
        FallbackStrategy<T> fallbackStrategy = (throwable, context) -> {
            log.warn("Circuit breaker OPEN for {}, executing fallback", contextName);
            return fallback.apply(throwable).next();
        };
        
        return circuitBreakerService.executeFlux(supplier.get())
                .onErrorResume(fallback);
    }
    
    /**
     * Mono 타입에 서킷브레이커 적용 (Fallback 전략 포함)
     */
    public <T> Mono<T> executeMonoWithFallback(
            String contextName,
            java.util.function.Supplier<Mono<T>> supplier,
            java.util.function.Function<Throwable, Mono<T>> fallback) {
        
        FallbackStrategy<T> fallbackStrategy = (throwable, context) -> {
            log.warn("Circuit breaker OPEN for {}, executing fallback", contextName);
            return fallback.apply(throwable);
        };
        
        return circuitBreakerService.executeMono(supplier.get(), fallbackStrategy);
    }
    
    /**
     * 서킷브레이커 상태 조회
     */
    public String getCircuitBreakerState() {
        return circuitBreakerService.getState().name();
    }
    
    /**
     * 서킷브레이커 메트릭 조회
     */
    public CircuitBreakerMetrics getMetrics() {
        var metrics = circuitBreakerService.getMetrics();
        return new CircuitBreakerMetrics(
                metrics.name(),
                getCircuitBreakerState(),
                metrics.failureRate(),
                metrics.slowCallRate(),
                metrics.bufferedCalls(),
                metrics.failedCalls(),
                metrics.slowCalls(),
                metrics.successfulCalls()
        );
    }
    
    /**
     * 서킷브레이커 수동 리셋 (관리자 기능)
     */
    public Mono<Void> resetCircuitBreaker() {
        log.warn("Manual circuit breaker reset requested for: {}", CIRCUIT_BREAKER_NAME);
        return circuitBreakerService.reset();
    }
    
    /**
     * 서킷브레이커 강제 Open (장애 대응)
     */
    public Mono<Void> forceOpenCircuitBreaker() {
        log.warn("Force opening circuit breaker for: {}", CIRCUIT_BREAKER_NAME);
        return circuitBreakerService.forceOpen();
    }
    
    /**
     * 이벤트 리스너 등록
     */
    private void registerEventListeners(CircuitBreaker circuitBreaker) {
        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> {
                    var transition = event.getStateTransition();
                    log.warn("🔌 Circuit Breaker State Change: {} -> {} for {}",
                            transition.getFromState(),
                            transition.getToState(),
                            CIRCUIT_BREAKER_NAME);
                    
                    // Slack 알림 등 추가 액션 가능
                    if (transition.getToState() == CircuitBreaker.State.OPEN) {
                        log.error("ALERT: KIS API Circuit Breaker is now OPEN! API calls will be blocked.");
                    } else if (transition.getToState() == CircuitBreaker.State.CLOSED) {
                        log.info("KIS API Circuit Breaker recovered and is now CLOSED.");
                    }
                })
                .onFailureRateExceeded(event -> 
                    log.error("Failure rate exceeded: {}% for {}",
                            event.getFailureRate(), CIRCUIT_BREAKER_NAME))
                .onSlowCallRateExceeded(event ->
                    log.warn("Slow call rate exceeded: {}% for {}",
                            event.getSlowCallRate(), CIRCUIT_BREAKER_NAME))
                .onCallNotPermitted(event ->
                    log.debug("Call not permitted for {}", CIRCUIT_BREAKER_NAME))
                .onError(event ->
                    log.debug("Error recorded: {} for {}",
                            event.getThrowable().getMessage(), CIRCUIT_BREAKER_NAME))
                .onSuccess(event ->
                    log.trace("Successful call for {}", CIRCUIT_BREAKER_NAME));
    }
    
    /**
     * 서킷브레이커 메트릭 DTO
     */
    public record CircuitBreakerMetrics(
            String name,
            String state,
            float failureRate,
            float slowCallRate,
            int totalCalls,
            int failedCalls,
            int slowCalls,
            int successfulCalls
    ) {
        public String getHealthStatus() {
            if ("OPEN".equals(state) || "FORCED_OPEN".equals(state)) {
                return "UNHEALTHY";
            } else if ("HALF_OPEN".equals(state)) {
                return "RECOVERING";
            } else {
                return "HEALTHY";
            }
        }
    }
    
    /**
     * KIS API 서킷브레이커 전용 예외
     */
    public static class KISApiCircuitBreakerException extends RuntimeException {
        public KISApiCircuitBreakerException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
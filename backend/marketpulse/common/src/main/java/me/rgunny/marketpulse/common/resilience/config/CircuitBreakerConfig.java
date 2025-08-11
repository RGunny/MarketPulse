package me.rgunny.marketpulse.common.resilience.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 서킷브레이커 자동 설정
 * 
 * Micrometer 메트릭 자동 등록
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(CircuitBreakerProperties.class)
@ConditionalOnProperty(
    prefix = "resilience4j.circuitbreaker",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class CircuitBreakerConfig {
    
    /**
     * 서킷브레이커 레지스트리 빈 등록
     * 
     * 모든 서킷브레이커 인스턴스 중앙 관리
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry(CircuitBreakerProperties properties) {
        var config = io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                .failureRateThreshold(properties.failureRateThreshold())
                .slowCallRateThreshold(properties.slowCallRateThreshold())
                .slowCallDurationThreshold(properties.slowCallDurationThreshold())
                .permittedNumberOfCallsInHalfOpenState(properties.permittedNumberOfCallsInHalfOpenState())
                .slidingWindowSize(properties.slidingWindowSize())
                .slidingWindowType(getSlidingWindowType(properties.slidingWindowType()))
                .minimumNumberOfCalls(properties.minimumNumberOfCalls())
                .waitDurationInOpenState(properties.waitDurationInOpenState())
                .automaticTransitionFromOpenToHalfOpenEnabled(properties.automaticTransitionFromOpenToHalfOpenEnabled())
                .build();
        
        var registry = CircuitBreakerRegistry.of(config);
        
        // 전역 이벤트 리스너 등록
        registry.getEventPublisher()
                .onEntryAdded(event -> {
                    var circuitBreaker = event.getAddedEntry();
                    log.info("Circuit breaker {} has been registered", circuitBreaker.getName());
                    
                    // 각 서킷브레이커별 이벤트 리스너
                    circuitBreaker.getEventPublisher()
                            .onStateTransition(stateEvent -> 
                                log.info("Circuit breaker {} state transition: {} -> {}", 
                                    circuitBreaker.getName(),
                                    stateEvent.getStateTransition().getFromState(),
                                    stateEvent.getStateTransition().getToState()))
                            .onFailureRateExceeded(rateEvent ->
                                log.warn("Circuit breaker {} failure rate exceeded: {}%",
                                    circuitBreaker.getName(),
                                    rateEvent.getFailureRate()))
                            .onSlowCallRateExceeded(slowEvent ->
                                log.warn("Circuit breaker {} slow call rate exceeded: {}%",
                                    circuitBreaker.getName(),
                                    slowEvent.getSlowCallRate()));
                });
        
        log.info("CircuitBreakerRegistry initialized with default config: {}", config);
        return registry;
    }
    
    /**
     * Micrometer 메트릭 자동 등록
     * 
     * Prometheus, Grafana 등과 연동 가능
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "resilience4j.circuitbreaker",
        name = "registerHealthIndicator",
        havingValue = "true",
        matchIfMissing = true
    )
    public TaggedCircuitBreakerMetrics circuitBreakerMetrics(
            CircuitBreakerRegistry registry,
            MeterRegistry meterRegistry) {
        
        var metrics = TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(registry);
        metrics.bindTo(meterRegistry);
        
        log.info("Circuit breaker metrics registered to MeterRegistry");
        return metrics;
    }
    
    private io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType 
            getSlidingWindowType(String type) {
        return "TIME_BASED".equalsIgnoreCase(type) 
                ? io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.TIME_BASED
                : io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.COUNT_BASED;
    }
}
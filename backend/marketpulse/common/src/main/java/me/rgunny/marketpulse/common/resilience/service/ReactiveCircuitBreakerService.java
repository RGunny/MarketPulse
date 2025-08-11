package me.rgunny.marketpulse.common.resilience.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.common.resilience.model.CircuitBreakerEvent;
import me.rgunny.marketpulse.common.resilience.model.CircuitBreakerState;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Instant;
import java.util.function.Function;

/**
 * Reactive 서킷브레이커 서비스
 * 
 * WebFlux 환경에서 논블로킹 서킷브레이커 제공
 * Resilience4j와 Project Reactor 통합
 */
@Slf4j
public class ReactiveCircuitBreakerService {
    
    private final CircuitBreaker circuitBreaker;
    private final Sinks.Many<CircuitBreakerEvent> eventSink;
    private final FallbackStrategy<?> defaultFallbackStrategy;
    
    public ReactiveCircuitBreakerService(
            CircuitBreaker circuitBreaker,
            FallbackStrategy<?> defaultFallbackStrategy) {
        this.circuitBreaker = circuitBreaker;
        this.defaultFallbackStrategy = defaultFallbackStrategy;
        this.eventSink = Sinks.many().multicast().onBackpressureBuffer();
        
        registerEventListeners();
    }
    
    /**
     * Mono에 서킷브레이커 적용
     */
    public <T> Mono<T> executeMono(Mono<T> mono) {
        return executeMono(mono, null);
    }
    
    /**
     * Mono에 서킷브레이커와 Fallback 적용
     */
    @SuppressWarnings("unchecked")
    public <T> Mono<T> executeMono(Mono<T> mono, FallbackStrategy<T> fallbackStrategy) {
        FallbackStrategy<T> strategy = fallbackStrategy != null ? 
                fallbackStrategy : (FallbackStrategy<T>) defaultFallbackStrategy;
        
        return mono
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .doOnError(error -> publishFailureEvent(error))
                .doOnSuccess(result -> publishSuccessEvent())
                .onErrorResume(throwable -> {
                    publishFallbackEvent(throwable);
                    return strategy.execute(throwable);
                });
    }
    
    /**
     * Flux에 서킷브레이커 적용
     */
    public <T> Flux<T> executeFlux(Flux<T> flux) {
        return executeFlux(flux, null);
    }
    
    /**
     * Flux에 서킷브레이커와 Fallback 적용
     */
    @SuppressWarnings("unchecked")
    public <T> Flux<T> executeFlux(Flux<T> flux, FallbackStrategy<T> fallbackStrategy) {
        FallbackStrategy<T> strategy = fallbackStrategy != null ? 
                fallbackStrategy : (FallbackStrategy<T>) defaultFallbackStrategy;
        
        return flux
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .doOnError(error -> publishFailureEvent(error))
                .doOnComplete(this::publishSuccessEvent)
                .onErrorResume(throwable -> {
                    publishFallbackEvent(throwable);
                    return strategy.execute(throwable).flux();
                });
    }
    
    /**
     * 함수형 변환 적용
     */
    public <T, R> Function<Mono<T>, Mono<R>> decorateMono(
            Function<Mono<T>, Mono<R>> function) {
        return mono -> executeMono(function.apply(mono));
    }
    
    /**
     * 서킷브레이커 상태 조회
     */
    public CircuitBreakerState getState() {
        return mapState(circuitBreaker.getState());
    }
    
    /**
     * 서킷브레이커 메트릭 조회
     */
    public CircuitBreakerMetrics getMetrics() {
        var metrics = circuitBreaker.getMetrics();
        return new CircuitBreakerMetrics(
                circuitBreaker.getName(),
                metrics.getFailureRate(),
                metrics.getSlowCallRate(),
                metrics.getNumberOfBufferedCalls(),
                metrics.getNumberOfFailedCalls(),
                metrics.getNumberOfSlowCalls(),
                metrics.getNumberOfSuccessfulCalls()
        );
    }
    
    /**
     * 서킷브레이커 수동 리셋
     */
    public Mono<Void> reset() {
        return Mono.fromRunnable(() -> {
            circuitBreaker.reset();
            log.info("Circuit breaker {} has been reset", circuitBreaker.getName());
            publishStateTransitionEvent(getState(), CircuitBreakerState.CLOSED, "Manual reset");
        });
    }
    
    /**
     * 서킷브레이커 강제 Open
     */
    public Mono<Void> forceOpen() {
        return Mono.fromRunnable(() -> {
            var previousState = getState();
            circuitBreaker.transitionToForcedOpenState();
            log.warn("Circuit breaker {} has been forced open", circuitBreaker.getName());
            publishStateTransitionEvent(previousState, CircuitBreakerState.FORCED_OPEN, "Manual force open");
        });
    }
    
    /**
     * 이벤트 스트림 구독
     */
    public Flux<CircuitBreakerEvent> getEventStream() {
        return eventSink.asFlux();
    }
    
    /**
     * Resilience4j 이벤트 리스너 등록
     */
    private void registerEventListeners() {
        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> {
                    var from = mapState(event.getStateTransition().getFromState());
                    var to = mapState(event.getStateTransition().getToState());
                    publishStateTransitionEvent(from, to, "Automatic transition");
                })
                .onSuccess(event -> publishSuccessEvent())
                .onError(event -> publishFailureEvent(new RuntimeException(event.getThrowable())))
                .onSlowCallRateExceeded(event -> {
                    log.warn("Slow call rate exceeded for {}: {}%", 
                            circuitBreaker.getName(), event.getSlowCallRate());
                })
                .onFailureRateExceeded(event -> {
                    log.warn("Failure rate exceeded for {}: {}%", 
                            circuitBreaker.getName(), event.getFailureRate());
                });
    }
    
    private void publishStateTransitionEvent(CircuitBreakerState from, CircuitBreakerState to, String reason) {
        eventSink.tryEmitNext(new CircuitBreakerEvent.StateTransitionEvent(
                circuitBreaker.getName(),
                Instant.now(),
                from,
                to,
                reason
        ));
    }
    
    private void publishFailureEvent(Throwable error) {
        eventSink.tryEmitNext(new CircuitBreakerEvent.CallFailedEvent(
                circuitBreaker.getName(),
                Instant.now(),
                "unknown",
                error,
                0L
        ));
    }
    
    private void publishSuccessEvent() {
        eventSink.tryEmitNext(new CircuitBreakerEvent.CallSucceededEvent(
                circuitBreaker.getName(),
                Instant.now(),
                "unknown",
                0L
        ));
    }
    
    private void publishFallbackEvent(Throwable throwable) {
        eventSink.tryEmitNext(new CircuitBreakerEvent.FallbackExecutedEvent(
                circuitBreaker.getName(),
                Instant.now(),
                "unknown",
                "fallback",
                throwable.getMessage()
        ));
    }
    
    private CircuitBreakerState mapState(CircuitBreaker.State state) {
        return switch (state) {
            case CLOSED -> CircuitBreakerState.CLOSED;
            case OPEN -> CircuitBreakerState.OPEN;
            case HALF_OPEN -> CircuitBreakerState.HALF_OPEN;
            case DISABLED -> CircuitBreakerState.DISABLED;
            case FORCED_OPEN -> CircuitBreakerState.FORCED_OPEN;
            case METRICS_ONLY -> CircuitBreakerState.CLOSED; // Metrics only mode는 CLOSED로 매핑
        };
    }
    
    /**
     * 서킷브레이커 메트릭 DTO
     */
    public record CircuitBreakerMetrics(
            String name,
            float failureRate,
            float slowCallRate,
            int bufferedCalls,
            int failedCalls,
            int slowCalls,
            int successfulCalls
    ) {}
}
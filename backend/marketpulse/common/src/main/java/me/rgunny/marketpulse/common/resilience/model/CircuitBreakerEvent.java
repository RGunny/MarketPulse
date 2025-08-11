package me.rgunny.marketpulse.common.resilience.model;

import java.time.Instant;

/**
 * 서킷브레이커 이벤트 모델
 * 
 * 상태 변경 및 중요 이벤트 추적용
 */
public sealed interface CircuitBreakerEvent {
    
    String circuitBreakerName();
    Instant timestamp();
    String eventType();
    
    /**
     * 상태 전환 이벤트
     */
    record StateTransitionEvent(
            String circuitBreakerName,
            Instant timestamp,
            CircuitBreakerState fromState,
            CircuitBreakerState toState,
            String reason
    ) implements CircuitBreakerEvent {
        @Override
        public String eventType() {
            return "STATE_TRANSITION";
        }
    }
    
    /**
     * 호출 실패 이벤트
     */
    record CallFailedEvent(
            String circuitBreakerName,
            Instant timestamp,
            String methodName,
            Throwable exception,
            long duration
    ) implements CircuitBreakerEvent {
        @Override
        public String eventType() {
            return "CALL_FAILED";
        }
    }
    
    /**
     * 호출 성공 이벤트
     */
    record CallSucceededEvent(
            String circuitBreakerName,
            Instant timestamp,
            String methodName,
            long duration
    ) implements CircuitBreakerEvent {
        @Override
        public String eventType() {
            return "CALL_SUCCEEDED";
        }
    }
    
    /**
     * Fallback 실행 이벤트
     */
    record FallbackExecutedEvent(
            String circuitBreakerName,
            Instant timestamp,
            String methodName,
            String fallbackMethod,
            String reason
    ) implements CircuitBreakerEvent {
        @Override
        public String eventType() {
            return "FALLBACK_EXECUTED";
        }
    }
    
    /**
     * 메트릭 업데이트 이벤트
     */
    record MetricsUpdatedEvent(
            String circuitBreakerName,
            Instant timestamp,
            float failureRate,
            float slowCallRate,
            int numberOfBufferedCalls,
            int numberOfFailedCalls,
            int numberOfSlowCalls
    ) implements CircuitBreakerEvent {
        @Override
        public String eventType() {
            return "METRICS_UPDATED";
        }
    }
}
package me.rgunny.marketpulse.common.resilience.model;

/**
 * 서킷브레이커 상태
 */
public enum CircuitBreakerState {
    
    /**
     * 정상 상태 - 모든 요청 통과
     */
    CLOSED("Circuit is closed, all calls are allowed"),
    
    /**
     * 차단 상태 - 모든 요청 차단, Fallback 실행
     */
    OPEN("Circuit is open, all calls are blocked"),
    
    /**
     * 반개방 상태 - 제한된 요청만 허용하여 복구 테스트
     */
    HALF_OPEN("Circuit is half-open, limited calls allowed for testing"),
    
    /**
     * 비활성 상태 - 서킷브레이커 비활성화
     */
    DISABLED("Circuit breaker is disabled"),
    
    /**
     * 강제 개방 - 수동으로 차단
     */
    FORCED_OPEN("Circuit is forced open manually");
    
    private final String description;
    
    CircuitBreakerState(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 요청 허용 여부
     */
    public boolean allowsRequests() {
        return this == CLOSED || this == HALF_OPEN || this == DISABLED;
    }
    
    /**
     * Fallback 실행 필요 여부
     */
    public boolean requiresFallback() {
        return this == OPEN || this == FORCED_OPEN;
    }
}
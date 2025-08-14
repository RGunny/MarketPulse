package me.rgunny.marketpulse.event.marketdata.domain.policy;

import java.time.Duration;

/**
 * 재시도 정책
 */
public record RetryPolicy(
    int maxAttempts,
    Duration initialDelay,
    double backoffMultiplier,
    Duration maxDelay,
    boolean addJitter
) {
    
    /**
     * 기본 재시도 정책
     */
    public static RetryPolicy defaultPolicy() {
        return new RetryPolicy(
            3,
            Duration.ofSeconds(1),
            2.0,
            Duration.ofSeconds(30),
            true
        );
    }
    
    /**
     * 적극적 재시도 정책 (중요 작업용)
     */
    public static RetryPolicy aggressivePolicy() {
        return new RetryPolicy(
            5,
            Duration.ofMillis(500),
            1.5,
            Duration.ofSeconds(10),
            true
        );
    }
    
    /**
     * 보수적 재시도 정책 (부하 민감 작업용)
     */
    public static RetryPolicy conservativePolicy() {
        return new RetryPolicy(
            2,
            Duration.ofSeconds(5),
            3.0,
            Duration.ofMinutes(1),
            false
        );
    }
    
    /**
     * 재시도 지연 시간 계산
     */
    public Duration calculateDelay(int attemptNumber) {
        if (attemptNumber <= 0) {
            return Duration.ZERO;
        }
        
        // 지수 백오프 계산
        double delaySeconds = initialDelay.getSeconds() * 
            Math.pow(backoffMultiplier, attemptNumber - 1);
        
        // 최대 지연 시간 제한
        delaySeconds = Math.min(delaySeconds, maxDelay.getSeconds());
        
        // 지터 추가 (0.8 ~ 1.2 범위)
        if (addJitter) {
            double jitter = 0.8 + (Math.random() * 0.4);
            delaySeconds *= jitter;
        }
        
        return Duration.ofMillis((long)(delaySeconds * 1000));
    }
    
    /**
     * 재시도 가능 여부
     */
    public boolean canRetry(int attemptCount) {
        return attemptCount < maxAttempts;
    }
}
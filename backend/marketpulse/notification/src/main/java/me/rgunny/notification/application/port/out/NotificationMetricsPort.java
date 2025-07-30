package me.rgunny.notification.application.port.out;

import io.micrometer.core.instrument.Timer;
import me.rgunny.notification.domain.model.NotificationChannel;
import me.rgunny.notification.domain.model.NotificationType;

/**
 * 알림 메트릭 수집 포트
 */
public interface NotificationMetricsPort {
    
    /**
     * 타이머 시작
     */
    Timer.Sample startTimer();
    
    /**
     * 성공 메트릭 기록
     */
    void recordSuccess(NotificationType type, NotificationChannel channel);
    
    /**
     * 실패 메트릭 기록
     */
    void recordFailure(NotificationType type, NotificationChannel channel, String errorType);
    
    /**
     * 응답 시간 기록
     */
    void recordResponseTime(Timer.Sample sample, NotificationChannel channel);
}
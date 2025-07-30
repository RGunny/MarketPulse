package me.rgunny.notification.infrastructure.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import me.rgunny.notification.application.port.out.NotificationMetricsPort;
import me.rgunny.notification.domain.model.NotificationChannel;
import me.rgunny.notification.domain.model.NotificationType;
import org.springframework.stereotype.Component;

/**
 * 알림 메트릭 수집기
 * - Spring Boot Actuator 메트릭으로 알림 발송 통계 수집
 * TODO: Prometheus 연동 시 메트릭 라벨링 최적화 필요
 */
@Component
public class NotificationMetrics implements NotificationMetricsPort {
    
    private final Counter successCounter;
    private final Counter failureCounter;
    private final Timer responseTimer;
    private final MeterRegistry meterRegistry;
    
    public NotificationMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.successCounter = Counter.builder("notification.sent.total")
                .tag("status", "success")
                .description("Total successful notifications sent")
                .register(meterRegistry);
        this.failureCounter = Counter.builder("notification.sent.total")
                .tag("status", "failed")
                .description("Total failed notifications")
                .register(meterRegistry);
        this.responseTimer = Timer.builder("notification.response.time")
                .description("Notification sending response time")
                .register(meterRegistry);
    }
    
    /**
     * 성공 메트릭 기록
     */
    public void recordSuccess(NotificationType type, NotificationChannel channel) {
        Counter.builder("notification.sent.total")
                .tags(Tags.of(
                        "status", "success",
                        "type", type.name().toLowerCase(),
                        "channel", channel.name().toLowerCase()
                ))
                .register(meterRegistry)
                .increment();
    }
    
    /**
     * 실패 메트릭 기록
     */
    public void recordFailure(NotificationType type, NotificationChannel channel, String errorType) {
        Counter.builder("notification.sent.total")
                .tags(Tags.of(
                        "status", "failed",
                        "type", type.name().toLowerCase(),
                        "channel", channel.name().toLowerCase(),
                        "error_type", errorType
                ))
                .register(meterRegistry)
                .increment();
    }
    
    /**
     * 응답 시간 측정 시작
     */
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }
    
    /**
     * 응답 시간 측정 완료
     */
    public void recordResponseTime(Timer.Sample sample, NotificationChannel channel) {
        sample.stop(Timer.builder("notification.response.time")
                .tag("channel", channel.name().toLowerCase())
                .register(meterRegistry));
    }
}
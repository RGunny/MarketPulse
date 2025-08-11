package me.rgunny.notification.application.usecase;

import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.common.core.exception.BusinessException;
import me.rgunny.notification.application.port.in.SendNotificationUseCase;
import me.rgunny.notification.application.port.out.NotificationMetricsPort;
import me.rgunny.notification.application.port.out.NotificationSenderPort;
import me.rgunny.notification.domain.error.NotificationErrorCode;
import me.rgunny.notification.domain.event.MarketEvent;
import me.rgunny.notification.domain.event.NotificationAuditEvent;
import me.rgunny.notification.domain.event.PriceAlertEvent;
import me.rgunny.notification.domain.model.Notification;
import me.rgunny.notification.domain.model.NotificationChannel;
import me.rgunny.notification.domain.model.NotificationType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * 알림 서비스
 */
@Service
@Slf4j
public class NotificationService implements SendNotificationUseCase {
    
    private final List<NotificationSenderPort> senders;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationMetricsPort metrics;
    private final String defaultRecipient;
    
    public NotificationService(
            List<NotificationSenderPort> senders,
            ApplicationEventPublisher eventPublisher,
            NotificationMetricsPort metrics,
            @Value("${notification.default.recipient}") String defaultRecipient) {
        this.senders = senders;
        this.eventPublisher = eventPublisher;
        this.metrics = metrics;
        this.defaultRecipient = defaultRecipient;
    }
    
    @Override
    public Mono<Void> sendNotification(MarketEvent event) {
        return Mono.defer(() -> {
            Notification notification = createNotification(event);
            Timer.Sample sample = metrics.startTimer();
            
            return sendToChannel(notification)
                    .doOnSuccess(unused -> handleSuccess(notification, sample))
                    .doOnError(error -> handleError(notification, error, sample));
        });
    }
    
    /**
     * 성공 처리
     */
    private void handleSuccess(Notification notification, Timer.Sample sample) {
        // 메트릭 기록
        metrics.recordSuccess(notification.type(), notification.channel());
        metrics.recordResponseTime(sample, notification.channel());
        
        // 이력 이벤트 발행 (추후 audit-service에서 Spring Events로 수신)
        eventPublisher.publishEvent(NotificationAuditEvent.success(notification));
        
        log.info("Notification sent successfully: eventId={}, type={}, channel={}",
                notification.eventId(), notification.type(), notification.channel());
    }
    
    /**
     * 실패 처리
     */
    private void handleError(Notification notification, Throwable error, Timer.Sample sample) {
        // 메트릭 기록
        String errorType = error.getClass().getSimpleName();
        metrics.recordFailure(notification.type(), notification.channel(), errorType);
        metrics.recordResponseTime(sample, notification.channel());
        
        // 이력 이벤트 발행
        eventPublisher.publishEvent(NotificationAuditEvent.failed(notification, error.getMessage()));
        
        log.error("Notification failed: eventId={}, type={}, channel={}, error={}",
                notification.eventId(), notification.type(), notification.channel(), error.getMessage());
    }
    
    private Notification createNotification(MarketEvent event) {
        return switch (event) {
            case PriceAlertEvent priceEvent -> Notification.create(
                    priceEvent.eventId(),
                    NotificationType.PRICE_ALERT,
                    NotificationChannel.SLACK,
                    defaultRecipient,
                    formatPriceAlertTitle(priceEvent),
                    priceEvent.generateMessage(),
                    Map.of(
                            "symbol", priceEvent.symbol(),
                            "alertType", priceEvent.alertType(),
                            "changeRate", priceEvent.changeRate().toString()
                    )
            );
        };
    }
    
    private String formatPriceAlertTitle(PriceAlertEvent event) {
        return switch (event.alertType()) {
            case "LIMIT_UP" -> "🚀 상한가 도달!";
            case "LIMIT_DOWN" -> "💥 하한가 도달!";
            case "RISE" -> "📈 가격 상승 알림";
            case "FALL" -> "📉 가격 하락 알림";
            default -> "가격 변동 알림";
        };
    }
    
    private Mono<Void> sendToChannel(Notification notification) {
        return Flux.fromIterable(senders)
                .filter(sender -> sender.supports(notification.channel()))
                .next()
                .switchIfEmpty(Mono.error(new BusinessException(NotificationErrorCode.NOTIFICATION_SEND_003)))
                .flatMap(sender -> sender.send(notification));
    }
    
}
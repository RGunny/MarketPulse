package me.rgunny.marketpulse.notification.infrastructure.adapter.out.slack;

import com.slack.api.Slack;
import com.slack.api.webhook.Payload;
import com.slack.api.webhook.WebhookResponse;
import me.rgunny.marketpulse.notification.application.port.out.NotificationSenderPort;
import me.rgunny.marketpulse.notification.domain.error.NotificationErrorCode;
import me.rgunny.marketpulse.notification.domain.model.Notification;
import me.rgunny.marketpulse.notification.domain.model.NotificationChannel;
import me.rgunny.marketpulse.common.core.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


/**
 * Slack 알림 발송 어댑터
 * - Slack Webhook을 통한 메시지 발송
 */
@Component
public class SlackNotificationAdapter implements NotificationSenderPort {
    
    private static final Logger log = LoggerFactory.getLogger(SlackNotificationAdapter.class);
    
    private final Slack slack;
    private final String webhookUrl;
    
    public SlackNotificationAdapter(@Value("${notification.slack.webhook-url}") String webhookUrl) {
        this.slack = Slack.getInstance();
        this.webhookUrl = webhookUrl;
    }
    
    @Override
    public Mono<Void> send(Notification notification) {
        return Mono.fromCallable(() -> {
            Payload payload = buildPayload(notification);
            WebhookResponse response = slack.send(webhookUrl, payload);
            
            if (response.getCode() != 200) {
                throw new BusinessException(NotificationErrorCode.NOTIFICATION_SEND_002);
            }
            
            log.info("Slack notification sent successfully. EventId: {}", notification.eventId());
            return response;
        })
        .doOnError(error -> log.error("Failed to send Slack notification", error))
        .then();
    }
    
    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.SLACK;
    }
    
    private Payload buildPayload(Notification notification) {
        return Payload.builder()
                .text(formatMessage(notification))
                .build();
    }
    
    private String formatMessage(Notification notification) {
        StringBuilder message = new StringBuilder();
        
        // 제목
        message.append("*").append(notification.title()).append("*\n");
        
        // 본문
        message.append(notification.message()).append("\n");
        
        // 메타데이터 (있는 경우)
        if (notification.metadata() != null && !notification.metadata().isEmpty()) {
            message.append("\n```\n");
            notification.metadata().forEach((key, value) -> 
                message.append(key).append(": ").append(value).append("\n")
            );
            message.append("```");
        }
        
        return message.toString();
    }
    
}
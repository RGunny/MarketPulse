package me.rgunny.marketpulse.notification.domain.event;

import me.rgunny.marketpulse.notification.domain.model.Notification;
import me.rgunny.marketpulse.notification.domain.model.NotificationChannel;
import me.rgunny.marketpulse.notification.domain.model.NotificationType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 알림 이력 이벤트
 * - 알림 발송 후 이력 관리를 위한 도메인 이벤트
 * - TODO: audit-service에서 Spring Events로 수신하여 이력 저장
 */
public record NotificationAuditEvent(
        String eventId,
        String notificationId,
        NotificationType type,
        NotificationChannel channel,
        String recipient,
        String title,
        String message,
        String status, // SUCCESS, FAILED
        String errorMessage,
        LocalDateTime timestamp,
        Map<String, Object> metadata
) {
    
    /**
     * 성공 이벤트 생성
     */
    public static NotificationAuditEvent success(Notification notification) {
        return new NotificationAuditEvent(
                notification.eventId(),
                UUID.randomUUID().toString(),
                notification.type(),
                notification.channel(),
                notification.recipient(),
                notification.title(),
                notification.message(),
                "SUCCESS",
                null,
                LocalDateTime.now(),
                notification.metadata()
        );
    }
    
    /**
     * 실패 이벤트 생성
     */
    public static NotificationAuditEvent failed(Notification notification, String error) {
        return new NotificationAuditEvent(
                notification.eventId(),
                UUID.randomUUID().toString(),
                notification.type(),
                notification.channel(),
                notification.recipient(),
                notification.title(),
                notification.message(),
                "FAILED",
                error,
                LocalDateTime.now(),
                notification.metadata()
        );
    }
}
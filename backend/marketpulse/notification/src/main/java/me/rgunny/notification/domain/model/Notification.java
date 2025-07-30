package me.rgunny.notification.domain.model;

import me.rgunny.marketpulse.common.exception.BusinessException;
import me.rgunny.notification.domain.error.NotificationErrorCode;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 알림 도메인 엔티티
 */
public record Notification(
        String id,
        String eventId,
        NotificationType type,
        NotificationChannel channel,
        String recipient,
        String title,
        String message,
        Map<String, Object> metadata,
        NotificationStatus status,
        LocalDateTime createdAt,
        LocalDateTime sentAt,
        String errorMessage
) {
    /**
     * 신규 알림 생성 (팩토리 메서드)
     */
    public static Notification create(
            String eventId,
            NotificationType type,
            NotificationChannel channel,
            String recipient,
            String title,
            String message,
            Map<String, Object> metadata
    ) {
        return new Notification(
                null,
                eventId,
                type,
                channel,
                recipient,
                title,
                message,
                metadata,
                NotificationStatus.PENDING,
                LocalDateTime.now(),
                null,
                null
        );
    }
    
    /**
     * 알림 발송 성공 처리
     */
    public Notification markAsSent() {
        return new Notification(
                id,
                eventId,
                type,
                channel,
                recipient,
                title,
                message,
                metadata,
                NotificationStatus.SENT,
                createdAt,
                LocalDateTime.now(),
                null
        );
    }
    
    /**
     * 알림 발송 실패 처리
     */
    public Notification markAsFailed(String errorMessage) {
        return new Notification(
                id,
                eventId,
                type,
                channel,
                recipient,
                title,
                message,
                metadata,
                NotificationStatus.FAILED,
                createdAt,
                null,
                errorMessage
        );
    }
    
    /**
     * 재시도 가능 여부 확인
     */
    public boolean canRetry() {
        return status == NotificationStatus.FAILED 
                && metadata != null 
                && metadata.containsKey("retryCount")
                && ((Integer) metadata.get("retryCount")) < 3;
    }
    
    public Notification {
        // 필수 필드 검증
        if (eventId == null || eventId.trim().isEmpty()) {
            throw new BusinessException(NotificationErrorCode.NOTIFICATION_VALIDATION_001);
        }
        if (type == null) {
            throw new BusinessException(NotificationErrorCode.NOTIFICATION_VALIDATION_001);
        }
        if (channel == null) {
            throw new BusinessException(NotificationErrorCode.NOTIFICATION_VALIDATION_001);
        }
        if (recipient == null || recipient.trim().isEmpty()) {
            throw new BusinessException(NotificationErrorCode.NOTIFICATION_VALIDATION_002);
        }
        if (title == null || title.trim().isEmpty()) {
            throw new BusinessException(NotificationErrorCode.NOTIFICATION_VALIDATION_001);
        }
        if (message == null || message.trim().isEmpty()) {
            throw new BusinessException(NotificationErrorCode.NOTIFICATION_VALIDATION_001);
        }
        if (status == null) {
            throw new BusinessException(NotificationErrorCode.NOTIFICATION_VALIDATION_001);
        }
        if (createdAt == null) {
            throw new BusinessException(NotificationErrorCode.NOTIFICATION_VALIDATION_001);
        }
        
        // 비즈니스 규칙 검증
        if (title.length() > 100) {
            throw new BusinessException(NotificationErrorCode.NOTIFICATION_VALIDATION_003);
        }
    }
}
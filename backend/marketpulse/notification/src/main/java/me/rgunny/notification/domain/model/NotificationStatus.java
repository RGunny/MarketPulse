package me.rgunny.notification.domain.model;

/**
 * 알림 상태
 */
public enum NotificationStatus {
    /**
     * 대기중
     */
    PENDING,
    
    /**
     * 발송됨
     */
    SENT,
    
    /**
     * 실패
     */
    FAILED,
    
    /**
     * 취소됨
     */
    CANCELLED
}
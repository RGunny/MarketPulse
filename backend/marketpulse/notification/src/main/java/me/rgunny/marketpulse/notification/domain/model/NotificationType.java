package me.rgunny.marketpulse.notification.domain.model;

/**
 * 알림 유형
 */
public enum NotificationType {
    /**
     * 가격 변동 알림
     */
    PRICE_ALERT("가격 변동 알림");
    
    private final String description;
    
    NotificationType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
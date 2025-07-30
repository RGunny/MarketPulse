package me.rgunny.notification.domain.model;

/**
 * 알림 채널
 */
public enum NotificationChannel {

    SLACK("Slack"), EMAIL("Email")
    ;

    private final String displayName;
    
    NotificationChannel(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
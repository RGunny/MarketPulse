package me.rgunny.notification.domain.model;

/**
 * 알림 채널
 */
public enum NotificationChannel {

    SLACK("Slack"), EMAIL("Email")
    ;

    private final String channel;
    
    NotificationChannel(String channel) {
        this.channel = channel;
    }
    
    public String getChannel() {
        return channel;
    }
}
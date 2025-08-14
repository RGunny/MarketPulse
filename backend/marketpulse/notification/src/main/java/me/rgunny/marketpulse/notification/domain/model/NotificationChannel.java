package me.rgunny.marketpulse.notification.domain.model;

import lombok.Getter;

/**
 * 알림 채널
 */
@Getter
public enum NotificationChannel {

    SLACK("Slack"), EMAIL("Email")
    ;

    private final String channel;
    
    NotificationChannel(String channel) {
        this.channel = channel;
    }

}
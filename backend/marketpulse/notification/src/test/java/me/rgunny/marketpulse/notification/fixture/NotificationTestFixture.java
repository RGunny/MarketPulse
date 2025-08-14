package me.rgunny.marketpulse.notification.fixture;

import me.rgunny.marketpulse.notification.domain.event.PriceAlertEvent;
import me.rgunny.marketpulse.notification.domain.model.Notification;
import me.rgunny.marketpulse.notification.domain.model.NotificationChannel;
import me.rgunny.marketpulse.notification.domain.model.NotificationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 알림 테스트 픽스처
 */
public class NotificationTestFixture {
    
    public static PriceAlertEvent createPriceAlertEvent() {
        return new PriceAlertEvent(
                UUID.randomUUID().toString(),
                "005930",
                "삼성전자",
                BigDecimal.valueOf(71500),
                BigDecimal.valueOf(72000),
                BigDecimal.valueOf(0.0070), // 0.7%
                "RISE",
                LocalDateTime.now(),
                Map.of("threshold", "5%")
        );
    }
    
    
    public static Notification createPendingNotification() {
        return Notification.create(
                "event-123",
                NotificationType.PRICE_ALERT,
                NotificationChannel.SLACK,
                "test-channel",
                "가격 상승 알림",
                "삼성전자(005930) 가격이 5% 상승했습니다.",
                Map.of("symbol", "005930")
        );
    }
    
    public static Notification createSentNotification() {
        return createPendingNotification().markAsSent();
    }
    
    public static Notification createFailedNotification() {
        return createPendingNotification().markAsFailed("Slack API Error");
    }
}
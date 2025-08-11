package me.rgunny.notification.unit.domain;

import me.rgunny.notification.domain.model.Notification;
import me.rgunny.notification.domain.model.NotificationChannel;
import me.rgunny.notification.domain.model.NotificationStatus;
import me.rgunny.notification.domain.model.NotificationType;
import me.rgunny.notification.fixture.NotificationTestFixture;
import me.rgunny.marketpulse.common.core.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Notification 도메인 엔티티 단위 테스트
 * - BDD 스타일 테스트
 */
@DisplayName("Notification 도메인 엔티티")
class NotificationTest {
    
    @Test
    @DisplayName("신규 알림 생성 시 PENDING 상태로 생성된다")
    void givenNotificationData_whenCreateNotification_thenStatusIsPending() {
        // given
        String eventId = "event-123";
        NotificationType type = NotificationType.PRICE_ALERT;
        NotificationChannel channel = NotificationChannel.SLACK;
        String recipient = "test-channel";
        String title = "가격 상승 알림";
        String message = "삼성전자 가격이 상승했습니다.";
        Map<String, Object> metadata = Map.of("symbol", "005930");
        
        // when
        Notification notification = Notification.create(
                eventId, type, channel, recipient, title, message, metadata
        );
        
        // then
        assertThat(notification.eventId()).isEqualTo(eventId);
        assertThat(notification.type()).isEqualTo(type);
        assertThat(notification.channel()).isEqualTo(channel);
        assertThat(notification.recipient()).isEqualTo(recipient);
        assertThat(notification.title()).isEqualTo(title);
        assertThat(notification.message()).isEqualTo(message);
        assertThat(notification.metadata()).isEqualTo(metadata);
        assertThat(notification.status()).isEqualTo(NotificationStatus.PENDING);
        assertThat(notification.createdAt()).isNotNull();
        assertThat(notification.sentAt()).isNull();
        assertThat(notification.errorMessage()).isNull();
    }
    
    @Test
    @DisplayName("PENDING 상태 알림을 발송 성공 처리 시 SENT 상태가 된다")
    void givenPendingNotification_whenMarkAsSent_thenStatusIsSent() {
        // given
        Notification pendingNotification = NotificationTestFixture.createPendingNotification();
        
        // when
        Notification sentNotification = pendingNotification.markAsSent();
        
        // then
        assertThat(sentNotification.status()).isEqualTo(NotificationStatus.SENT);
        assertThat(sentNotification.sentAt()).isNotNull();
        assertThat(sentNotification.errorMessage()).isNull();
        // 기존 속성은 유지
        assertThat(sentNotification.eventId()).isEqualTo(pendingNotification.eventId());
        assertThat(sentNotification.title()).isEqualTo(pendingNotification.title());
    }
    
    @Test
    @DisplayName("PENDING 상태 알림을 발송 실패 처리 시 FAILED 상태가 된다")
    void givenPendingNotification_whenMarkAsFailed_thenStatusIsFailed() {
        // given
        Notification pendingNotification = NotificationTestFixture.createPendingNotification();
        String errorMessage = "Slack API connection failed";
        
        // when
        Notification failedNotification = pendingNotification.markAsFailed(errorMessage);
        
        // then
        assertThat(failedNotification.status()).isEqualTo(NotificationStatus.FAILED);
        assertThat(failedNotification.errorMessage()).isEqualTo(errorMessage);
        assertThat(failedNotification.sentAt()).isNull();
        // 기존 속성은 유지
        assertThat(failedNotification.eventId()).isEqualTo(pendingNotification.eventId());
        assertThat(failedNotification.title()).isEqualTo(pendingNotification.title());
    }
    
    @Test
    @DisplayName("재시도 카운트가 3 미만인 FAILED 알림은 재시도 가능하다")
    void givenFailedNotificationWithRetryCount_whenCanRetry_thenReturnsTrue() {
        // given
        Notification notification = Notification.create(
                "event-123",
                NotificationType.PRICE_ALERT,
                NotificationChannel.SLACK,
                "test-channel",
                "테스트 알림",
                "테스트 메시지",
                Map.of("retryCount", 2)
        );
        Notification failedNotification = notification.markAsFailed("API Error");
        
        // when
        boolean canRetry = failedNotification.canRetry();
        
        // then
        assertThat(canRetry).isTrue();
    }
    
    @Test
    @DisplayName("재시도 카운트가 3 이상인 FAILED 알림은 재시도 불가능하다")
    void givenFailedNotificationWithMaxRetryCount_whenCanRetry_thenReturnsFalse() {
        // given
        Notification notification = Notification.create(
                "event-123",
                NotificationType.PRICE_ALERT,
                NotificationChannel.SLACK,
                "test-channel",
                "테스트 알림",
                "테스트 메시지",
                Map.of("retryCount", 3)
        );
        Notification failedNotification = notification.markAsFailed("API Error");
        
        // when
        boolean canRetry = failedNotification.canRetry();
        
        // then
        assertThat(canRetry).isFalse();
    }
    
    @Test
    @DisplayName("필수 필드가 null인 경우 BusinessException이 발생한다")
    void givenNullEventId_whenCreateNotification_thenThrowsBusinessException() {
        // given
        String nullEventId = null;
        
        // when & then
        assertThatThrownBy(() -> Notification.create(
                nullEventId,
                NotificationType.PRICE_ALERT,
                NotificationChannel.SLACK,
                "test-channel",
                "테스트 알림",
                "테스트 메시지",
                Map.of()
        )).isInstanceOf(BusinessException.class);
    }
}
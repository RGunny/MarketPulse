package me.rgunny.marketpulse.notification.unit.domain;

import me.rgunny.marketpulse.notification.domain.error.NotificationErrorCode;
import me.rgunny.marketpulse.notification.domain.model.Notification;
import me.rgunny.marketpulse.notification.domain.model.NotificationChannel;
import me.rgunny.marketpulse.notification.domain.model.NotificationStatus;
import me.rgunny.marketpulse.notification.domain.model.NotificationType;
import me.rgunny.marketpulse.common.core.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Notification 도메인 검증 로직 테스트
 * - BusinessException 검증 테스트
 */
@DisplayName("Notification 도메인 검증")
class NotificationValidationTest {
    
    @Test
    @DisplayName("eventId가 null이면 검증 예외가 발생한다")
    void givenNullEventId_whenCreateNotification_thenThrowsValidationException() {
        // when & then
        assertThatThrownBy(() -> new Notification(
                null, null, NotificationType.PRICE_ALERT, NotificationChannel.SLACK,
                "recipient", "title", "message", Map.of(), 
                NotificationStatus.PENDING, LocalDateTime.now(), null, null
        ))
        .isInstanceOf(BusinessException.class)
        .extracting(e -> ((BusinessException) e).errorCode())
        .isEqualTo(NotificationErrorCode.NOTIFICATION_VALIDATION_001);
    }
    
    @Test
    @DisplayName("eventId가 빈 문자열이면 검증 예외가 발생한다")
    void givenEmptyEventId_whenCreateNotification_thenThrowsValidationException() {
        // when & then
        assertThatThrownBy(() -> new Notification(
                null, "  ", NotificationType.PRICE_ALERT, NotificationChannel.SLACK,
                "recipient", "title", "message", Map.of(), 
                NotificationStatus.PENDING, LocalDateTime.now(), null, null
        ))
        .isInstanceOf(BusinessException.class)
        .extracting(e -> ((BusinessException) e).errorCode())
        .isEqualTo(NotificationErrorCode.NOTIFICATION_VALIDATION_001);
    }
    
    @Test
    @DisplayName("recipient가 null이면 검증 예외가 발생한다")
    void givenNullRecipient_whenCreateNotification_thenThrowsValidationException() {
        // when & then
        assertThatThrownBy(() -> new Notification(
                null, "event-123", NotificationType.PRICE_ALERT, NotificationChannel.SLACK,
                null, "title", "message", Map.of(), 
                NotificationStatus.PENDING, LocalDateTime.now(), null, null
        ))
        .isInstanceOf(BusinessException.class)
        .extracting(e -> ((BusinessException) e).errorCode())
        .isEqualTo(NotificationErrorCode.NOTIFICATION_VALIDATION_002);
    }
    
    @Test
    @DisplayName("title이 100자를 초과하면 검증 예외가 발생한다")
    void givenTooLongTitle_whenCreateNotification_thenThrowsValidationException() {
        // given
        String longTitle = "a".repeat(101); // 101자
        
        // when & then
        assertThatThrownBy(() -> new Notification(
                null, "event-123", NotificationType.PRICE_ALERT, NotificationChannel.SLACK,
                "recipient", longTitle, "message", Map.of(), 
                NotificationStatus.PENDING, LocalDateTime.now(), null, null
        ))
        .isInstanceOf(BusinessException.class)
        .extracting(e -> ((BusinessException) e).errorCode())
        .isEqualTo(NotificationErrorCode.NOTIFICATION_VALIDATION_003);
    }
    
    @Test
    @DisplayName("title이 100자이면 정상 생성된다")
    void givenExact100CharTitle_whenCreateNotification_thenCreatedSuccessfully() {
        // given
        String title100 = "a".repeat(100); // 정확히 100자
        
        // when & then
        assertThatCode(() -> new Notification(
                null, "event-123", NotificationType.PRICE_ALERT, NotificationChannel.SLACK,
                "recipient", title100, "message", Map.of(), 
                NotificationStatus.PENDING, LocalDateTime.now(), null, null
        )).doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("message가 빈 문자열이면 검증 예외가 발생한다")
    void givenEmptyMessage_whenCreateNotification_thenThrowsValidationException() {
        // when & then
        assertThatThrownBy(() -> new Notification(
                null, "event-123", NotificationType.PRICE_ALERT, NotificationChannel.SLACK,
                "recipient", "title", "  ", Map.of(), 
                NotificationStatus.PENDING, LocalDateTime.now(), null, null
        ))
        .isInstanceOf(BusinessException.class)
        .extracting(e -> ((BusinessException) e).errorCode())
        .isEqualTo(NotificationErrorCode.NOTIFICATION_VALIDATION_001);
    }
}
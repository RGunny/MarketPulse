package me.rgunny.event.unit.notification;

import me.rgunny.event.notification.domain.model.NotificationHistory;
import me.rgunny.notification.grpc.NotificationServiceProto.PriceAlertType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("NotificationHistory 도메인 모델 단위 테스트")
class NotificationHistoryTest {
    
    @Test
    @DisplayName("NotificationHistory 생성 - 정상 케이스")
    void createNotificationHistory_Success() {
        // given
        String symbol = "005930";
        String symbolName = "삼성전자";
        PriceAlertType alertType = PriceAlertType.RISE;
        BigDecimal triggerPrice = BigDecimal.valueOf(70000);
        BigDecimal changeRate = BigDecimal.valueOf(5.5);
        String notificationId = "test-notification-id";
        Duration cooldown = Duration.ofMinutes(30);
        
        // when
        NotificationHistory history = NotificationHistory.create(
            symbol, symbolName, alertType, triggerPrice, changeRate, notificationId, cooldown
        );
        
        // then
        assertThat(history).isNotNull();
        assertThat(history.symbol()).isEqualTo(symbol);
        assertThat(history.symbolName()).isEqualTo(symbolName);
        assertThat(history.alertType()).isEqualTo(alertType);
        assertThat(history.triggerPrice()).isEqualTo(triggerPrice);
        assertThat(history.changeRate()).isEqualTo(changeRate);
        assertThat(history.notificationId()).isEqualTo(notificationId);
        assertThat(history.cooldownPeriod()).isEqualTo(cooldown);
        assertThat(history.sentAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(history.id()).contains(symbol).contains(alertType.toString());
    }
    
    @Test
    @DisplayName("NotificationHistory 생성 - 필수값 누락시 예외")
    void createNotificationHistory_RequiredFieldsMissing_ThrowsException() {
        // given & when & then
        assertThatThrownBy(() -> NotificationHistory.create(
            null, "삼성전자", PriceAlertType.RISE, 
            BigDecimal.valueOf(70000), BigDecimal.valueOf(5.5),
            "notification-id", Duration.ofMinutes(30)
        )).isInstanceOf(NullPointerException.class)
          .hasMessage("Symbol must not be null");
        
        assertThatThrownBy(() -> NotificationHistory.create(
            "005930", "삼성전자", null,
            BigDecimal.valueOf(70000), BigDecimal.valueOf(5.5),
            "notification-id", Duration.ofMinutes(30)
        )).isInstanceOf(NullPointerException.class)
          .hasMessage("AlertType must not be null");
    }
    
    @Test
    @DisplayName("쿨다운 체크 - 쿨다운 중")
    void isInCooldown_WhenWithinCooldownPeriod_ReturnsTrue() {
        // given
        LocalDateTime sentAt = LocalDateTime.now().minusMinutes(10);
        NotificationHistory history = new NotificationHistory(
            "notification:history:005930:RISE",
            "005930", "삼성전자", PriceAlertType.RISE,
            BigDecimal.valueOf(70000), BigDecimal.valueOf(5.5),
            sentAt, "notification-id", Duration.ofMinutes(30)
        );
        
        // when
        boolean inCooldown = history.isInCooldown();
        
        // then
        assertThat(inCooldown).isTrue();
    }
    
    @Test
    @DisplayName("쿨다운 체크 - 쿨다운 만료")
    void isInCooldown_WhenCooldownExpired_ReturnsFalse() {
        // given
        LocalDateTime sentAt = LocalDateTime.now().minusMinutes(31);
        NotificationHistory history = new NotificationHistory(
            "notification:history:005930:RISE",
            "005930", "삼성전자", PriceAlertType.RISE,
            BigDecimal.valueOf(70000), BigDecimal.valueOf(5.5),
            sentAt, "notification-id", Duration.ofMinutes(30)
        );
        
        // when
        boolean inCooldown = history.isInCooldown();
        
        // then
        assertThat(inCooldown).isFalse();
    }
    
    @Test
    @DisplayName("남은 쿨다운 시간 계산")
    void getRemainingCooldown_CalculatesCorrectly() {
        // given
        LocalDateTime sentAt = LocalDateTime.now().minusMinutes(20);
        NotificationHistory history = new NotificationHistory(
            "notification:history:005930:RISE",
            "005930", "삼성전자", PriceAlertType.RISE,
            BigDecimal.valueOf(70000), BigDecimal.valueOf(5.5),
            sentAt, "notification-id", Duration.ofMinutes(30)
        );
        
        // when
        Duration remaining = history.getRemainingCooldown();
        
        // then
        assertThat(remaining.toMinutes()).isBetween(9L, 10L);
    }
    
    @Test
    @DisplayName("TTL 계산 - 쿨다운 + 5분 버퍼")
    void calculateTTL_AddsBufferToCooldown() {
        // given
        NotificationHistory history = NotificationHistory.create(
            "005930", "삼성전자", PriceAlertType.RISE,
            BigDecimal.valueOf(70000), BigDecimal.valueOf(5.5),
            "notification-id", Duration.ofMinutes(30)
        );
        
        // when
        Duration ttl = history.calculateTTL();
        
        // then
        assertThat(ttl).isEqualTo(Duration.ofMinutes(35)); // 30분 + 5분 버퍼
    }
    
    @Test
    @DisplayName("동일 알림 판단")
    void isSameAlert_ComparesSymbolAndType() {
        // given
        NotificationHistory history = NotificationHistory.create(
            "005930", "삼성전자", PriceAlertType.RISE,
            BigDecimal.valueOf(70000), BigDecimal.valueOf(5.5),
            "notification-id", Duration.ofMinutes(30)
        );
        
        // when & then
        assertThat(history.isSameAlert("005930", PriceAlertType.RISE)).isTrue();
        assertThat(history.isSameAlert("005930", PriceAlertType.FALL)).isFalse();
        assertThat(history.isSameAlert("000660", PriceAlertType.RISE)).isFalse();
    }
}
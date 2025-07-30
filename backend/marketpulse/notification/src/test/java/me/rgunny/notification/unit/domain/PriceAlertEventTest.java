package me.rgunny.notification.unit.domain;

import me.rgunny.notification.domain.event.PriceAlertEvent;
import me.rgunny.notification.fixture.NotificationTestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * PriceAlertEvent 도메인 이벤트 단위 테스트
 */
@DisplayName("PriceAlertEvent 도메인 이벤트")
class PriceAlertEventTest {
    
    @Test
    @DisplayName("상승률이 양수인 경우 + 기호가 포함된 변동률 문자열을 반환한다")
    void givenPositiveChangeRate_whenGetChangeRateString_thenReturnsWithPlusSign() {
        // given
        PriceAlertEvent event = NotificationTestFixture.createPriceAlertEvent();
        
        // when
        String changeRateString = event.getChangeRateString();
        
        // then
        assertThat(changeRateString).startsWith("+");
        assertThat(changeRateString).endsWith("%");
        assertThat(changeRateString).contains("0.70");
    }
    
    @Test
    @DisplayName("하락률이 음수인 경우 - 기호가 포함된 변동률 문자열을 반환한다")
    void givenNegativeChangeRate_whenGetChangeRateString_thenReturnsWithMinusSign() {
        // given
        PriceAlertEvent event = new PriceAlertEvent(
                "event-123",
                "005930",
                "삼성전자",
                BigDecimal.valueOf(72000),
                BigDecimal.valueOf(71500),
                BigDecimal.valueOf(-0.0069), // -0.69%
                "FALL",
                null,
                null
        );
        
        // when
        String changeRateString = event.getChangeRateString();
        
        // then
        assertThat(changeRateString).startsWith("-");
        assertThat(changeRateString).endsWith("%");
        assertThat(changeRateString).contains("0.69");
    }
    
    @Test
    @DisplayName("상승 알림 이벤트는 적절한 메시지를 생성한다")
    void givenRiseAlertEvent_whenGenerateMessage_thenReturnsRiseMessage() {
        // given
        PriceAlertEvent event = NotificationTestFixture.createPriceAlertEvent();
        
        // when
        String message = event.generateMessage();
        
        // then
        assertThat(message).contains("삼성전자(005930)");
        assertThat(message).contains("📈 가격 상승");
        assertThat(message).contains("71,500원");
        assertThat(message).contains("72,000원");
        assertThat(message).contains("+0.70%");
    }
    
    @Test
    @DisplayName("상한가 알림 이벤트는 상한가 메시지를 생성한다")
    void givenLimitUpEvent_whenGenerateMessage_thenReturnsLimitUpMessage() {
        // given
        PriceAlertEvent event = new PriceAlertEvent(
                "event-123",
                "005930",
                "삼성전자",
                BigDecimal.valueOf(70000),
                BigDecimal.valueOf(91000),
                BigDecimal.valueOf(0.30), // 30%
                "LIMIT_UP",
                null,
                null
        );
        
        // when
        String message = event.generateMessage();
        
        // then
        assertThat(message).contains("🚀 상한가");
        assertThat(message).contains("삼성전자(005930)");
        assertThat(message).contains("70,000원");
        assertThat(message).contains("91,000원");
    }
    
    @Test
    @DisplayName("하한가 알림 이벤트는 하한가 메시지를 생성한다")
    void givenLimitDownEvent_whenGenerateMessage_thenReturnsLimitDownMessage() {
        // given
        PriceAlertEvent event = new PriceAlertEvent(
                "event-123",
                "005930",
                "삼성전자",
                BigDecimal.valueOf(70000),
                BigDecimal.valueOf(49000),
                BigDecimal.valueOf(-0.30), // -30%
                "LIMIT_DOWN",
                null,
                null
        );
        
        // when
        String message = event.generateMessage();
        
        // then
        assertThat(message).contains("💥 하한가");
        assertThat(message).contains("삼성전자(005930)");
        assertThat(message).contains("70,000원");
        assertThat(message).contains("49,000원");
    }
}
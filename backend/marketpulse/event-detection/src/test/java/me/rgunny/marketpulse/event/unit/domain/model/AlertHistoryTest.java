package me.rgunny.marketpulse.event.unit.domain.model;

import me.rgunny.marketpulse.event.marketdata.domain.model.AlertHistory;
import me.rgunny.marketpulse.event.marketdata.domain.model.AlertType;
import me.rgunny.marketpulse.event.support.TestClockFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * AlertHistory 도메인 엔티티 단위 테스트
 */
@DisplayName("AlertHistory 도메인 엔티티 테스트")
class AlertHistoryTest {
    
    // TestClockFactory를 사용한 재사용 가능한 테스트 시간
    private final Clock FIXED_CLOCK = TestClockFactory.marketMiddle();
    private final Instant FIXED_INSTANT = Instant.now(FIXED_CLOCK);
    
    @Nested
    @DisplayName("팩토리 메서드 테스트")
    class CreateTests {
        
        @Test
        @DisplayName("유효한 파라미터로 알림 이력을 생성한다")
        void given_validParameters_when_create_then_returnsAlertHistory() {
            // given
            String symbol = "005930";
            AlertType alertType = AlertType.PRICE_RISE;
            int cooldownMinutes = 30;
            
            // when
            AlertHistory history = AlertHistory.create(symbol, alertType, cooldownMinutes, FIXED_CLOCK);
            
            // then
            assertThat(history).isNotNull();
            assertThat(history.symbol()).isEqualTo(symbol);
            assertThat(history.alertType()).isEqualTo(alertType);
            assertThat(history.id()).isEqualTo("005930:PRICE_RISE");
            assertThat(history.alertedAt()).isEqualTo(FIXED_INSTANT);
            assertThat(history.cooldownUntil()).isEqualTo(FIXED_INSTANT.plusSeconds(1800)); // 30분
        }
        
        @Test
        @DisplayName("null 심볼로 생성 시 예외가 발생한다")
        void given_nullSymbol_when_create_then_throwsException() {
            // given
            String symbol = null;
            AlertType alertType = AlertType.PRICE_FALL;
            int cooldownMinutes = 30;
            
            // when & then
            assertThatThrownBy(() -> AlertHistory.create(symbol, alertType, cooldownMinutes, FIXED_CLOCK))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Symbol must not be null");
        }
        
        @Test
        @DisplayName("잘못된 심볼 형식으로 생성 시 예외가 발생한다")
        void given_invalidSymbol_when_create_then_throwsException() {
            // given - 특수문자 포함
            String symbol = "005930:KS";
            AlertType alertType = AlertType.PRICE_RISE;
            int cooldownMinutes = 30;
            
            // when & then
            assertThatThrownBy(() -> AlertHistory.create(symbol, alertType, cooldownMinutes, FIXED_CLOCK))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid symbol format");
        }
        
        @Test
        @DisplayName("쿨다운이 최소값 미만으로 생성 시 예외가 발생한다")
        void given_tooShortCooldown_when_create_then_throwsException() {
            // given
            String symbol = "005930";
            AlertType alertType = AlertType.LIMIT_UP;
            int cooldownMinutes = 0; // 최소 1분
            
            // when & then
            assertThatThrownBy(() -> AlertHistory.create(symbol, alertType, cooldownMinutes, FIXED_CLOCK))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cooldown must be between");
        }
        
        @Test
        @DisplayName("쿨다운이 최대값 초과로 생성 시 예외가 발생한다")
        void given_tooLongCooldown_when_create_then_throwsException() {
            // given
            String symbol = "005930";
            AlertType alertType = AlertType.LIMIT_DOWN;
            int cooldownMinutes = 2881; // 최대 2880분 (48시간)
            
            // when & then
            assertThatThrownBy(() -> AlertHistory.create(symbol, alertType, cooldownMinutes, FIXED_CLOCK))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("48 hours");
        }
    }
    
    @Nested
    @DisplayName("쿨다운 테스트")
    class CooldownTests {
    
        @Test
        @DisplayName("쿨다운 중인지 정확히 판단한다")
        void given_alertHistory_when_checkCooldown_then_returnsCorrectStatus() {
            // given - 쿨다운 중인 이력
            AlertHistory activeHistory = new AlertHistory(
                    "005930:RISE",
                    "005930",
                    AlertType.PRICE_RISE,
                    FIXED_INSTANT.minusSeconds(300),
                    FIXED_INSTANT.plusSeconds(300)  // 5분 후까지 쿨다운
            );
            
            // given - 쿨다운이 끝난 이력
            AlertHistory expiredHistory = new AlertHistory(
                    "005930:FALL",
                    "005930",
                    AlertType.PRICE_FALL,
                    FIXED_INSTANT.minusSeconds(3600),
                    FIXED_INSTANT.minusSeconds(1800)  // 30분 전에 끝남
            );
            
            // when & then
            assertThat(activeHistory.isInCooldown(FIXED_CLOCK)).isTrue();
            assertThat(expiredHistory.isInCooldown(FIXED_CLOCK)).isFalse();
        }
    
        @Test
        @DisplayName("남은 쿨다운 시간을 Duration으로 정확히 계산한다")
        void given_alertHistory_when_getRemainingCooldown_then_returnsCorrectDuration() {
            // given
            int futureSeconds = 600;
            AlertHistory history = new AlertHistory(
                    "005930:LIMIT_UP",
                    "005930",
                    AlertType.LIMIT_UP,
                    FIXED_INSTANT,
                    FIXED_INSTANT.plusSeconds(futureSeconds)
            );
            
            // when
            Duration remaining = history.getRemainingCooldown(FIXED_CLOCK);
            
            // then
            assertThat(remaining).isEqualTo(Duration.ofSeconds(futureSeconds));
            assertThat(remaining.toMinutes()).isEqualTo(10);
        }
    
        @Test
        @DisplayName("쿨다운이 끝난 경우 Duration.ZERO를 반환한다")
        void given_expiredHistory_when_getRemainingCooldown_then_returnsZero() {
            // given
            AlertHistory expiredHistory = new AlertHistory(
                    "005930:LIMIT_DOWN",
                    "005930",
                    AlertType.LIMIT_DOWN,
                    FIXED_INSTANT.minusSeconds(3600),
                    FIXED_INSTANT.minusSeconds(1800)
            );
            
            // when
            Duration remaining = expiredHistory.getRemainingCooldown(FIXED_CLOCK);
            
            // then
            assertThat(remaining).isEqualTo(Duration.ZERO);
            assertThat(expiredHistory.getRemainingCooldownSeconds(FIXED_CLOCK)).isEqualTo(0);
        }
    }
    
    @Test
    @DisplayName("같은 종목과 타입을 정확히 매칭한다")
    void given_alertHistory_when_matches_then_returnsCorrectResult() {
        // given
        AlertHistory history = AlertHistory.create("005930", AlertType.PRICE_RISE, 30, FIXED_CLOCK);
        
        // when & then - 일치하는 경우
        assertThat(history.matches("005930", AlertType.PRICE_RISE)).isTrue();
        
        // when & then - 종목이 다른 경우
        assertThat(history.matches("000660", AlertType.PRICE_RISE)).isFalse();
        
        // when & then - 타입이 다른 경우
        assertThat(history.matches("005930", AlertType.PRICE_FALL)).isFalse();
        
        // when & then - 둘 다 다른 경우
        assertThat(history.matches("000660", AlertType.PRICE_FALL)).isFalse();
    }
    
    @Nested
    @DisplayName("Compact Constructor 테스트")
    class CompactConstructorTests {
        
        @Test
        @DisplayName("null 필드로 생성 시 예외가 발생한다")
        void given_nullFields_when_construct_then_throwsException() {
            // when & then - id null
            assertThatThrownBy(() -> new AlertHistory(
                    null, "005930", AlertType.PRICE_RISE, 
                    FIXED_INSTANT, FIXED_INSTANT.plusSeconds(1800)))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("ID must not be null");
            
            // when & then - symbol null
            assertThatThrownBy(() -> new AlertHistory(
                    "005930:RISE", null, AlertType.PRICE_RISE, 
                    FIXED_INSTANT, FIXED_INSTANT.plusSeconds(1800)))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Symbol must not be null");
        }
    }
    
    @Test
    @DisplayName("AlertType enum의 설명을 반환한다")
    void given_alertType_when_getDescription_then_returnsCorrectDescription() {
        // given & when & then
        assertThat(AlertType.PRICE_RISE.getDescription()).isEqualTo("급등");
        assertThat(AlertType.PRICE_FALL.getDescription()).isEqualTo("급락");
        assertThat(AlertType.LIMIT_UP.getDescription()).isEqualTo("상한가");
        assertThat(AlertType.LIMIT_DOWN.getDescription()).isEqualTo("하한가");
        assertThat(AlertType.VOLUME_SURGE.getDescription()).isEqualTo("거래량 급증");
        assertThat(AlertType.NEWS_ALERT.getDescription()).isEqualTo("뉴스 알림");
    }
}
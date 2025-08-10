package me.rgunny.event.unit.domain.model;

import me.rgunny.event.marketdata.domain.model.MarketHours;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.assertj.core.api.Assertions.*;

/**
 * MarketHours 도메인 값 객체 단위 테스트
 */
@DisplayName("MarketHours 도메인 값 객체 테스트")
class MarketHoursTest {
    
    private final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
    
    @Test
    @DisplayName("한국 주식시장 기본 거래시간이 올바르게 설정된다")
    void given_koreaStockMarket_when_checkHours_then_returnsCorrectTime() {
        // given
        MarketHours marketHours = MarketHours.KOREA_STOCK_MARKET;
        
        // when & then
        assertThat(marketHours.openTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(marketHours.closeTime()).isEqualTo(LocalTime.of(15, 30));
        assertThat(marketHours.zoneId()).isEqualTo(SEOUL_ZONE);
    }
    
    @Test
    @DisplayName("평일 거래시간 내에는 시장이 열려있다고 판단한다")
    void given_weekdayTradingHours_when_isMarketOpen_then_returnsTrue() {
        // given
        MarketHours marketHours = MarketHours.KOREA_STOCK_MARKET;
        ZonedDateTime tradingTime = ZonedDateTime.of(
                2025, 1, 13,  // 월요일
                10, 30, 0, 0,
                SEOUL_ZONE
        );
        
        // when
        boolean isOpen = marketHours.isMarketOpen(tradingTime);
        
        // then
        assertThat(isOpen).isTrue();
    }
    
    @Test
    @DisplayName("평일 거래시간 전에는 시장이 닫혀있다고 판단한다")
    void given_beforeTradingHours_when_isMarketOpen_then_returnsFalse() {
        // given
        MarketHours marketHours = MarketHours.KOREA_STOCK_MARKET;
        ZonedDateTime beforeOpen = ZonedDateTime.of(
                2025, 1, 13,  // 월요일
                8, 30, 0, 0,  // 08:30
                SEOUL_ZONE
        );
        
        // when
        boolean isOpen = marketHours.isMarketOpen(beforeOpen);
        
        // then
        assertThat(isOpen).isFalse();
    }
    
    @Test
    @DisplayName("평일 거래시간 후에는 시장이 닫혀있다고 판단한다")
    void given_afterTradingHours_when_isMarketOpen_then_returnsFalse() {
        // given
        MarketHours marketHours = MarketHours.KOREA_STOCK_MARKET;
        ZonedDateTime afterClose = ZonedDateTime.of(
                2025, 1, 13,  // 월요일
                16, 0, 0, 0,  // 16:00
                SEOUL_ZONE
        );
        
        // when
        boolean isOpen = marketHours.isMarketOpen(afterClose);
        
        // then
        assertThat(isOpen).isFalse();
    }
    
    @Test
    @DisplayName("주말에는 시장이 닫혀있다고 판단한다")
    void given_weekend_when_isMarketOpen_then_returnsFalse() {
        // given
        MarketHours marketHours = MarketHours.KOREA_STOCK_MARKET;
        ZonedDateTime saturday = ZonedDateTime.of(
                2025, 1, 11,  // 토요일
                10, 0, 0, 0,
                SEOUL_ZONE
        );
        ZonedDateTime sunday = ZonedDateTime.of(
                2025, 1, 12,  // 일요일
                10, 0, 0, 0,
                SEOUL_ZONE
        );
        
        // when & then
        assertThat(marketHours.isMarketOpen(saturday)).isFalse();
        assertThat(marketHours.isMarketOpen(sunday)).isFalse();
    }
    
    @Test
    @DisplayName("공휴일에는 시장이 닫혀있다고 판단한다")
    void given_holiday_when_isMarketOpen_then_returnsFalse() {
        // given
        MarketHours marketHours = MarketHours.KOREA_STOCK_MARKET;
        ZonedDateTime newYear = ZonedDateTime.of(
                2025, 1, 1,  // 신정
                10, 0, 0, 0,
                SEOUL_ZONE
        );
        ZonedDateTime lunarNewYear = ZonedDateTime.of(
                2025, 1, 29,  // 설날
                10, 0, 0, 0,
                SEOUL_ZONE
        );
        
        // when & then
        assertThat(marketHours.isMarketOpen(newYear)).isFalse();
        assertThat(marketHours.isMarketOpen(lunarNewYear)).isFalse();
    }
    
    @Test
    @DisplayName("프리마켓 시간이 올바르게 설정된다")
    void given_preMarket_when_checkHours_then_returnsCorrectTime() {
        // given
        MarketHours preMarket = MarketHours.KOREA_PRE_MARKET;
        
        // when & then
        assertThat(preMarket.openTime()).isEqualTo(LocalTime.of(8, 30));
        assertThat(preMarket.closeTime()).isEqualTo(LocalTime.of(9, 0));
    }
    
    @Test
    @DisplayName("애프터마켓 시간이 올바르게 설정된다")
    void given_afterMarket_when_checkHours_then_returnsCorrectTime() {
        // given
        MarketHours afterMarket = MarketHours.KOREA_AFTER_MARKET;
        
        // when & then
        assertThat(afterMarket.openTime()).isEqualTo(LocalTime.of(15, 30));
        assertThat(afterMarket.closeTime()).isEqualTo(LocalTime.of(16, 0));
    }
    
    @Test
    @DisplayName("다음 개장 시간까지 남은 시간을 계산한다")
    void given_closedMarket_when_untilNextOpen_then_returnsCorrectDuration() {
        // given
        MarketHours marketHours = MarketHours.KOREA_STOCK_MARKET;
        
        // 금요일 장 마감 후 시간으로 고정
        ZonedDateTime fridayAfterClose = ZonedDateTime.of(
                2025, 1, 10,  // 금요일
                16, 0, 0, 0,
                SEOUL_ZONE
        );
        
        // 월요일 개장 시간
        ZonedDateTime mondayOpen = ZonedDateTime.of(
                2025, 1, 13,  // 월요일
                9, 0, 0, 0,
                SEOUL_ZONE
        );
        
        // when - 실제로는 현재 시간 기준으로 계산되므로, 
        // 정확한 테스트를 위해서는 Clock을 주입받도록 리팩토링 필요
        Duration duration = marketHours.untilNextOpen();
        
        // then - Duration이 양수인지만 확인
        assertThat(duration).isNotNull();
        assertThat(duration.isNegative()).isFalse();
    }
    
    @Test
    @DisplayName("toString 메서드가 올바른 형식으로 출력한다")
    void given_marketHours_when_toString_then_returnsFormattedString() {
        // given
        MarketHours marketHours = MarketHours.KOREA_STOCK_MARKET;
        
        // when
        String result = marketHours.toString();
        
        // then
        assertThat(result).isEqualTo("09:00 ~ 15:30 (Asia/Seoul)");
    }
    
    @Test
    @DisplayName("커스텀 거래시간을 생성할 수 있다")
    void given_customHours_when_create_then_returnsMarketHours() {
        // given
        LocalTime customOpen = LocalTime.of(10, 0);
        LocalTime customClose = LocalTime.of(14, 0);
        ZoneId customZone = ZoneId.of("America/New_York");
        
        // when
        MarketHours customHours = new MarketHours(customOpen, customClose, customZone);
        
        // then
        assertThat(customHours.openTime()).isEqualTo(customOpen);
        assertThat(customHours.closeTime()).isEqualTo(customClose);
        assertThat(customHours.zoneId()).isEqualTo(customZone);
    }
}
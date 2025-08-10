package me.rgunny.event.unit.infrastructure.config;

import me.rgunny.event.marketdata.domain.model.MarketHours;
import me.rgunny.event.marketdata.infrastructure.config.TimeConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TimeConfig 단위 테스트
 * 
 * 간소화된 TimeConfig의 핵심 기능만 테스트
 */
@DisplayName("TimeConfig 단위 테스트")
class TimeConfigTest {
    
    private final TimeConfig timeConfig = new TimeConfig();
    
    @Test
    @DisplayName("시스템 Clock 생성 - 지정된 타임존 사용")
    void systemClock_createsSystemClockWithTimezone() {
        // given
        String timezone = "Asia/Seoul";
        
        // when
        Clock clock = timeConfig.systemClock(timezone);
        
        // then
        assertThat(clock).isNotNull();
        assertThat(clock.getZone()).isEqualTo(ZoneId.of(timezone));
        
        // 시스템 시계이므로 현재 시간과 유사해야 함
        Instant now = Instant.now();
        Instant clockTime = Instant.now(clock);
        assertThat(clockTime).isBetween(
            now.minusSeconds(1), 
            now.plusSeconds(1)
        );
    }
    
    @Test
    @DisplayName("다른 타임존으로 Clock 생성")
    void systemClock_createsClockWithDifferentTimezone() {
        // given
        String timezone = "America/New_York";
        
        // when
        Clock clock = timeConfig.systemClock(timezone);
        
        // then
        assertThat(clock).isNotNull();
        assertThat(clock.getZone()).isEqualTo(ZoneId.of(timezone));
    }
    
    @Test
    @DisplayName("MarketHours - 한국 주식시장 설정")
    void marketHours_returnsKoreaStockMarket() {
        // given
        String market = "KOREA_STOCK_MARKET";
        
        // when
        MarketHours marketHours = timeConfig.marketHours(market);
        
        // then
        assertThat(marketHours).isEqualTo(MarketHours.KOREA_STOCK_MARKET);
    }
    
    @Test
    @DisplayName("MarketHours - 알 수 없는 시장은 기본값 반환")
    void marketHours_returnsDefaultForUnknownMarket() {
        // given
        String unknownMarket = "UNKNOWN_MARKET";
        
        // when
        MarketHours marketHours = timeConfig.marketHours(unknownMarket);
        
        // then
        assertThat(marketHours).isEqualTo(MarketHours.KOREA_STOCK_MARKET);
    }
}
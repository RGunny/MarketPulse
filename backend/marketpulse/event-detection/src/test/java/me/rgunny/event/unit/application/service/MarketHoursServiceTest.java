package me.rgunny.event.unit.application.service;

import me.rgunny.event.marketdata.application.port.in.MarketHoursUseCase;
import me.rgunny.event.marketdata.application.service.MarketHoursService;
import me.rgunny.event.marketdata.domain.model.MarketHours;
import me.rgunny.event.support.TestClockFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MarketHoursService 단위 테스트")
class MarketHoursServiceTest {
    
    private MarketHoursService marketHoursService;
    
    @Nested
    @DisplayName("장중 시간 테스트")
    class MarketOpenTests {
        
        @BeforeEach
        void setUp() {
            // TestClockFactory 사용 - 월요일 장중 시간
            Clock fixedClock = TestClockFactory.marketMiddle();
            MarketHours marketHours = MarketHours.KOREA_STOCK_MARKET;
            marketHoursService = new MarketHoursService(marketHours, fixedClock);
        }
        
        @Test
        @DisplayName("장중에는 시장이 열려있다고 응답한다")
        void given_marketOpen_when_isMarketOpen_then_returnsTrue() {
            // when
            boolean isOpen = marketHoursService.isMarketOpen();
            
            // then
            assertThat(isOpen).isTrue();
        }
        
        @Test
        @DisplayName("장중 상태에서 올바른 MarketStatus를 반환한다")
        void given_marketOpen_when_getMarketStatus_then_returnsOpenStatus() {
            // when
            MarketHoursUseCase.MarketStatus status = marketHoursService.getMarketStatus();
            
            // then
            assertThat(status.isOpen()).isTrue();
            assertThat(status.status()).isEqualTo("OPEN");
            assertThat(status.description()).isEqualTo("시장 거래 중");
            assertThat(status.timeUntilNextChange()).isNotNull();
            assertThat(status.timeUntilNextChange().toHours()).isEqualTo(3); // 12:00에서 15:30까지 3시간 30분
        }
    }
    
    @Nested
    @DisplayName("장 마감 시간 테스트")
    class MarketCloseTests {
        
        @BeforeEach
        void setUp() {
            // TestClockFactory 사용 - 장 마감 후 시간
            Clock fixedClock = TestClockFactory.afterMarket();
            MarketHours marketHours = MarketHours.KOREA_STOCK_MARKET;
            marketHoursService = new MarketHoursService(marketHours, fixedClock);
        }
        
        @Test
        @DisplayName("장 마감 후에는 시장이 닫혀있다고 응답한다")
        void given_afterMarketClose_when_isMarketOpen_then_returnsFalse() {
            // when
            boolean isOpen = marketHoursService.isMarketOpen();
            
            // then
            assertThat(isOpen).isFalse();
        }
        
        @Test
        @DisplayName("장 마감 후 CLOSED 상태를 반환한다")
        void given_afterMarketClose_when_getMarketStatus_then_returnsClosedStatus() {
            // when
            MarketHoursUseCase.MarketStatus status = marketHoursService.getMarketStatus();
            
            // then
            assertThat(status.isOpen()).isFalse();
            assertThat(status.status()).isEqualTo("CLOSED");
            assertThat(status.description()).isEqualTo("시장 마감");
            assertThat(status.timeUntilNextChange()).isNotNull();
            assertThat(status.timeUntilNextChange().toHours()).isEqualTo(17); // 다음날 09:00까지
        }
    }
    
    @Nested
    @DisplayName("경계값 테스트")
    class BoundaryTests {
        
        @Test
        @DisplayName("개장 직전(08:59:59)에는 시장이 닫혀있다")
        void given_oneSecondBeforeOpen_when_isMarketOpen_then_returnsFalse() {
            // given - 개장 1초 전
            Clock fixedClock = TestClockFactory.fixed(
                TestClockFactory.MARKET_OPEN_TIME.minusSeconds(1)
            );
            MarketHours marketHours = MarketHours.KOREA_STOCK_MARKET;
            marketHoursService = new MarketHoursService(marketHours, fixedClock);
            
            // when
            boolean isOpen = marketHoursService.isMarketOpen();
            
            // then
            assertThat(isOpen).isFalse();
        }
        
        @Test
        @DisplayName("개장 시간 정각(09:00:00)에는 시장이 열려있다")
        void given_exactOpenTime_when_isMarketOpen_then_returnsTrue() {
            // given - 개장 시간 정각
            Clock fixedClock = TestClockFactory.marketOpen();
            MarketHours marketHours = MarketHours.KOREA_STOCK_MARKET;
            marketHoursService = new MarketHoursService(marketHours, fixedClock);
            
            // when
            boolean isOpen = marketHoursService.isMarketOpen();
            
            // then
            assertThat(isOpen).isTrue();
        }
        
        @Test
        @DisplayName("마감 시간 정각(15:30:00)에는 시장이 열려있다")
        void given_exactCloseTime_when_isMarketOpen_then_returnsTrue() {
            // given - 마감 시간 정각
            Clock fixedClock = TestClockFactory.marketClose();
            MarketHours marketHours = MarketHours.KOREA_STOCK_MARKET;
            marketHoursService = new MarketHoursService(marketHours, fixedClock);
            
            // when
            boolean isOpen = marketHoursService.isMarketOpen();
            
            // then
            assertThat(isOpen).isTrue();
        }
        
        @Test
        @DisplayName("마감 직후(15:30:01)에는 시장이 닫혀있다")
        void given_oneSecondAfterClose_when_isMarketOpen_then_returnsFalse() {
            // given - 마감 1초 후
            Clock fixedClock = TestClockFactory.fixed(
                TestClockFactory.MARKET_CLOSE_TIME.plusSeconds(1)
            );
            MarketHours marketHours = MarketHours.KOREA_STOCK_MARKET;
            marketHoursService = new MarketHoursService(marketHours, fixedClock);
            
            // when
            boolean isOpen = marketHoursService.isMarketOpen();
            
            // then
            assertThat(isOpen).isFalse();
        }
    }
    
    @Nested
    @DisplayName("주말 테스트")
    class WeekendTests {
        
        @BeforeEach
        void setUp() {
            // TestClockFactory 사용 - 토요일
            Clock fixedClock = TestClockFactory.weekend();
            MarketHours marketHours = MarketHours.KOREA_STOCK_MARKET;
            marketHoursService = new MarketHoursService(marketHours, fixedClock);
        }
        
        @Test
        @DisplayName("주말에는 시장이 닫혀있다")
        void given_weekend_when_isMarketOpen_then_returnsFalse() {
            // when
            boolean isOpen = marketHoursService.isMarketOpen();
            
            // then
            assertThat(isOpen).isFalse();
        }
        
        @Test
        @DisplayName("주말에는 WEEKEND 상태를 반환한다")
        void given_weekend_when_getMarketStatus_then_returnsWeekendStatus() {
            // when
            MarketHoursUseCase.MarketStatus status = marketHoursService.getMarketStatus();
            
            // then
            assertThat(status.isOpen()).isFalse();
            assertThat(status.status()).isEqualTo("WEEKEND");
            assertThat(status.description()).isEqualTo("주말 휴장");
            assertThat(status.timeUntilNextChange()).isNotNull();
            assertThat(status.timeUntilNextChange().toDays()).isEqualTo(1); // 월요일까지
        }
    }
    
    @Nested
    @DisplayName("공휴일 테스트")
    class HolidayTests {
        
        @BeforeEach
        void setUp() {
            // TestClockFactory 사용 - 공휴일 (신정)
            Clock fixedClock = TestClockFactory.holiday();
            MarketHours marketHours = MarketHours.KOREA_STOCK_MARKET;
            marketHoursService = new MarketHoursService(marketHours, fixedClock);
        }
        
        @Test
        @DisplayName("공휴일에는 시장이 닫혀있다")
        void given_holiday_when_isMarketOpen_then_returnsFalse() {
            // when
            boolean isOpen = marketHoursService.isMarketOpen();
            
            // then
            assertThat(isOpen).isFalse();
        }
        
        @Test
        @DisplayName("공휴일에는 HOLIDAY 상태를 반환한다")
        void given_holiday_when_getMarketStatus_then_returnsHolidayStatus() {
            // when
            MarketHoursUseCase.MarketStatus status = marketHoursService.getMarketStatus();
            
            // then
            assertThat(status.isOpen()).isFalse();
            assertThat(status.status()).isEqualTo("HOLIDAY");
            assertThat(status.description()).isEqualTo("공휴일 휴장");
            assertThat(status.timeUntilNextChange()).isNotNull();
        }
    }
    
    @Nested
    @DisplayName("타임존 변환 테스트")
    class TimezoneTests {
        
        @Test
        @DisplayName("다른 타임존의 시간도 정확히 처리한다")
        void given_differentTimezone_when_isMarketOpen_then_correctlyNormalizes() {
            // given - UTC 01:00 = KST 10:00 (장중)
            Clock utcClock = Clock.fixed(
                Instant.parse("2025-01-13T01:00:00Z"),
                ZoneId.of("UTC")
            );
            MarketHours marketHours = MarketHours.KOREA_STOCK_MARKET;
            marketHoursService = new MarketHoursService(marketHours, utcClock);
            
            // when
            boolean isOpen = marketHoursService.isMarketOpen();
            
            // then
            assertThat(isOpen).isTrue(); // KST 10:00이므로 장중
        }
        
        @Test
        @DisplayName("특정 시간 조회 시 타임존이 정규화된다")
        void given_specificTimeWithDifferentZone_when_isMarketOpen_then_normalizes() {
            // given
            Clock systemClock = Clock.systemDefaultZone();
            MarketHours marketHours = MarketHours.KOREA_STOCK_MARKET;
            marketHoursService = new MarketHoursService(marketHours, systemClock);
            
            // when - 뉴욕 시간으로 조회 (EST 19:00 = KST 09:00 다음날)
            ZonedDateTime nyTime = ZonedDateTime.of(
                2025, 1, 12, 19, 0, 0, 0,
                ZoneId.of("America/New_York")
            );
            boolean isOpen = marketHoursService.isMarketOpen(nyTime);
            
            // then
            assertThat(isOpen).isTrue(); // KST 09:00 월요일 개장
        }
    }
}
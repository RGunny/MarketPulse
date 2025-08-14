package me.rgunny.marketpulse.event.support;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 테스트용 Clock 팩토리
 * 
 * 테스트에서 자주 사용되는 시간대를 미리 정의하여 일관성 있는 테스트 작성 지원
 */
public class TestClockFactory {
    
    // 기본 타임존
    public static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
    
    // 테스트용 고정 시간들
    public static final LocalDateTime MARKET_OPEN_TIME = LocalDateTime.of(2025, 1, 13, 9, 0, 0);      // 월요일 장 시작
    public static final LocalDateTime MARKET_CLOSE_TIME = LocalDateTime.of(2025, 1, 13, 15, 30, 0);   // 월요일 장 마감
    public static final LocalDateTime MARKET_MIDDLE_TIME = LocalDateTime.of(2025, 1, 13, 12, 0, 0);   // 월요일 장중
    public static final LocalDateTime BEFORE_MARKET_TIME = LocalDateTime.of(2025, 1, 13, 8, 30, 0);   // 장 시작 전
    public static final LocalDateTime AFTER_MARKET_TIME = LocalDateTime.of(2025, 1, 13, 16, 0, 0);    // 장 마감 후
    public static final LocalDateTime WEEKEND_TIME = LocalDateTime.of(2025, 1, 11, 10, 0, 0);         // 토요일
    public static final LocalDateTime HOLIDAY_TIME = LocalDateTime.of(2025, 1, 1, 10, 0, 0);          // 신정
    
    // 장 마감 임박 시간들
    public static final LocalDateTime ONE_MIN_BEFORE_CLOSE = LocalDateTime.of(2025, 1, 13, 15, 29, 0);
    public static final LocalDateTime FIVE_MIN_BEFORE_CLOSE = LocalDateTime.of(2025, 1, 13, 15, 25, 0);
    
    private TestClockFactory() {
        // 유틸리티 클래스
    }
    
    /**
     * 장 시작 시간 Clock
     */
    public static Clock marketOpen() {
        return fixed(MARKET_OPEN_TIME);
    }
    
    /**
     * 장 마감 시간 Clock
     */
    public static Clock marketClose() {
        return fixed(MARKET_CLOSE_TIME);
    }
    
    /**
     * 장중 시간 Clock
     */
    public static Clock marketMiddle() {
        return fixed(MARKET_MIDDLE_TIME);
    }
    
    /**
     * 장 시작 전 Clock
     */
    public static Clock beforeMarket() {
        return fixed(BEFORE_MARKET_TIME);
    }
    
    /**
     * 장 마감 후 Clock
     */
    public static Clock afterMarket() {
        return fixed(AFTER_MARKET_TIME);
    }
    
    /**
     * 주말 Clock
     */
    public static Clock weekend() {
        return fixed(WEEKEND_TIME);
    }
    
    /**
     * 공휴일 Clock
     */
    public static Clock holiday() {
        return fixed(HOLIDAY_TIME);
    }
    
    /**
     * 장 마감 1분 전 Clock
     */
    public static Clock oneMinuteBeforeClose() {
        return fixed(ONE_MIN_BEFORE_CLOSE);
    }
    
    /**
     * 장 마감 5분 전 Clock
     */
    public static Clock fiveMinutesBeforeClose() {
        return fixed(FIVE_MIN_BEFORE_CLOSE);
    }
    
    /**
     * 특정 LocalDateTime으로 고정된 Clock 생성
     */
    public static Clock fixed(LocalDateTime dateTime) {
        return Clock.fixed(dateTime.atZone(KOREA_ZONE).toInstant(), KOREA_ZONE);
    }
    
    /**
     * 특정 LocalDateTime과 ZoneId로 고정된 Clock 생성
     */
    public static Clock fixed(LocalDateTime dateTime, ZoneId zoneId) {
        return Clock.fixed(dateTime.atZone(zoneId).toInstant(), zoneId);
    }
    
    /**
     * 특정 Instant로 고정된 Clock 생성
     */
    public static Clock fixed(Instant instant) {
        return Clock.fixed(instant, KOREA_ZONE);
    }
    
    /**
     * 특정 Instant와 ZoneId로 고정된 Clock 생성
     */
    public static Clock fixed(Instant instant, ZoneId zoneId) {
        return Clock.fixed(instant, zoneId);
    }
}
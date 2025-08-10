package me.rgunny.event.marketdata.domain.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.util.Objects;
import java.util.Set;

/**
 * 시장 거래시간 도메인 값 객체
 * 한국 주식시장 거래시간 관리
 *
 * - 모든 시간 연산은 내부에서 zoneId로 정규화
 * - 음수 Duration은 0으로 clamp 후 trace 로깅
 * - 주말/공휴일 판단을 도메인에서 책임
 */
public record MarketHours(
        LocalTime openTime,
        LocalTime closeTime,
        ZoneId zoneId
) {
    
    private static final Logger log = LoggerFactory.getLogger(MarketHours.class);
    
    /**
     * 한국 주식시장 기본 거래시간
     */
    public static final MarketHours KOREA_STOCK_MARKET = new MarketHours(
            LocalTime.of(9, 0),
            LocalTime.of(15, 30),
            ZoneId.of("Asia/Seoul")
    );
    
    /**
     * 프리마켓 시간 (08:30 ~ 09:00)
     */
    public static final MarketHours KOREA_PRE_MARKET = new MarketHours(
            LocalTime.of(8, 30),
            LocalTime.of(9, 0),
            ZoneId.of("Asia/Seoul")
    );
    
    /**
     * 애프터마켓 시간 (15:30 ~ 16:00)
     */
    public static final MarketHours KOREA_AFTER_MARKET = new MarketHours(
            LocalTime.of(15, 30),
            LocalTime.of(16, 0),
            ZoneId.of("Asia/Seoul")
    );
    
    /**
     * 현재 시간이 거래시간인지 확인
     * 
     * @return 거래시간이면 true
     */
    public boolean isMarketOpen() {
        return isMarketOpen(ZonedDateTime.now(zoneId));
    }
    
    /**
     * 특정 시간이 거래시간인지 확인
     * 타임존 정규화 포함
     * 
     * @param dateTime 확인할 시간 (어떤 타임존이든 허용)
     * @return 거래시간이면 true
     */
    public boolean isMarketOpen(ZonedDateTime dateTime) {
        Objects.requireNonNull(dateTime, "DateTime must not be null");
        
        // 타임존 정규화 - 도메인이 책임
        ZonedDateTime normalized = dateTime.withZoneSameInstant(zoneId);
        
        // 주말 체크
        if (isWeekend(normalized)) {
            return false;
        }
        
        // 공휴일 체크
        if (isHoliday(normalized)) {
            return false;
        }
        
        LocalTime currentTime = normalized.toLocalTime();
        return !currentTime.isBefore(openTime) && !currentTime.isAfter(closeTime);
    }
    
    /**
     * 주말 여부 확인
     * 
     * @param dateTime 확인할 날짜시간
     * @return 주말이면 true
     */
    public boolean isWeekend(ZonedDateTime dateTime) {
        ZonedDateTime normalized = dateTime.withZoneSameInstant(zoneId);
        DayOfWeek dayOfWeek = normalized.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
    
    /**
     * 공휴일 여부 확인
     * TODO: 실제 공휴일 API 연동 필요 (한국거래소 휴장일 API)
     * 
     * @param dateTime 확인할 날짜시간
     * @return 공휴일이면 true
     */
    public boolean isHoliday(ZonedDateTime dateTime) {
        ZonedDateTime normalized = dateTime.withZoneSameInstant(zoneId);
        LocalDate date = normalized.toLocalDate();
        // 2025년 주요 공휴일 하드코딩 (임시)
        Set<LocalDate> holidays2025 = Set.of(
                LocalDate.of(2025, 1, 1),   // 신정
                LocalDate.of(2025, 1, 28),  // 설날 연휴
                LocalDate.of(2025, 1, 29),  // 설날
                LocalDate.of(2025, 1, 30),  // 설날 연휴
                LocalDate.of(2025, 3, 1),   // 삼일절
                LocalDate.of(2025, 5, 5),   // 어린이날
                LocalDate.of(2025, 6, 6),   // 현충일
                LocalDate.of(2025, 8, 15),  // 광복절
                LocalDate.of(2025, 10, 3),  // 개천절
                LocalDate.of(2025, 10, 9),  // 한글날
                LocalDate.of(2025, 12, 25)  // 크리스마스
        );
        
        return holidays2025.contains(date);
    }
    
    /**
     * Duration 경계값 보정
     * 음수 Duration을 0으로 clamp하고 trace 로깅
     * 
     * @param duration 보정할 Duration
     * @param context 로깅용 컨텍스트
     * @return 보정된 Duration (>= 0)
     */
    private Duration clampDuration(Duration duration, String context) {
        if (duration.isNegative()) {
            log.debug("Negative duration {} detected in {}, clamping to ZERO", duration, context);
            return Duration.ZERO;
        }
        return duration;
    }
    
    /**
     * 다음 개장 시간까지 남은 시간 계산
     * 
     * @return 남은 시간 (Duration)
     */
    public Duration untilNextOpen() {
        return untilNextOpen(ZonedDateTime.now(zoneId));
    }
    
    /**
     * 다음 개장 시간까지 남은 시간 계산
     * 
     * @param now 기준 시간
     * @return 남은 시간 (Duration, 음수 방지)
     */
    public Duration untilNextOpen(ZonedDateTime now) {
        ZonedDateTime normalized = now.withZoneSameInstant(zoneId);
        ZonedDateTime nextOpen = getNextOpenTime(normalized);
        Duration duration = Duration.between(normalized, nextOpen);
        
        return clampDuration(duration, "untilNextOpen");
    }
    
    /**
     * 장 마감까지 남은 시간 계산
     * 
     * @param now 기준 시간
     * @return 마감까지 남은 시간 (Duration, 음수 방지)
     */
    public Duration untilClose(ZonedDateTime now) {
        ZonedDateTime normalized = now.withZoneSameInstant(zoneId);
        
        if (!isMarketOpen(normalized)) {
            log.trace("Market is not open at {}, returning ZERO", normalized);
            return Duration.ZERO;
        }
        
        ZonedDateTime closeTime = normalized.withHour(this.closeTime.getHour())
                                           .withMinute(this.closeTime.getMinute())
                                           .withSecond(0)
                                           .withNano(0);
        
        Duration duration = Duration.between(normalized, closeTime);
        return clampDuration(duration, "untilClose");
    }
    
    /**
     * 다음 개장 시간 계산
     * 
     * @param from 기준 시간
     * @return 다음 개장 시간
     */
    private ZonedDateTime getNextOpenTime(ZonedDateTime from) {
        ZonedDateTime candidate = from.withHour(openTime.getHour())
                                     .withMinute(openTime.getMinute())
                                     .withSecond(0)
                                     .withNano(0);
        
        // 오늘 개장 시간이 이미 지났거나 주말/공휴일이면 다음 영업일로
        while (candidate.isBefore(from) || isWeekend(candidate) || isHoliday(candidate)) {
            candidate = candidate.plusDays(1);
            
            // 주말 건너뛰기
            while (isWeekend(candidate) || isHoliday(candidate)) {
                candidate = candidate.plusDays(1);
            }
        }
        
        return candidate;
    }
    
    /**
     * 거래시간 정보 문자열 반환
     * 
     * @return "09:00 ~ 15:30 (Asia/Seoul)"
     */
    @Override
    public String toString() {
        return String.format("%s ~ %s (%s)", openTime, closeTime, zoneId);
    }
}
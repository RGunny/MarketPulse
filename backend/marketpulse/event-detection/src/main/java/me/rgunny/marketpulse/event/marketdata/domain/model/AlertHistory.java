package me.rgunny.marketpulse.event.marketdata.domain.model;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 알림 이력 도메인 엔티티
 */
public record AlertHistory(
        String id,
        String symbol,
        AlertType alertType,
        Instant alertedAt,
        Instant cooldownUntil
) {
    
    // 종목 코드 패턴: 숫자 6자리 또는 영문 대문자 + 숫자
    private static final Pattern SYMBOL_PATTERN = Pattern.compile("^[A-Z0-9]{1,10}$");
    private static final int MIN_COOLDOWN_MINUTES = 1;
    private static final int MAX_COOLDOWN_MINUTES = 2880; // 48시간
    
    /**
     * Compact Constructor - 모든 필드 null 체크
     */
    public AlertHistory {
        Objects.requireNonNull(id, "ID must not be null");
        Objects.requireNonNull(symbol, "Symbol must not be null");
        Objects.requireNonNull(alertType, "AlertType must not be null");
        Objects.requireNonNull(alertedAt, "AlertedAt must not be null");
        Objects.requireNonNull(cooldownUntil, "CooldownUntil must not be null");
    }

    /**
     * 팩토리 메서드: 새로운 알림 이력 생성
     * 
     * @param symbol 종목 코드 (A-Z, 0-9만 허용)
     * @param alertType 알림 타입
     * @param cooldownMinutes 쿨다운 시간(분, 1~2880)
     * @param clock 시간 소스 (테스트 가능)
     * @return AlertHistory
     */
    public static AlertHistory create(String symbol, AlertType alertType, 
                                     int cooldownMinutes, Clock clock) {
        Objects.requireNonNull(symbol, "Symbol must not be null");
        Objects.requireNonNull(alertType, "AlertType must not be null");
        Objects.requireNonNull(clock, "Clock must not be null");
        
        // Symbol 검증 - Redis Key 안전성
        validateSymbol(symbol);
        
        // 쿨다운 범위 검증
        validateCooldownMinutes(cooldownMinutes);
        
        Instant now = Instant.now(clock);
        return new AlertHistory(
                generateId(symbol, alertType),
                symbol,
                alertType,
                now,
                now.plusSeconds(cooldownMinutes * 60L)
        );
    }
    
    /**
     * Symbol 검증
     * Redis Key로 사용되므로 특수문자 방지
     */
    private static void validateSymbol(String symbol) {
        if (!SYMBOL_PATTERN.matcher(symbol).matches()) {
            throw new IllegalArgumentException(
                String.format("Invalid symbol format: %s. Must be alphanumeric (A-Z, 0-9) and max 10 chars", symbol)
            );
        }
    }
    
    /**
     * 쿨다운 시간 검증
     */
    private static void validateCooldownMinutes(int cooldownMinutes) {
        if (cooldownMinutes < MIN_COOLDOWN_MINUTES || cooldownMinutes > MAX_COOLDOWN_MINUTES) {
            throw new IllegalArgumentException(
                String.format("Cooldown must be between %d and %d minutes (48 hours)", 
                    MIN_COOLDOWN_MINUTES, MAX_COOLDOWN_MINUTES)
            );
        }
    }
    
    /**
     * ID 생성 규칙: {symbol}:{alertType}
     * Redis Key로 사용하기 적합한 형태
     */
    private static String generateId(String symbol, AlertType alertType) {
        return String.format("%s:%s", symbol, alertType.name());
    }
    
    /**
     * 쿨다운 중인지 확인
     * 
     * @param clock 시간 소스
     * @return 쿨다운 중이면 true
     */
    public boolean isInCooldown(Clock clock) {
        return Instant.now(clock).isBefore(cooldownUntil);
    }
    
    /**
     * 쿨다운 남은 시간 계산 (Duration)
     * 정밀도 개선: Duration 사용으로 밀리초 단위 보존
     * 
     * @param clock 시간 소스
     * @return 남은 시간, 쿨다운이 끝났으면 Duration.ZERO
     */
    public Duration getRemainingCooldown(Clock clock) {
        if (!isInCooldown(clock)) {
            return Duration.ZERO;
        }
        Duration remaining = Duration.between(Instant.now(clock), cooldownUntil);

        // 음수 방지 (경계값 보정)
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }
    
    /**
     * 쿨다운 남은 시간(초) 계산
     * 하위 호환성을 위해 유지
     * 
     * @param clock 시간 소스
     * @return 남은 시간(초), 쿨다운이 끝났으면 0
     */
    public long getRemainingCooldownSeconds(Clock clock) {
        return getRemainingCooldown(clock).getSeconds();
    }
    
    /**
     * 같은 종목의 같은 타입 알림인지 확인
     * 
     * @param symbol 종목 코드
     * @param alertType 알림 타입
     * @return 일치하면 true
     */
    public boolean matches(String symbol, AlertType alertType) {
        return this.symbol.equals(symbol) && this.alertType == alertType;
    }
}
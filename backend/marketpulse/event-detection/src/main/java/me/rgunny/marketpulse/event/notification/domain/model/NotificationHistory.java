package me.rgunny.marketpulse.event.notification.domain.model;

import me.rgunny.marketpulse.notification.grpc.NotificationServiceProto.PriceAlertType;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 알림 발송 이력 도메인 엔티티
 * 
 * 중복 알림 방지를 위한 발송 이력 관리
 * Redis에 저장되며 TTL로 자동 만료
 */
public record NotificationHistory(
    String id,                    // Redis Key: "notification:history:{symbol}:{alertType}"
    String symbol,                // 종목코드
    String symbolName,            // 종목명
    PriceAlertType alertType,     // 알림 타입 (RISE, FALL, LIMIT_UP, LIMIT_DOWN)
    BigDecimal triggerPrice,      // 알림 발생 시점 가격
    BigDecimal changeRate,        // 변동률
    LocalDateTime sentAt,         // 발송 시간
    String notificationId,        // gRPC 응답 ID
    Duration cooldownPeriod       // 쿨다운 기간
) {
    
    public NotificationHistory {
        Objects.requireNonNull(symbol, "Symbol must not be null");
        Objects.requireNonNull(alertType, "AlertType must not be null");
        Objects.requireNonNull(sentAt, "SentAt must not be null");
        Objects.requireNonNull(cooldownPeriod, "CooldownPeriod must not be null");
        
        if (symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol must not be blank");
        }
        
        if (cooldownPeriod.isNegative() || cooldownPeriod.isZero()) {
            throw new IllegalArgumentException("Cooldown period must be positive");
        }
    }
    
    /**
     * 팩토리 메서드: 새로운 알림 이력 생성
     */
    public static NotificationHistory create(
            String symbol,
            String symbolName,
            PriceAlertType alertType,
            BigDecimal triggerPrice,
            BigDecimal changeRate,
            String notificationId,
            Duration cooldownPeriod) {
        
        String id = generateId(symbol, alertType);
        
        return new NotificationHistory(
            id,
            symbol,
            symbolName,
            alertType,
            triggerPrice,
            changeRate,
            LocalDateTime.now(),
            notificationId,
            cooldownPeriod
        );
    }
    
    /**
     * Redis Key 생성 전략
     * 종목별, 알림타입별로 구분하여 저장
     */
    private static String generateId(String symbol, PriceAlertType alertType) {
        return String.format("notification:history:%s:%s", symbol, alertType);
    }
    
    /**
     * 쿨다운 중인지 확인
     */
    public boolean isInCooldown() {
        return isInCooldown(LocalDateTime.now());
    }
    
    /**
     * 특정 시점 기준 쿨다운 체크 (테스트 용이성)
     */
    public boolean isInCooldown(LocalDateTime checkTime) {
        Duration elapsed = Duration.between(sentAt, checkTime);
        return elapsed.compareTo(cooldownPeriod) < 0;
    }
    
    /**
     * 쿨다운 남은 시간 계산
     */
    public Duration getRemainingCooldown() {
        return getRemainingCooldown(LocalDateTime.now());
    }
    
    /**
     * 특정 시점 기준 남은 쿨다운 시간
     */
    public Duration getRemainingCooldown(LocalDateTime checkTime) {
        Duration elapsed = Duration.between(sentAt, checkTime);
        Duration remaining = cooldownPeriod.minus(elapsed);
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }
    
    /**
     * 동일한 알림인지 판단
     * 종목과 알림 타입이 같으면 동일한 알림으로 간주
     */
    public boolean isSameAlert(String symbol, PriceAlertType alertType) {
        return this.symbol.equals(symbol) && this.alertType == alertType;
    }
    
    /**
     * Redis TTL 계산
     * 쿨다운 기간 + 버퍼(5분)
     */
    public Duration calculateTTL() {
        return cooldownPeriod.plus(Duration.ofMinutes(5));
    }
}
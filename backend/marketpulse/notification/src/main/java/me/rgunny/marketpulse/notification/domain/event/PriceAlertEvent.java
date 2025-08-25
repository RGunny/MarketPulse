package me.rgunny.marketpulse.notification.domain.event;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 가격 변동 알림 이벤트
 */
public record PriceAlertEvent(
        String eventId,
        String symbol,
        String stockName,
        BigDecimal previousPrice,
        BigDecimal currentPrice,
        BigDecimal changeRate,
        String alertType, // RISE, FALL, LIMIT_UP, LIMIT_DOWN
        LocalDateTime timestamp,
        Map<String, Object> metadata
) implements MarketEvent {
    
    /**
     * 변동률 문자열 생성
     */
    public String getChangeRateString() {
        String sign = changeRate.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
        return sign + changeRate.setScale(2, RoundingMode.HALF_UP) + "%";
    }
    
    /**
     * 알림 메시지 생성
     */
    public String generateMessage() {
        return String.format(
                "%s(%s) %s\n%,d원 → %,d원 (%s)",
                stockName,
                symbol,
                getAlertTypeDescription(),
                previousPrice.intValue(),
                currentPrice.intValue(),
                getChangeRateString()
        );
    }
    
    private String getAlertTypeDescription() {
        return switch (alertType) {
            case "RISE" -> "📈 가격 상승";
            case "FALL" -> "📉 가격 하락";
            case "LIMIT_UP" -> "🚀 상한가";
            case "LIMIT_DOWN" -> "💥 하한가";
            default -> "가격 변동";
        };
    }
}
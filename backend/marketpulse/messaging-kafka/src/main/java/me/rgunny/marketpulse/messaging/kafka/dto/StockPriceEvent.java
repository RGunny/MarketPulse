package me.rgunny.marketpulse.messaging.kafka.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record StockPriceEvent(
        String eventId,
        String symbol,
        String name,
        BigDecimal currentPrice,
        BigDecimal previousClose,
        BigDecimal changeAmount,
        BigDecimal changeRate,
        Long volume,
        EventType eventType,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp
) {
    public enum EventType {
        PRICE_UPDATE,          // 일반 가격 업데이트
        THRESHOLD_EXCEEDED,    // 임계값 초과
        VOLUME_SPIKE,          // 거래량 급증
        RAPID_CHANGE           // 급격한 가격 변동
    }

    public static StockPriceEvent priceUpdate(String symbol, String name, BigDecimal currentPrice, 
                                             BigDecimal previousClose, Long volume) {
        var changeAmount = currentPrice.subtract(previousClose);
        var changeRate = previousClose.compareTo(BigDecimal.ZERO) != 0 
                ? changeAmount.divide(previousClose, 4, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
                
        return StockPriceEvent.builder()
                .eventId(generateEventId())
                .symbol(symbol)
                .name(name)
                .currentPrice(currentPrice)
                .previousClose(previousClose)
                .changeAmount(changeAmount)
                .changeRate(changeRate)
                .volume(volume)
                .eventType(EventType.PRICE_UPDATE)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private static String generateEventId() {
        return "EVT-" + System.currentTimeMillis() + "-" + Thread.currentThread().getId();
    }
}
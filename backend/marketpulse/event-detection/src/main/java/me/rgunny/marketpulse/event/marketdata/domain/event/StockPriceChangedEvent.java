package me.rgunny.marketpulse.event.marketdata.domain.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import me.rgunny.marketpulse.messaging.kafka.core.KafkaEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * 주가 변경 이벤트
 */
@Builder
public record StockPriceChangedEvent(
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
) implements KafkaEvent {
    
    public enum EventType {
        PRICE_UPDATE,           // 일반 가격 업데이트
        THRESHOLD_EXCEEDED,     // 임계값 초과
        VOLUME_SPIKE,          // 거래량 급증
        RAPID_CHANGE           // 급격한 가격 변동
    }
    
    @Override
    public String getId() {
        return eventId;
    }
    
    @Override
    public String getSource() {
        return "marketpulse.event-detection";
    }
    
    @Override
    public String getType() {
        return switch (eventType) {
            case PRICE_UPDATE -> "stock.price.updated";
            case THRESHOLD_EXCEEDED -> "stock.threshold.exceeded";
            case VOLUME_SPIKE -> "stock.volume.spike";
            case RAPID_CHANGE -> "stock.rapid.change";
        };
    }
    
    @Override
    public Instant getTime() {
        return timestamp.toInstant(ZoneOffset.UTC);
    }
    
    @Override
    public String getAggregateId() {
        return symbol;
    }
    
    /**
     * 가격 업데이트 이벤트 생성 팩토리 메서드
     */
    public static StockPriceChangedEvent priceUpdate(String symbol, String name, 
                                                     BigDecimal currentPrice, 
                                                     BigDecimal previousClose, 
                                                     Long volume) {
        var changeAmount = currentPrice.subtract(previousClose);
        var changeRate = previousClose.compareTo(BigDecimal.ZERO) != 0 
                ? changeAmount.divide(previousClose, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
                
        return StockPriceChangedEvent.builder()
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
    
    /**
     * 임계값 초과 이벤트 생성
     */
    public static StockPriceChangedEvent thresholdExceeded(String symbol, String name,
                                                          BigDecimal currentPrice,
                                                          BigDecimal previousClose,
                                                          Long volume) {
        var event = priceUpdate(symbol, name, currentPrice, previousClose, volume);
        return event.toBuilder()
                .eventType(EventType.THRESHOLD_EXCEEDED)
                .build();
    }
    
    /**
     * 거래량 급증 이벤트 생성
     */
    public static StockPriceChangedEvent volumeSpike(String symbol, String name,
                                                    BigDecimal currentPrice,
                                                    Long volume) {
        return StockPriceChangedEvent.builder()
                .eventId(generateEventId())
                .symbol(symbol)
                .name(name)
                .currentPrice(currentPrice)
                .previousClose(currentPrice) // 거래량 이벤트는 가격 변동 무관
                .changeAmount(BigDecimal.ZERO)
                .changeRate(BigDecimal.ZERO)
                .volume(volume)
                .eventType(EventType.VOLUME_SPIKE)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    private static String generateEventId() {
        return "EVT-" + UUID.randomUUID().toString();
    }
    
    // Builder에 toBuilder 추가를 위한 내부 빌더 확장
    public StockPriceChangedEventBuilder toBuilder() {
        return builder()
                .eventId(this.eventId)
                .symbol(this.symbol)
                .name(this.name)
                .currentPrice(this.currentPrice)
                .previousClose(this.previousClose)
                .changeAmount(this.changeAmount)
                .changeRate(this.changeRate)
                .volume(this.volume)
                .eventType(this.eventType)
                .timestamp(this.timestamp);
    }
}
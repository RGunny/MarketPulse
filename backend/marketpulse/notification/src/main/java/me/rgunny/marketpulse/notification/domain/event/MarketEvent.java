package me.rgunny.marketpulse.notification.domain.event;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 시장 이벤트 인터페이스
 * - gRPC로부터 수신하는 이벤트의 공통 인터페이스
 */
public sealed interface MarketEvent 
    permits PriceAlertEvent {
    
    String eventId();
    String symbol();
    LocalDateTime timestamp();
    Map<String, Object> metadata();
}
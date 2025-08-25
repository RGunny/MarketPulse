package me.rgunny.marketpulse.messaging.kafka.consumer.example;

import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.messaging.kafka.consumer.EventHandler;
import me.rgunny.marketpulse.messaging.kafka.dto.StockPriceEvent;

/**
 * EventHandler 구현 예제
 * 실제로는 각 도메인 모듈(event-detection, notification 등)에서 구현
 * 
 * 예시:
 * - event-detection 모듈: PriceAnalysisEventHandler
 * - notification 모듈: AlertNotificationEventHandler
 * - monitoring 모듈: MetricsCollectorEventHandler
 */
@Slf4j
// @Component  // 실제 구현체에서는 @Component 추가
public class ExampleEventHandler implements EventHandler<StockPriceEvent> {
    
    @Override
    public boolean canHandle(Object event) {
        // 이 핸들러가 처리할 수 있는 이벤트 타입 확인
        if (!(event instanceof StockPriceEvent stockPriceEvent)) {
            return false;
        }
        
        // 특정 이벤트 타입만 처리하도록 필터링 가능
        return switch (stockPriceEvent.eventType()) {
            case THRESHOLD_EXCEEDED, RAPID_CHANGE -> true;
            default -> false;
        };
    }
    
    @Override
    public void handle(StockPriceEvent event) {
        // 도메인 특화 비즈니스 로직 처리
        log.info("Processing event in domain: eventId={}, type={}", 
                event.eventId(), event.eventType());
        
        switch (event.eventType()) {
            case THRESHOLD_EXCEEDED -> handleThresholdExceeded(event);
            case RAPID_CHANGE -> handleRapidChange(event);
            default -> log.debug("Event type not handled by this handler");
        }
    }
    
    @Override
    public int getOrder() {
        // 우선순위 설정 (낮을수록 먼저 실행)
        return 10;
    }
    
    private void handleThresholdExceeded(StockPriceEvent event) {
        // 도메인 특화 로직
        // 예: notification 모듈에서는 알림 발송
        log.info("Domain-specific handling for threshold exceeded: {}", event.symbol());
    }
    
    private void handleRapidChange(StockPriceEvent event) {
        // 도메인 특화 로직
        log.info("Domain-specific handling for rapid change: {}", event.symbol());
    }
}
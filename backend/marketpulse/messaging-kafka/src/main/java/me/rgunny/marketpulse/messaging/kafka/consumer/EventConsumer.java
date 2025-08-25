package me.rgunny.marketpulse.messaging.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.messaging.kafka.dto.StockPriceEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * 제네릭 이벤트 컨슈머 - 도메인 중립적
 * 비즈니스 로직 없이 이벤트 라우팅만 담당
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventConsumer {
    
    // Spring이 모든 EventHandler 구현체를 자동 주입
    private final List<EventHandler<StockPriceEvent>> eventHandlers;

    @KafkaListener(
            topics = "${kafka.topics.stock-price:stock-price-events}",
            groupId = "${spring.kafka.consumer.group-id:marketpulse-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeStockPriceEvent(
            @Payload StockPriceEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received event: eventId={}, type={}, symbol={}, partition={}, offset={}",
                    event.eventId(), event.eventType(), event.symbol(), partition, offset);
            
            // 이벤트를 처리할 수 있는 핸들러들에게 전달
            int processedCount = 0;
            for (EventHandler<StockPriceEvent> handler : getOrderedHandlers(event)) {
                try {
                    handler.handle(event);
                    processedCount++;
                    log.debug("Event processed by handler: {}", handler.getClass().getSimpleName());
                } catch (Exception e) {
                    log.error("Handler {} failed to process event: {}", 
                            handler.getClass().getSimpleName(), e.getMessage(), e);
                    // 개별 핸들러 실패는 전체 처리를 중단시키지 않음
                }
            }
            
            if (processedCount == 0) {
                log.warn("No handlers processed event: eventId={}, type={}", 
                        event.eventId(), event.eventType());
            }
            
            acknowledgment.acknowledge();
            log.debug("Event acknowledged: eventId={}, processedBy={} handlers", 
                    event.eventId(), processedCount);
            
        } catch (Exception e) {
            log.error("Critical error processing event: eventId={}, error={}", 
                    event.eventId(), e.getMessage(), e);
            // TODO: DLQ(Dead Letter Queue) 전송 또는 재시도 로직
            // 현재는 ACK하지 않아 재처리되도록 함
        }
    }
    
    private List<EventHandler<StockPriceEvent>> getOrderedHandlers(StockPriceEvent event) {
        return eventHandlers.stream()
                .filter(handler -> handler.canHandle(event))
                .sorted(Comparator.comparingInt(EventHandler::getOrder))
                .toList();
    }
}
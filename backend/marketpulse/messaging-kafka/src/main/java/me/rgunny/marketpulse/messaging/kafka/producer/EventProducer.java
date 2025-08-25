package me.rgunny.marketpulse.messaging.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.messaging.kafka.dto.StockPriceEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * @deprecated Use {@link GenericEventProducer} instead
 */
@Deprecated(since = "1.1.0", forRemoval = true)
@Slf4j
@Component
@RequiredArgsConstructor
public class EventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.stock-price:stock-price-events}")
    private String stockPriceTopic;

    public CompletableFuture<SendResult<String, Object>> sendStockPriceEvent(StockPriceEvent event) {
        log.debug("Sending stock price event: symbol={}, price={}", event.symbol(), event.currentPrice());
        
        return kafkaTemplate.send(stockPriceTopic, event.symbol(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Successfully sent event: eventId={}, partition={}, offset={}", 
                                event.eventId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to send event: eventId={}, error={}", 
                                event.eventId(), ex.getMessage(), ex);
                    }
                });
    }

    public void sendStockPriceEventAsync(StockPriceEvent event) {
        sendStockPriceEvent(event);
    }
}
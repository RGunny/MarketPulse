package me.rgunny.marketpulse.messaging.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.messaging.kafka.core.KafkaEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * 제네릭 이벤트 프로듀서
 * 모든 타입의 KafkaEvent를 처리할 수 있는 범용 프로듀서
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GenericEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 이벤트를 지정된 토픽으로 발송
     * 
     * @param topic 대상 토픽
     * @param event 발송할 이벤트
     * @return 발송 결과
     */
    public <T extends KafkaEvent> CompletableFuture<SendResult<String, Object>> send(String topic, T event) {
        
        // 직접 send 메서드 사용 (topic, key, data)
        return kafkaTemplate.send(topic, event.getAggregateId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Event sent successfully: eventId={}, type={}, topic={}, partition={}, offset={}", 
                                event.getId(),
                                event.getType(),
                                topic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to send event: eventId={}, type={}, error={}", 
                                event.getId(), 
                                event.getType(), 
                                ex.getMessage(), 
                                ex);
                    }
                });
    }
    
    /**
     * 이벤트를 비동기로 발송
     */
    public <T extends KafkaEvent> void sendAsync(String topic, T event) {
        send(topic, event);
    }
}
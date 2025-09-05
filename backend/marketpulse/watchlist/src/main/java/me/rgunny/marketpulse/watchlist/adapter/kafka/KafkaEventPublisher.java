package me.rgunny.marketpulse.watchlist.adapter.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.messaging.kafka.producer.GenericEventProducer;
import me.rgunny.marketpulse.watchlist.application.out.EventPublisher;
import me.rgunny.marketpulse.watchlist.domain.WatchTarget;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        value = "kafka.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher implements EventPublisher {

    private final GenericEventProducer producer;

    @Value("${watchlist.kafka.topic:watchlist}")
    private String topic;

    @Override
    public void publishCreated(WatchTarget watchTarget) {
        WatchTargetEvent event = WatchTargetEvent.fromDomain(watchTarget, WatchTargetEvent.EventType.CREATED);
        producer.send(topic, event);
        log.info("Published CREATED event: targetId={}, stockCode={}, stockName={},", watchTarget.getId(), watchTarget.getStockCode(), watchTarget.getStockName());
    }

    @Override
    public void publishActivated(WatchTarget watchTarget) {
        WatchTargetEvent event = WatchTargetEvent.fromDomain(watchTarget, WatchTargetEvent.EventType.ACTIVATED);
        producer.send(topic, event);
        log.info("Published ACTIVATED event: targetId={}, stockCode={}, stockName={},", watchTarget.getId(), watchTarget.getStockCode(), watchTarget.getStockName());
    }

    @Override
    public void publishDeactivated(WatchTarget watchTarget) {
        WatchTargetEvent event = WatchTargetEvent.fromDomain(watchTarget, WatchTargetEvent.EventType.DEACTIVATED);
        producer.send(topic, event);
        log.info("Published DEACTIVATED event: targetId={}, stockCode={}, stockName={},", watchTarget.getId(), watchTarget.getStockCode(), watchTarget.getStockName());
    }

}

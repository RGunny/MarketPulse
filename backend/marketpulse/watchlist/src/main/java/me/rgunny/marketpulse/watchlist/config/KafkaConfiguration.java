package me.rgunny.marketpulse.watchlist.config;

import me.rgunny.marketpulse.messaging.kafka.config.KafkaConfig;
import me.rgunny.marketpulse.messaging.kafka.producer.GenericEventProducer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnProperty(
        prefix = "kafka",
        name = "enbaled",
        havingValue = "true",
        matchIfMissing = true
)
@Import({
        GenericEventProducer.class,
        KafkaConfig.class
})
public class KafkaConfiguration {
}

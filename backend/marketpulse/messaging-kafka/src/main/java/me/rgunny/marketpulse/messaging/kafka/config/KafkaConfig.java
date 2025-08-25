package me.rgunny.marketpulse.messaging.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;

@Configuration
@EnableKafka
@ConditionalOnProperty(value = "spring.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConfig {

    @Value("${kafka.topics.stock-price:stock-price-events}")
    private String stockPriceTopic;

    @Value("${kafka.topics.market-ranking:market-ranking-events}")
    private String marketRankingTopic;

    @Value("${kafka.topics.alert:alert-events}")
    private String alertTopic;

    @Bean
    public NewTopic stockPriceTopic() {
        return TopicBuilder.name(stockPriceTopic)
                .partitions(3)
                .replicas(1)
                .compact()
                .build();
    }

    @Bean
    public NewTopic marketRankingTopic() {
        return TopicBuilder.name(marketRankingTopic)
                .partitions(3)
                .replicas(1)
                .compact()
                .build();
    }

    @Bean
    public NewTopic alertTopic() {
        return TopicBuilder.name(alertTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public RecordMessageConverter converter() {
        return new JsonMessageConverter();
    }
}
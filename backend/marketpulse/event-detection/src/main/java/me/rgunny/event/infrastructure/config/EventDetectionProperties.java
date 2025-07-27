package me.rgunny.event.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "event-detection")
public record EventDetectionProperties(
        boolean enabled,
        Thresholds thresholds,
        Scheduler scheduler
) {
    public EventDetectionProperties {
        // 기본값 설정
        if (thresholds == null) {
            thresholds = new Thresholds(5.0, -5.0, 2.0);
        }
        if (scheduler == null) {
            scheduler = new Scheduler(Duration.ofSeconds(30));
        }
    }

    public record Thresholds(
            double priceSpikePercent,
            double priceDropPercent,
            double volumeSurgeRatio
    ) {}

    public record Scheduler(
            Duration interval
    ) {}
}
package me.rgunny.event.infrastructure.adapter.output.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "market-data.kis")
public record KISProperties(
        boolean enabled,
        String baseUrl,
        String appKey,
        String appSecret
) {}

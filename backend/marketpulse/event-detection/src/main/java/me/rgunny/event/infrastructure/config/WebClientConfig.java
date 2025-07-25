package me.rgunny.event.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean("kisWebClient")
    public WebClient kisWebClient(WebClient.Builder builder) {
        return builder
            .baseUrl("https://openapi.koreainvestment.com:9443")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .codecs(configurer -> configurer
                .defaultCodecs()
                .maxInMemorySize(1024 * 1024))
            .build();
    }

    @Bean("defaultWebClient")
    public WebClient defaultWebClient(WebClient.Builder builder) {
        return builder
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .codecs(configurer -> configurer
                .defaultCodecs()
                .maxInMemorySize(1024 * 1024))
            .build();
    }
}

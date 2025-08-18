package me.rgunny.marketpulse.event.marketdata.infrastructure.config.shared;

import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.event.marketdata.infrastructure.config.kis.KISApiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;

/**
 * WebClient 설정
 */
@Slf4j
@Configuration
public class WebClientConfig {
    
    @Autowired
    private KISApiProperties kisApiProperties;
    
    /**
     * KIS API용 WebClient
     */
    @Primary
    @Bean("kisWebClient")
    public WebClient kisWebClient(KISApiProperties kisApiProperties) {
        String baseUrl = kisApiProperties.baseUrl();
        log.info("Creating KIS WebClient with baseUrl: {}", baseUrl);
        
        if (baseUrl == null || baseUrl.isEmpty()) {
            log.error("KIS API baseUrl is null or empty!");
            throw new IllegalStateException("KIS API baseUrl must be configured");
        }
        
        WebClient client = WebClient.builder()
                .baseUrl(baseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
        
        log.info("WebClient created successfully with baseUrl: {}", baseUrl);
        return client;
    }
    
    /**
     * 기본 WebClient (다른 API용)
     */
    @Bean("defaultWebClient")
    public WebClient defaultWebClient() {
        log.info("Creating DEFAULT WebClient (no baseUrl)");
        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }
}
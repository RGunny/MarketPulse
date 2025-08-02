package me.rgunny.event.marketdata.infrastructure.config.shared;

import lombok.extern.slf4j.Slf4j;
import me.rgunny.event.marketdata.infrastructure.config.kis.KISApiProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient 설정
 */
@Slf4j
@Configuration
public class WebClientConfig {
    
    /**
     * KIS API용 WebClient
     */
    @Bean("kisWebClient")
    public WebClient kisWebClient(KISApiProperties kisApiProperties) {
        String baseUrl = kisApiProperties.baseUrl();
        log.info("Creating KIS WebClient with baseUrl: {}", baseUrl);
        
        return WebClient.builder()
                .baseUrl(baseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }
    
    /**
     * 기본 WebClient (다른 API용)
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }
}
package me.rgunny.marketpulse.event.marketdata.infrastructure.config.shared;

import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.event.marketdata.infrastructure.config.kis.KISApiProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

/**
 * WebClient 설정
 */
@Slf4j
@Configuration
public class WebClientConfig {
    
    /**
     * KIS API용 WebClient
     */
    @Primary
    @Bean("kisWebClient")
    public WebClient kisWebClient(KISApiProperties kisApiProperties) {
        String baseUrl = kisApiProperties.baseUrl();
        log.info("Creating KIS WebClient with baseUrl: {}", baseUrl);

        ConnectionProvider connectionProvider = initializeConnectionProvider();

        HttpClient httpClient = initializeHttpClient(connectionProvider);
        
        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))  // 2MB
                .build();
    }

    private HttpClient initializeHttpClient(ConnectionProvider connectionProvider) {
        return HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(10))
                .compress(true);
    }

    private ConnectionProvider initializeConnectionProvider() {
        return ConnectionProvider.builder("kis-pool")
                .maxConnections(100)
                .maxIdleTime(Duration.ofSeconds(20))
                .pendingAcquireTimeout(Duration.ofSeconds(30))
                .build();
    }

    /**
     * 기본 WebClient
     */
    @Bean("defaultWebClient")
    public WebClient defaultWebClient() {
        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }
}
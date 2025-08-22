package me.rgunny.marketpulse.event.marketdata.infrastructure.adapter.out.kis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.event.marketdata.application.port.out.kis.KISCredentialPort;
import me.rgunny.marketpulse.event.marketdata.application.port.out.kis.KISTokenCachePort;
import me.rgunny.marketpulse.event.marketdata.application.port.out.kis.KISTokenPort;
import me.rgunny.marketpulse.event.marketdata.domain.error.StockPriceErrorCode;
import me.rgunny.marketpulse.event.marketdata.domain.exception.kis.KisApiException;
import me.rgunny.marketpulse.event.marketdata.infrastructure.config.kis.KISApiProperties;
import me.rgunny.marketpulse.event.marketdata.infrastructure.dto.kis.KISTokenRequest;
import me.rgunny.marketpulse.event.marketdata.infrastructure.dto.kis.KISTokenResponse;
import me.rgunny.marketpulse.event.marketdata.infrastructure.resilience.KISApiCircuitBreakerService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * KIS API 토큰 관리 서비스
 * 
 * 토큰 발급, 캐싱, 갱신을 전담
 * 다른 Infrastructure 컴포넌트들이 이 서비스를 통해 토큰 획득
 */
@Slf4j
@Service
public class KISTokenService implements KISTokenPort {
    
    private final WebClient webClient;
    private final KISCredentialPort credentialPort;
    private final KISTokenCachePort tokenCachePort;
    private final KISApiProperties kisApiProperties;
    private final KISApiCircuitBreakerService circuitBreakerService;
    
    public KISTokenService(
            @Qualifier("kisWebClient") WebClient kisWebClient,
            KISCredentialPort credentialPort,
            KISTokenCachePort tokenCachePort,
            KISApiProperties kisApiProperties,
            KISApiCircuitBreakerService circuitBreakerService) {
        this.webClient = kisWebClient;
        this.credentialPort = credentialPort;
        this.tokenCachePort = tokenCachePort;
        this.kisApiProperties = kisApiProperties;
        this.circuitBreakerService = circuitBreakerService;
    }
    
    // KIS OAuth 토큰 유효시간 (24시간)
    private static final Duration TOKEN_TTL = Duration.ofHours(24);
    
    // API 호출 타임아웃 (10초)
    private static final Duration API_TIMEOUT = Duration.ofSeconds(10);
    
    @Override
    public Mono<String> getAccessToken() {
        return tokenCachePort.getToken()
                .filter(token -> token != null && !token.isEmpty())
                .switchIfEmpty(refreshToken())
                .onErrorResume(error -> {
                    log.warn("Token cache error, fetching new token", error);
                    return refreshToken();
                });
    }
    
    @Override
    public Mono<String> refreshToken() {
        return requestNewToken()
                .flatMap(token ->
                    tokenCachePort.saveToken(token, TOKEN_TTL)
                        .thenReturn(token)
                        .doOnSuccess(t -> log.debug("Token cached successfully"))
                );
    }
    
    @Override
    public Mono<Boolean> isTokenValid() {
        return tokenCachePort.getToken()
                .map(token -> token != null && !token.isEmpty())
                .defaultIfEmpty(false);
    }
    
    /**
     * KIS OAuth 토큰 발급 API 호출
     */
    private Mono<String> requestNewToken() {
        KISTokenRequest request = new KISTokenRequest(
                kisApiProperties.grantType(),
                credentialPort.getDecryptedAppKey(),
                credentialPort.getDecryptedAppSecret()
        );

        log.debug("Requesting KIS API Token");
        log.debug("Token API Path: {}", kisApiProperties.tokenPath());
        log.debug("Base URL from properties: {}", kisApiProperties.baseUrl());
        
        Mono<String> apiCall = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(kisApiProperties.tokenPath())
                        .build())
                .header("Content-Type", kisApiProperties.headers().contentType())
                .bodyValue(request)
                .retrieve()
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    response -> response.bodyToMono(String.class)
                        .flatMap(body -> {
                            log.error("KIS API Token Request Failed - Status: {}", 
                                    response.statusCode());
                            return Mono.error(new KisApiException(
                                    StockPriceErrorCode.STOCK_PRICE_005,
                                    "Token Request Failed: " + response.statusCode()));
                        })
                )
                .bodyToMono(KISTokenResponse.class)
                .map(KISTokenResponse::getAccessToken)
                .timeout(API_TIMEOUT)
                .doOnError(error -> log.error("KIS API Token Request Failed", error))
                .doOnSuccess(token -> log.debug("KIS API Token received successfully"));
        
        // 서킷브레이커 적용
        return circuitBreakerService.executeGetAccessToken(apiCall);
    }
}
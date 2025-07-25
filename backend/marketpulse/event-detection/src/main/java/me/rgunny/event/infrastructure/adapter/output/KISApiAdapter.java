package me.rgunny.event.infrastructure.adapter.output;

import me.rgunny.event.application.port.output.KISApiPort;
import me.rgunny.event.application.port.output.KISCredentialPort;
import me.rgunny.event.application.port.output.KISTokenCachePort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class KISApiAdapter implements KISApiPort {

    private final WebClient webClient;
    private final KISCredentialPort credentialPort;
    private final KISTokenCachePort tokenCachePort;
    
    // KIS OAuth 토큰 유효시간 (24시간)
    private static final Duration TOKEN_TTL = Duration.ofHours(24);

    public KISApiAdapter(@Qualifier("kisWebClient") WebClient webClient,
                        KISCredentialPort credentialPort,
                        KISTokenCachePort tokenCachePort) {
        this.credentialPort = credentialPort;
        this.tokenCachePort = tokenCachePort;
        this.webClient = webClient;
    }

    @Override
    public Mono<String> getAccessToken() {
        KISTokenRequest request = new KISTokenRequest(
                "client_credentials",
                credentialPort.getDecryptedAppKey(),
                credentialPort.getDecryptedAppSecret()
        );

        return webClient.post()
                .uri("/oauth2/tokenP")
                .header("Content-Type", "application/json")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(KISTokenResponse.class)
                .map(KISTokenResponse::getAccessToken)
                .timeout(Duration.ofSeconds(10));
    }

    @Override
    public Mono<String> getCachedOrNewToken() {
        return tokenCachePort.getToken()
                .filter(token -> token != null && !token.isEmpty())
                .switchIfEmpty(getAccessTokenAndCache())
                .onErrorResume(error -> getAccessTokenAndCache());
    }

    @Override
    public Mono<Boolean> validateConnection() {
        return getCachedOrNewToken()
                .map(token -> token != null && !token.isEmpty())
                .onErrorReturn(false);
    }

    /**
     * 새 토큰 발급 후 캐시에 저장
     */
    private Mono<String> getAccessTokenAndCache() {
        return getAccessToken()
                .flatMap(token -> 
                    tokenCachePort.saveToken(token, TOKEN_TTL)
                        .thenReturn(token)
                );
    }

    public record KISTokenRequest(
            String grant_type,
            String appkey,
            String appsecret
    ) {
    }

    public record KISTokenResponse(
            String access_token,
            String token_type,
            int expires_in
    ) {
        public String getAccessToken() {
            return access_token;
        }
    }
}

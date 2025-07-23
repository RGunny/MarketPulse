package me.rgunny.event.infrastructure.adapter.output;

import me.rgunny.event.application.port.output.KISApiPort;
import me.rgunny.event.application.port.output.KISCredentialPort;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class KISApiAdapter implements KISApiPort {

    private final WebClient webClient;
    private final KISCredentialPort credentialPort;

    public KISApiAdapter(WebClient.Builder webClientBuilder, KISCredentialPort credentialPort) {
        this.credentialPort = credentialPort;
        this.webClient = webClientBuilder
                .baseUrl(credentialPort.getBaseUrl())
                .build();
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
    public Mono<Boolean> validateConnection() {
        return getAccessToken()
                .map(token -> token != null && !token.isEmpty())
                .onErrorReturn(false);
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

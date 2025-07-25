package me.rgunny.event.application.service;

import me.rgunny.event.application.port.input.CheckKISConnectionUseCase;
import me.rgunny.event.application.port.input.KISConnectionStatus;
import me.rgunny.event.application.port.output.KISApiPort;
import me.rgunny.event.application.port.output.KISCredentialPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class KISConnectionService implements CheckKISConnectionUseCase {

    private final KISCredentialPort credentialPort;
    private final KISApiPort apiPort;

    public KISConnectionService(KISCredentialPort credentialPort, KISApiPort apiPort) {
        this.credentialPort = credentialPort;
        this.apiPort = apiPort;
    }

    @Override
    public Mono<KISConnectionStatus> checkConnection() {
        if (!credentialPort.isEnabled()) {
            return Mono.just(new KISConnectionStatus(
                    false,
                    "KIS API is disabled",
                    null,
                    0
            ));
        }

        long startTime = System.currentTimeMillis();

        return apiPort.getCachedOrNewToken()
                .map(token -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    String message = "Connection successful with cached token";

                    return new KISConnectionStatus(
                            true,
                            message,
                            credentialPort.getMaskedAppKey(),
                            responseTime
                    );
                })
                .onErrorReturn(new KISConnectionStatus(
                        false,
                        "Connection error: Network or authentication failure",
                        credentialPort.getMaskedAppKey(),
                        System.currentTimeMillis() - startTime
                ));
    }
}

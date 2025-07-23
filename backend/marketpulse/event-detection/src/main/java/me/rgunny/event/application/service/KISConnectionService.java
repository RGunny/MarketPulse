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

        return apiPort.validateConnection()
                .map(connected -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    String message = connected ? "Connection successful" : "Connection failed";

                    return new KISConnectionStatus(
                            connected,
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

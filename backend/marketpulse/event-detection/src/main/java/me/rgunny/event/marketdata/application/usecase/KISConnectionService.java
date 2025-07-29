package me.rgunny.event.marketdata.application.usecase;

import me.rgunny.event.marketdata.application.port.in.CheckKISConnectionUseCase;
import me.rgunny.event.marketdata.application.port.in.KISConnectionStatus;
import me.rgunny.event.marketdata.application.port.out.kis.KISCredentialPort;
import me.rgunny.event.marketdata.infrastructure.adapter.out.kis.KISApiAdapter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class KISConnectionService implements CheckKISConnectionUseCase {

    private final KISCredentialPort credentialPort;
    private final KISApiAdapter kisApiAdapter;

    public KISConnectionService(KISCredentialPort credentialPort, KISApiAdapter kisApiAdapter) {
        this.credentialPort = credentialPort;
        this.kisApiAdapter = kisApiAdapter;
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

        return kisApiAdapter.getCachedOrNewToken()
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
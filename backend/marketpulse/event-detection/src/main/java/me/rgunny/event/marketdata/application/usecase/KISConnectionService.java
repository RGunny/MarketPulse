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

        return kisApiAdapter.validateConnection()
                .map(isConnected -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    
                    if (isConnected) {
                        return new KISConnectionStatus(
                                true,
                                "KIS API connection successful",
                                credentialPort.getMaskedAppKey(),
                                responseTime
                        );
                    } else {
                        return new KISConnectionStatus(
                                false,
                                "KIS API connection failed",
                                credentialPort.getMaskedAppKey(),
                                responseTime
                        );
                    }
                })
                .onErrorResume(error -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    return Mono.just(new KISConnectionStatus(
                            false,
                            "Connection error: " + error.getMessage(),
                            credentialPort.getMaskedAppKey(),
                            responseTime
                    ));
                });
    }
}
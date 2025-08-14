package me.rgunny.marketpulse.event.marketdata.application.usecase;

import me.rgunny.marketpulse.event.marketdata.application.port.in.CheckKISConnectionUseCase;
import me.rgunny.marketpulse.event.marketdata.application.port.in.KISConnectionStatus;
import me.rgunny.marketpulse.event.marketdata.application.port.out.ExternalApiPort;
import me.rgunny.marketpulse.event.marketdata.application.port.out.kis.KISCredentialPort;
import me.rgunny.marketpulse.event.marketdata.domain.model.StockPrice;
import me.rgunny.marketpulse.event.shared.domain.value.MarketDataType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class KISConnectionService implements CheckKISConnectionUseCase {

    private final KISCredentialPort credentialPort;
    private final ExternalApiPort externalApiPort;

    public KISConnectionService(KISCredentialPort credentialPort, ExternalApiPort externalApiPort) {
        this.credentialPort = credentialPort;
        this.externalApiPort = externalApiPort;
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

        // 연결 테스트를 위해 삼성전자 종목으로 테스트
        return externalApiPort.fetchMarketData("005930", MarketDataType.STOCK, StockPrice.class)
                .map(data -> true)
                .onErrorReturn(false)
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
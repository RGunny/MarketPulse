package me.rgunny.event.infrastructure.adapter.output;

import me.rgunny.event.application.port.output.KISCredentialPort;
import org.springframework.stereotype.Component;

@Component
public class KISProvider {

    private final KISCredentialPort credentialPort;

    public KISProvider(KISCredentialPort credentialPort) {
        this.credentialPort = credentialPort;
    }

    public void testConnection() {
        if (!credentialPort.isEnabled()) {
            System.out.println("KIS API disabled");
            return;
        }

        System.out.println("KIS API Test - URL: " + credentialPort.getBaseUrl() +
                ", Key: " + credentialPort.getMaskedAppKey());
    }
}
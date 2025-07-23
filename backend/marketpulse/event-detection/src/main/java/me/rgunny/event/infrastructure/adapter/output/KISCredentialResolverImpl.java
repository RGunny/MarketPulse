package me.rgunny.event.infrastructure.adapter.output;

import me.rgunny.event.application.port.output.KISCredentialPort;
import me.rgunny.event.infrastructure.adapter.output.properties.KISProperties;
import me.rgunny.marketpulse.common.security.CryptoService;
import org.springframework.stereotype.Component;

@Component
public class KISCredentialResolverImpl implements KISCredentialPort {

    private final KISProperties properties;
    private final CryptoService cryptoService;

    public KISCredentialResolverImpl(KISProperties properties, CryptoService cryptoService) {
        this.properties = properties;
        this.cryptoService = cryptoService;
    }

    @Override
    public boolean isEnabled() {
        return properties.enabled();
    }

    @Override
    public String getBaseUrl() {
        return properties.baseUrl();
    }

    @Override
    public String getDecryptedAppKey() {
        return cryptoService.decrypt(properties.appKey());
    }

    @Override
    public String getDecryptedAppSecret() {
        return cryptoService.decrypt(properties.appSecret());
    }

    @Override
    public String getMaskedAppKey() {
        return cryptoService.maskSensitiveValue(getDecryptedAppKey());
    }
}
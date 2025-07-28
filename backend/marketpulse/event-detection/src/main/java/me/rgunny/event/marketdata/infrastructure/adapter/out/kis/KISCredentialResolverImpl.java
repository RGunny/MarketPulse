package me.rgunny.event.marketdata.infrastructure.adapter.out.kis;

import me.rgunny.event.marketdata.application.port.out.kis.KISCredentialPort;
import me.rgunny.event.marketdata.infrastructure.config.kis.KISApiProperties;
import me.rgunny.marketpulse.common.security.CryptoService;
import org.springframework.stereotype.Component;

@Component
public class KISCredentialResolverImpl implements KISCredentialPort {

    private final KISApiProperties properties;
    private final CryptoService cryptoService;

    public KISCredentialResolverImpl(KISApiProperties properties, CryptoService cryptoService) {
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
package me.rgunny.marketpulse.event.marketdata.infrastructure.adapter.out.kis;

import me.rgunny.marketpulse.event.marketdata.application.port.out.kis.KISCredentialPort;
import me.rgunny.marketpulse.event.marketdata.infrastructure.config.kis.KISApiProperties;
import org.springframework.stereotype.Component;

@Component
public class KISCredentialResolverImpl implements KISCredentialPort {

    private final KISApiProperties properties;

    public KISCredentialResolverImpl(KISApiProperties properties) {
        this.properties = properties;
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
        // Jasypt가 자동으로 복호화하므로 그대로 반환
        return properties.appKey();
    }

    @Override
    public String getDecryptedAppSecret() {
        // Jasypt가 자동으로 복호화하므로 그대로 반환
        return properties.appSecret();
    }

    @Override
    public String getMaskedAppKey() {
        String appKey = getDecryptedAppKey();
        if (appKey == null || appKey.length() < 8) {
            return "***";
        }
        return appKey.substring(0, 4) + "***" + appKey.substring(appKey.length() - 4);
    }
}
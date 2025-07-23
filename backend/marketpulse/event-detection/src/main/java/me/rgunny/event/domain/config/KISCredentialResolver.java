package me.rgunny.event.domain.config;

import me.rgunny.event.infrastructure.adapter.output.properties.KISProperties;
import me.rgunny.marketpulse.common.security.CryptoService;
import org.springframework.stereotype.Component;

@Component
public class KISCredentialResolver {

    private final KISProperties props;
    private final CryptoService crypto;

    public KISCredentialResolver(KISProperties props, CryptoService crypto) {
        this.props = props;
        this.crypto = crypto;
    }

    public String getDecryptedAppKey() {
        return crypto.decrypt(props.appKey());
    }

    public String getMaskedAppKey() {
        return crypto.maskSensitiveValue(getDecryptedAppKey());
    }

    public boolean isEnabled() {
        return props.enabled();
    }

}

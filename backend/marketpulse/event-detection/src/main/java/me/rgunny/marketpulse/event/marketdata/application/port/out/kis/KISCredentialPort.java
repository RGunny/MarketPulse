package me.rgunny.marketpulse.event.marketdata.application.port.out.kis;

public interface KISCredentialPort {
    boolean isEnabled();
    String getBaseUrl();
    String getDecryptedAppKey();
    String getDecryptedAppSecret();
    String getMaskedAppKey();
}
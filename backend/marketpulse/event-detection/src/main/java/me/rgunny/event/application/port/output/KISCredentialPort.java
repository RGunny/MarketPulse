package me.rgunny.event.application.port.output;

public interface KISCredentialPort {
    boolean isEnabled();
    String getBaseUrl();
    String getDecryptedAppKey();
    String getDecryptedAppSecret();
    String getMaskedAppKey();
}

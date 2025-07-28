package me.rgunny.event.unit.infrastructure.adapter.output;

import me.rgunny.event.marketdata.application.port.out.KISCredentialPort;
import me.rgunny.event.marketdata.infrastructure.adapter.out.credential.KISCredentialResolverImpl;
import me.rgunny.event.marketdata.infrastructure.config.KISApiProperties;
import me.rgunny.marketpulse.common.security.CryptoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@DisplayName("KISCredentialResolver 단위 테스트")
@ExtendWith(MockitoExtension.class)
class KISCredentialResolverTest {

    @Mock
    private KISApiProperties properties;
    
    @Mock
    private CryptoService cryptoService;
    
    private KISCredentialPort credentialPort;

    @BeforeEach
    void setUp() {
        credentialPort = new KISCredentialResolverImpl(properties, cryptoService);
    }

    @Test
    @DisplayName("KIS API 활성화 여부를 반환한다")
    void shouldReturnEnabledStatus() {
        // given
        given(properties.enabled()).willReturn(true);

        // when
        boolean enabled = credentialPort.isEnabled();

        // then
        assertThat(enabled).isTrue();
    }

    @Test
    @DisplayName("기본 URL을 반환한다")
    void shouldReturnBaseUrl() {
        // given
        String expectedUrl = "https://openapi.koreainvestment.com:9443";
        given(properties.baseUrl()).willReturn(expectedUrl);

        // when
        String baseUrl = credentialPort.getBaseUrl();

        // then
        assertThat(baseUrl).isEqualTo(expectedUrl);
    }

    @Test
    @DisplayName("복호화된 App Key를 반환한다")
    void shouldReturnDecryptedAppKey() {
        // given
        String encryptedKey = "encrypted-app-key";
        String decryptedKey = "decrypted-app-key";
        given(properties.appKey()).willReturn(encryptedKey);
        given(cryptoService.decrypt(encryptedKey)).willReturn(decryptedKey);

        // when
        String appKey = credentialPort.getDecryptedAppKey();

        // then
        assertThat(appKey).isEqualTo(decryptedKey);
    }

    @Test
    @DisplayName("복호화된 App Secret을 반환한다")
    void shouldReturnDecryptedAppSecret() {
        // given
        String encryptedSecret = "encrypted-app-secret";
        String decryptedSecret = "decrypted-app-secret";
        given(properties.appSecret()).willReturn(encryptedSecret);
        given(cryptoService.decrypt(encryptedSecret)).willReturn(decryptedSecret);

        // when
        String appSecret = credentialPort.getDecryptedAppSecret();

        // then
        assertThat(appSecret).isEqualTo(decryptedSecret);
    }

    @Test
    @DisplayName("마스킹된 App Key를 반환한다")
    void shouldReturnMaskedAppKey() {
        // given
        String encryptedKey = "encrypted-app-key";
        String decryptedKey = "decrypted-app-key";
        String maskedKey = "dec****-key";
        given(properties.appKey()).willReturn(encryptedKey);
        given(cryptoService.decrypt(encryptedKey)).willReturn(decryptedKey);
        given(cryptoService.maskSensitiveValue(decryptedKey)).willReturn(maskedKey);

        // when
        String appKey = credentialPort.getMaskedAppKey();

        // then
        assertThat(appKey).isEqualTo(maskedKey);
    }
}
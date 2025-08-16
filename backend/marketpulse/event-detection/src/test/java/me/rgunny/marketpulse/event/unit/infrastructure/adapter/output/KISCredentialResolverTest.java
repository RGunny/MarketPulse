package me.rgunny.marketpulse.event.unit.infrastructure.adapter.output;

import me.rgunny.marketpulse.event.marketdata.application.port.out.kis.KISCredentialPort;
import me.rgunny.marketpulse.event.marketdata.infrastructure.adapter.out.kis.KISCredentialResolverImpl;
import me.rgunny.marketpulse.event.marketdata.infrastructure.config.kis.KISApiProperties;
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
    
    private KISCredentialPort credentialPort;

    @BeforeEach
    void setUp() {
        credentialPort = new KISCredentialResolverImpl(properties);
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
    @DisplayName("App Key를 반환한다 (Jasypt가 자동 복호화)")
    void shouldReturnDecryptedAppKey() {
        // given
        String appKeyValue = "decrypted-app-key";
        given(properties.appKey()).willReturn(appKeyValue);

        // when
        String appKey = credentialPort.getDecryptedAppKey();

        // then
        assertThat(appKey).isEqualTo(appKeyValue);
    }

    @Test
    @DisplayName("App Secret을 반환한다 (Jasypt가 자동 복호화)")
    void shouldReturnDecryptedAppSecret() {
        // given
        String appSecretValue = "decrypted-app-secret";
        given(properties.appSecret()).willReturn(appSecretValue);

        // when
        String appSecret = credentialPort.getDecryptedAppSecret();

        // then
        assertThat(appSecret).isEqualTo(appSecretValue);
    }

    @Test
    @DisplayName("마스킹된 App Key를 반환한다")
    void shouldReturnMaskedAppKey() {
        // given
        String appKeyValue = "decrypted-app-key";
        given(properties.appKey()).willReturn(appKeyValue);

        // when
        String maskedKey = credentialPort.getMaskedAppKey();

        // then
        assertThat(maskedKey).isEqualTo("decr***-key");
    }
}
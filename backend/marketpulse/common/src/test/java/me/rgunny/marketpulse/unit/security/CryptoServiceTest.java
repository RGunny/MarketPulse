package me.rgunny.marketpulse.unit.security;

import me.rgunny.marketpulse.common.security.CryptoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CryptoService 테스트 (unit)")
class CryptoServiceTest {

    private CryptoService cryptoService;
    private static final String TEST_MASTER_KEY = "a1b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef123456";

    @BeforeEach
    void setUp() {
        cryptoService = new CryptoService();
        ReflectionTestUtils.setField(cryptoService, "masterKeyHex", TEST_MASTER_KEY);
        cryptoService.init();
    }

    @Test
    @DisplayName("평문 암호화 후 복호화 시 원본 데이터가 복원된다")
    void givenPlainText_whenEncryptAndDecrypt_thenOriginalDataRestored() {
        // given
        String plainText = "my-secret-api-key";

        // when
        String encrypted = cryptoService.encrypt(plainText);
        String decrypted = cryptoService.decrypt(encrypted);

        // then
        assertThat(encrypted).startsWith("ENC(");
        assertThat(encrypted).endsWith(")");
        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    @DisplayName("평문 복호화 시 그대로 반환된다")
    void givenPlainText_whenDecrypt_thenReturnsSameValue() {
        // given
        String plainText = "plain-api-key";

        // when
        String result = cryptoService.decrypt(plainText);

        // then
        assertThat(result).isEqualTo(plainText);
    }

    @Test
    @DisplayName("null 값 암호화 시 null을 반환한다")
    void givenNullValue_whenEncrypt_thenReturnsNull() {
        // given
        String nullValue = null;

        // when
        String result = cryptoService.encrypt(nullValue);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("null 값 복호화 시 null을 반환한다")
    void givenNullValue_whenDecrypt_thenReturnsNull() {
        // given
        String nullValue = null;

        // when
        String result = cryptoService.decrypt(nullValue);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("민감한 값이 적절히 마스킹된다")
    void givenSensitiveValue_whenMask_thenProperlyMasked() {
        // given
        String apiKey = "ABCD1234EFGHIJKL";

        // when
        String masked = cryptoService.maskSensitiveValue(apiKey);

        // then
        assertThat(masked).isEqualTo("ABCD***IJKL");
    }
}

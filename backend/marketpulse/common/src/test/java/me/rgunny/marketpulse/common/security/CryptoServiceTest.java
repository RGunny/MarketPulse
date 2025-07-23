package me.rgunny.marketpulse.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

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
    @DisplayName("암호화-복호화 라운드트립")
    void encrypt_decrypt() {
        // Given
        String plainText = "my-secret-api-key";

        // When
        String encrypted = cryptoService.encrypt(plainText);
        String decrypted = cryptoService.decrypt(encrypted);

        // Then
        assertThat(encrypted).startsWith("ENC(");
        assertThat(encrypted).endsWith(")");
        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    @DisplayName("평문 복호화는 그대로 반환")
    void decrypt_plainText() {
        // Given
        String plainText = "plain-api-key";

        // When
        String result = cryptoService.decrypt(plainText);

        // Then
        assertThat(result).isEqualTo(plainText);
    }

    @Test
    @DisplayName("null 암호화")
    void encrypt_null() {
        // When
        String result = cryptoService.encrypt(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("null 복호화")
    void decrypt_null() {
        // When
        String result = cryptoService.decrypt(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("키 마스킹")
    void maskSensitiveValue() {
        // Given
        String apiKey = "ABCD1234EFGHIJKL";

        // When
        String masked = cryptoService.maskSensitiveValue(apiKey);

        // Then
        assertThat(masked).isEqualTo("ABCD***IJKL");
    }
}
package me.rgunny.marketpulse.common.infrastructure.security;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
@ConditionalOnProperty(
    prefix = "app.encryption",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = false  // 설정이 없으면 Bean 생성 안 함
)
public class CryptoService {

    // AES-256-GCM: 현재 가장 안전하고 빠른 대칭 암호화
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int TAG_LENGTH = 16;

    @Value("${app.encryption.master-key}")
    private String masterKeyHex;

    private SecretKeySpec secretKey;

    @PostConstruct
    public void init() {
        // Hex 문자열을 바이트 배열로 변환
        byte[] keyBytes = hexToBytes(masterKeyHex);
        if (keyBytes.length != 32) { // AES-256은 32바이트 키 필요
            throw new IllegalArgumentException("Master key must be 64 hex characters (32 bytes)");
        }
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * 문자열 암호화
     * @param plainText 평문
     * @return "ENC(" + Base64로 인코딩된 암호문 + ")"
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);

            // 랜덤 IV 생성
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom.getInstanceStrong().nextBytes(iv);

            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] encryptedData = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // IV + 암호화된 데이터 합치기
            byte[] result = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encryptedData, 0, result, iv.length, encryptedData.length);

            return "ENC(" + Base64.getEncoder().encodeToString(result) + ")";

        } catch (Exception e) {
            throw new SecurityException("Encryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * 문자열 복호화
     * @param encryptedValue "ENC(...)" 형태 또는 평문
     * @return 복호화된 평문
     */
    public String decrypt(String encryptedValue) {
        if (encryptedValue == null || encryptedValue.isEmpty()) {
            return encryptedValue;
        }

        // ENC()로 감싸져 있지 않으면 평문으로 간주
        if (!encryptedValue.startsWith("ENC(") || !encryptedValue.endsWith(")")) {
            return encryptedValue;
        }

        try {
            // "ENC(" 제거하고 ")" 제거
            String base64Data = encryptedValue.substring(4, encryptedValue.length() - 1);
            byte[] data = Base64.getDecoder().decode(base64Data);

            // IV와 암호화된 데이터 분리
            byte[] iv = Arrays.copyOf(data, GCM_IV_LENGTH);
            byte[] encryptedData = Arrays.copyOfRange(data, GCM_IV_LENGTH, data.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] decryptedData = cipher.doFinal(encryptedData);
            return new String(decryptedData, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new SecurityException("Decryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * 키 마스킹 (로그 출력용)
     */
    public String maskSensitiveValue(String value) {
        if (value == null || value.length() < 8) {
            return "***";
        }
        return value.substring(0, 4) + "***" + value.substring(value.length() - 4);
    }

    /**
     * Hex 문자열을 바이트 배열로 변환
     */
    private byte[] hexToBytes(String hex) {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have even length");
        }

        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            bytes[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return bytes;
    }

}

package com.connectprojects.backend.employees;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Component
public class SalaryEncryptionService {
    private static final String PREFIX = "v1:";
    private static final int NONCE_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final SecretKeySpec key;

    public SalaryEncryptionService(@Value("${app.salary.encryption-key:}") String encodedKey) {
        if (encodedKey == null || encodedKey.isBlank()) {
            throw new IllegalStateException("SALARY_ENCRYPTION_KEY must be configured.");
        }

        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(encodedKey.trim());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("SALARY_ENCRYPTION_KEY must be a Base64-encoded 32-byte key.", exception);
        }
        if (keyBytes.length != 32) {
            Arrays.fill(keyBytes, (byte) 0);
            throw new IllegalStateException("SALARY_ENCRYPTION_KEY must be a Base64-encoded 32-byte key.");
        }
        this.key = new SecretKeySpec(keyBytes, "AES");
        Arrays.fill(keyBytes, (byte) 0);
    }

    public String encrypt(BigDecimal value) {
        byte[] nonce = new byte[NONCE_LENGTH];
        RANDOM.nextBytes(nonce);
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, nonce));
            byte[] encrypted = cipher.doFinal(value.toPlainString().getBytes(StandardCharsets.UTF_8));
            byte[] payload = ByteBuffer.allocate(nonce.length + encrypted.length)
                    .put(nonce)
                    .put(encrypted)
                    .array();
            return PREFIX + Base64.getEncoder().encodeToString(payload);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Could not encrypt employee salary.", exception);
        }
    }

    public BigDecimal decrypt(String value) {
        if (value == null || !value.startsWith(PREFIX)) {
            throw new IllegalStateException("Employee salary data has an unsupported encryption format.");
        }

        try {
            byte[] payload = Base64.getDecoder().decode(value.substring(PREFIX.length()));
            if (payload.length <= NONCE_LENGTH) {
                throw new IllegalStateException("Employee salary data is incomplete.");
            }
            byte[] nonce = Arrays.copyOfRange(payload, 0, NONCE_LENGTH);
            byte[] encrypted = Arrays.copyOfRange(payload, NONCE_LENGTH, payload.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, nonce));
            byte[] plaintext = cipher.doFinal(encrypted);
            return new BigDecimal(new String(plaintext, StandardCharsets.UTF_8));
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "Could not decrypt employee salary; verify the encryption key and database integrity.",
                    exception);
        }
    }
}

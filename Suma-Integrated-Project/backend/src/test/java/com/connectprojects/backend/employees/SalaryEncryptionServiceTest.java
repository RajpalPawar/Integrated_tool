package com.connectprojects.backend.employees;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalaryEncryptionServiceTest {
    private final String key = Base64.getEncoder().encodeToString(new byte[32]);

    @Test
    void encryptsAndDecryptsSalaryWithFreshNonce() {
        SalaryEncryptionService encryption = new SalaryEncryptionService(key);
        BigDecimal salary = new BigDecimal("123456.78");

        String first = encryption.encrypt(salary);
        String second = encryption.encrypt(salary);

        assertNotEquals(salary.toPlainString(), first);
        assertNotEquals(first, second);
        assertEquals(salary, encryption.decrypt(first));
    }

    @Test
    void rejectsTamperedSalaryCiphertext() {
        SalaryEncryptionService encryption = new SalaryEncryptionService(key);
        String encrypted = encryption.encrypt(new BigDecimal("80000.00"));
        char replacement = encrypted.charAt(encrypted.length() - 3) == 'A' ? 'B' : 'A';
        String tampered = encrypted.substring(0, encrypted.length() - 3)
                + replacement
                + encrypted.substring(encrypted.length() - 2);

        assertThrows(IllegalStateException.class, () -> encryption.decrypt(tampered));
    }

    @Test
    void requiresAValidEncryptionKey() {
        assertThrows(IllegalStateException.class, () -> new SalaryEncryptionService(""));
        assertThrows(IllegalStateException.class,
                () -> new SalaryEncryptionService(Base64.getEncoder().encodeToString(new byte[16])));
    }
}

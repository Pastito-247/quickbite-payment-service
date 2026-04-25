package com.quickbite.payment_service.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class TokenizationService {

    @Value("${PAYMENT_ENCRYPTION_KEY:quickbite-payment-encryption-key-32-chars}")
    private String encryptionKey;

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    private SecretKey getSecretKey() {
        byte[] key = encryptionKey.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(key, ALGORITHM);
    }

    public String tokenizeSensitiveData(String data) {
        try {
            if (data == null || data.isEmpty()) {
                return null;
            }

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
            
            byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedData);
            
        } catch (Exception e) {
            log.error("Error tokenizing sensitive data: {}", e.getMessage());
            throw new RuntimeException("Failed to tokenize sensitive data", e);
        }
    }

    public String detokenizeSensitiveData(String token) {
        try {
            if (token == null || token.isEmpty()) {
                return null;
            }

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
            
            byte[] decodedData = Base64.getDecoder().decode(token);
            byte[] decryptedData = cipher.doFinal(decodedData);
            
            return new String(decryptedData, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            log.error("Error detokenizing sensitive data: {}", e.getMessage());
            throw new RuntimeException("Failed to detokenize sensitive data", e);
        }
    }

    public Map<String, String> tokenizePaymentDetails(Map<String, String> paymentDetails) {
        Map<String, String> tokenizedDetails = new HashMap<>();
        
        if (paymentDetails != null) {
            paymentDetails.forEach((key, value) -> {
                if (isSensitiveField(key)) {
                    tokenizedDetails.put(key, tokenizeSensitiveData(value));
                } else {
                    tokenizedDetails.put(key, value);
                }
            });
        }
        
        return tokenizedDetails;
    }

    public Map<String, String> detokenizePaymentDetails(Map<String, String> tokenizedDetails) {
        Map<String, String> decryptedDetails = new HashMap<>();
        
        if (tokenizedDetails != null) {
            tokenizedDetails.forEach((key, value) -> {
                if (isSensitiveField(key)) {
                    decryptedDetails.put(key, detokenizeSensitiveData(value));
                } else {
                    decryptedDetails.put(key, value);
                }
            });
        }
        
        return decryptedDetails;
    }

    private boolean isSensitiveField(String fieldName) {
        return fieldName != null && (
            fieldName.toLowerCase().contains("card") ||
            fieldName.toLowerCase().contains("cvv") ||
            fieldName.toLowerCase().contains("expiry") ||
            fieldName.toLowerCase().contains("account") ||
            fieldName.toLowerCase().contains("routing") ||
            fieldName.toLowerCase().contains("token") ||
            fieldName.toLowerCase().contains("secret")
        );
    }

    public String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] token = new byte[32];
        random.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }

    public boolean isValidTokenFormat(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        try {
            Base64.getDecoder().decode(token);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

package com.edumy.backend.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

public class PaymentSecurityUtil {

    private static final String HMAC_SHA512 = "HmacSHA512";
    private static final String HMAC_SHA256 = "HmacSHA256";

    /**
     * Compute HMAC SHA512 hex string
     */
    public static String hmacSHA512(String key, String data) {
        try {
            if (key == null || data == null) {
                return "";
            }
            Mac hmac512 = Mac.getInstance(HMAC_SHA512);
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA512);
            hmac512.init(secretKey);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder(2 * bytes.length);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error computing HMAC SHA512: " + ex.getMessage(), ex);
        }
    }

    /**
     * Compute HMAC SHA256 hex string (e.g. for MoMo or Stripe webhooks)
     */
    public static String hmacSHA256(String key, String data) {
        try {
            if (key == null || data == null) {
                return "";
            }
            Mac hmac256 = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            hmac256.init(secretKey);
            byte[] bytes = hmac256.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder(2 * bytes.length);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error computing HMAC SHA256: " + ex.getMessage(), ex);
        }
    }

    /**
     * Sort all parameters alphabetically, construct the hash data string, and calculate HMAC SHA512 (VNPay 2.1 standard)
     */
    public static String hashAllFields(Map<String, String> fields, String secretKey) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder sb = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && !fieldValue.trim().isEmpty() 
                    && !fieldName.equalsIgnoreCase("vnp_SecureHash") 
                    && !fieldName.equalsIgnoreCase("vnp_SecureHashType")) {
                try {
                    sb.append(fieldName);
                    sb.append("=");
                    sb.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (Exception e) {
                    sb.append(fieldValue);
                }
                if (itr.hasNext()) {
                    sb.append("&");
                }
            }
        }
        
        // Remove trailing & if present
        String hashData = sb.toString();
        if (hashData.endsWith("&")) {
            hashData = hashData.substring(0, hashData.length() - 1);
        }
        
        return hmacSHA512(secretKey, hashData);
    }

    /**
     * Verifies VNPay HMAC SHA512 signature from request parameters
     */
    public static boolean verifyChecksum(Map<String, String> fields, String expectedHash, String secretKey) {
        if (expectedHash == null || secretKey == null) {
            return false;
        }
        String calculatedHash = hashAllFields(fields, secretKey);
        return calculatedHash.equalsIgnoreCase(expectedHash);
    }
}

package com.edumy.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.key:ThisIsASecretKeyForJwtAuthenticationInEduMyProject_PleaseChangeInProduction}")
    private String jwtSecret;

    @Value("${jwt.access-ttl-minutes:60}")
    private long accessTtlMinutes;

    @Value("${jwt.expire-days:7}")
    private int expireDays;

    public long getAccessTtlMinutes() {
        return accessTtlMinutes;
    }

    public int getExpireDays() {
        return expireDays;
    }

    private Key getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            return Keys.hmacShaKeyFor(padded);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Integer userId, String email, String role) {
        return generateToken(userId, email, role, null);
    }

    public String generateToken(Integer userId, String email, String role, String fullName) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTtlMinutes * 60L * 1000L);

        var builder = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role);

        if (fullName != null && !fullName.isEmpty()) {
            builder.claim("name", fullName);
            builder.claim("unique_name", fullName);
        }

        return builder
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Integer getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Integer.parseInt(claims.getSubject());
        } catch (Exception ex) {
            return null;
        }
    }

    public String getRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("role", String.class);
        } catch (Exception ex) {
            return null;
        }
    }

    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}

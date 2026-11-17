package com.edumy.shared.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private static final String SECRET = "ThisIsASecretKeyForJwtAuthenticationInEduMyProject_PleaseChangeInProduction";

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(tokenProvider, "accessTtlMinutes", 60L);
        ReflectionTestUtils.setField(tokenProvider, "expireDays", 7);
    }

    @Test
    @DisplayName("Valid signed token passes validation and extracts claims")
    void validToken() {
        String token = tokenProvider.generateToken(42, "user@edumy.com", "Student", "Nguyen Van A");
        assertTrue(tokenProvider.validateToken(token));
        assertEquals(42, tokenProvider.getUserIdFromToken(token));
        assertEquals("Student", tokenProvider.getRoleFromToken(token));
    }

    @Test
    @DisplayName("Forged token with wrong signature is rejected")
    void forgedSignatureRejected() {
        String forged = Jwts.builder()
                .setSubject("1")
                .claim("role", "Admin")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor("AnotherCompletelyDifferentSecretKey123456".getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();

        assertFalse(tokenProvider.validateToken(forged));
        assertNull(tokenProvider.getUserIdFromToken(forged));
        assertNull(tokenProvider.getRoleFromToken(forged));
    }

    @Test
    @DisplayName("Unsigned mock header.payload.signature token is rejected")
    void unsignedMockTokenRejected() {
        String header = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
        String payload = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"sub\":\"1\",\"role\":\"Admin\"}".getBytes(StandardCharsets.UTF_8));
        String fakeToken = header + "." + payload + ".fakesignature";

        assertFalse(tokenProvider.validateToken(fakeToken));
        assertNull(tokenProvider.getUserIdFromToken(fakeToken));
        assertNull(tokenProvider.getRoleFromToken(fakeToken));
    }

    @Test
    @DisplayName("Expired token is rejected")
    void expiredTokenRejected() {
        String expired = Jwts.builder()
                .setSubject("1")
                .claim("role", "Student")
                .setIssuedAt(new Date(System.currentTimeMillis() - 120_000))
                .setExpiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();

        assertFalse(tokenProvider.validateToken(expired));
        assertNull(tokenProvider.getUserIdFromToken(expired));
        assertNull(tokenProvider.getRoleFromToken(expired));
    }
}

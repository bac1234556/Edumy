package com.edumy.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {
    private final String secret;
    private final String issuer;
    private final String audience;
    private final Duration lifetime;

    public JwtTokenService(
            @Value("${edumy.jwt.secret:}") String secret,
            @Value("${edumy.jwt.issuer:edumy}") String issuer,
            @Value("${edumy.jwt.audience:edumy-api}") String audience,
            @Value("${edumy.jwt.expire-minutes:15}") long expireMinutes) {
        this.secret = secret;
        this.issuer = issuer;
        this.audience = audience;
        this.lifetime = Duration.ofMinutes(expireMinutes);
    }

    @PostConstruct
    void validateConfiguration() {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("EDUMY_JWT_SECRET must contain at least 32 UTF-8 bytes.");
        }
        if (lifetime.isZero() || lifetime.isNegative()) {
            throw new IllegalStateException("JWT expiration must be a positive duration.");
        }
    }

    public String issueToken(String userId, String email, String fullName, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .claim("name", fullName)
                .claim("role", role)
                .issuer(issuer)
                .audience().add(audience).and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(lifetime)))
                .signWith(signingKey())
                .compact();
    }

    public AuthenticatedUser parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey())
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        if (!claims.getAudience().contains(audience)) {
            throw new IllegalArgumentException("JWT audience is invalid.");
        }

        return new AuthenticatedUser(
                claims.getSubject(),
                claims.get("email", String.class),
                claims.get("role", String.class));
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}

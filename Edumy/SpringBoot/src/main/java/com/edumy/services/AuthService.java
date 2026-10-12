package com.edumy.services;

import com.edumy.annotations.Loggable;
import com.edumy.annotations.Monitored;
import com.edumy.auth.AuthException;
import com.edumy.auth.GoogleIdentity;
import com.edumy.auth.GoogleTokenVerifier;
import com.edumy.auth.JwtTokenService;
import com.edumy.dto.AuthResponse;
import com.edumy.dto.ForgotPasswordRequest;
import com.edumy.dto.GoogleLoginRequest;
import com.edumy.dto.LoginRequest;
import com.edumy.dto.RegisterRequest;
import com.edumy.dto.ResetPasswordRequest;
import com.edumy.entities.RefreshToken;
import com.edumy.entities.User;
import com.edumy.repositories.RefreshTokenRepository;
import com.edumy.repositories.UserRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final boolean exposePasswordResetToken;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            GoogleTokenVerifier googleTokenVerifier,
            @Value("${edumy.auth.expose-reset-token:false}") boolean exposePasswordResetToken) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.googleTokenVerifier = googleTokenVerifier;
        this.exposePasswordResetToken = exposePasswordResetToken;
    }

    @Transactional
    @Loggable
    @Monitored("auth.register")
    public void register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new AuthException(HttpStatus.CONFLICT, "Email already in use.");
        }

        userRepository.save(new User(
                request.fullName().trim(),
                email,
                passwordEncoder.encode(request.password())));
    }

    @Transactional
    @Loggable
    @Monitored("auth.login")
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(request.email()))
                .filter(User::isActive)
                .filter(existing -> passwordEncoder.matches(request.password(), existing.getPasswordHash()))
                .orElseThrow(() -> new AuthException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));

        return issueSession(user, "Login successful.");
    }

    @Transactional
    @Loggable
    @Monitored("auth.google-login")
    public AuthResponse googleLogin(GoogleLoginRequest request) {
        GoogleIdentity identity = googleTokenVerifier.verify(request.token());
        String email = normalizeEmail(identity.email());
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(new User(
                        identity.name(),
                        email,
                        passwordEncoder.encode(generateRefreshToken()))));
        if (!user.isActive()) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Account is inactive.");
        }
        return issueSession(user, "Google login successful.");
    }

    @Transactional
    @Loggable
    @Monitored("auth.refresh")
    public AuthResponse refresh(String tokenValue) {
        if (tokenValue == null || tokenValue.isBlank()) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Token is required.");
        }

        RefreshToken currentToken = refreshTokenRepository.findByTokenWithUser(tokenValue)
                .orElseThrow(() -> new AuthException(HttpStatus.UNAUTHORIZED, "Invalid token."));
        Instant now = Instant.now();
        if (!currentToken.isActive(now) || !currentToken.getUser().isActive()) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Token is expired or revoked.");
        }

        String replacement = generateRefreshToken();
        currentToken.revoke(now, replacement);
        User user = currentToken.getUser();
        user.addRefreshToken(new RefreshToken(replacement, now.plus(7, ChronoUnit.DAYS)));
        return response(user, "Token refreshed successfully.", replacement);
    }

    @Transactional
    @Loggable
    @Monitored("auth.forgot-password")
    public Map<String, String> forgotPassword(ForgotPasswordRequest request) {
        return userRepository.findByEmail(normalizeEmail(request.email()))
                .map(user -> {
                    byte[] randomBytes = new byte[4];
                    SECURE_RANDOM.nextBytes(randomBytes);
                    String resetToken = HexFormat.of().formatHex(randomBytes).toUpperCase(Locale.ROOT);
                    user.setPasswordResetToken(resetToken, Instant.now().plus(15, ChronoUnit.MINUTES));
                    if (exposePasswordResetToken) {
                        return Map.of(
                                "message", "Password reset token generated.",
                                "resetToken", resetToken);
                    }
                    return Map.of("message", "If the email is registered, a password reset token has been generated.");
                })
                .orElseGet(() -> Map.of(
                        "message", "If the email is registered, a password reset token has been generated."));
    }

    @Transactional
    @Loggable
    @Monitored("auth.reset-password")
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(request.email()))
                .filter(existing -> request.token().equals(existing.getResetToken()))
                .filter(existing -> existing.getResetTokenExpiry() != null
                        && Instant.now().isBefore(existing.getResetTokenExpiry()))
                .orElseThrow(() -> new AuthException(
                        HttpStatus.BAD_REQUEST, "Invalid or expired password reset token."));

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.clearPasswordResetToken();
    }

    @Transactional
    @Loggable
    @Monitored("auth.revoke")
    public void revoke(String tokenValue) {
        if (tokenValue == null || tokenValue.isBlank()) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "Token is required.");
        }

        RefreshToken token = refreshTokenRepository.findByTokenWithUser(tokenValue)
                .orElseThrow(() -> new AuthException(HttpStatus.BAD_REQUEST, "Invalid token."));
        if (!token.isActive(Instant.now())) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "Token is already expired or revoked.");
        }
        token.revoke(Instant.now(), null);
    }

    private AuthResponse response(User user, String message, String refreshToken) {
        String jwt = jwtTokenService.issueToken(
                user.getUserId().toString(), user.getEmail(), user.getFullName(), user.getRole());
        return new AuthResponse(jwt, user.getEmail(), user.getFullName(), user.getRole(), message, refreshToken);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private AuthResponse issueSession(User user, String message) {
        String refreshToken = generateRefreshToken();
        RefreshToken entity = new RefreshToken(refreshToken, Instant.now().plus(7, ChronoUnit.DAYS));
        user.addRefreshToken(entity);
        refreshTokenRepository.save(entity);
        return response(user, message, refreshToken);
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[64];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

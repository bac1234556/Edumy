package com.edumy.controllers;

import com.edumy.dto.AuthResponse;
import com.edumy.dto.ForgotPasswordRequest;
import com.edumy.dto.GoogleLoginRequest;
import com.edumy.dto.LoginRequest;
import com.edumy.dto.RegisterRequest;
import com.edumy.dto.ResetPasswordRequest;
import com.edumy.services.AuthService;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final String REFRESH_COOKIE = "refreshToken";
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(Map.of("message", "User registered successfully."));
    }

    @PostMapping("/google-login")
    public ResponseEntity<AuthResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        AuthResponse response = authService.googleLogin(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie(response.refreshToken()).toString())
                .body(response.withoutRefreshToken());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie(response.refreshToken()).toString())
                .body(response.withoutRefreshToken());
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
        AuthResponse response = authService.refresh(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie(response.refreshToken()).toString())
                .body(response.withoutRefreshToken());
    }

    @PostMapping("/revoke-token")
    public ResponseEntity<Map<String, String>> revoke(
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
        authService.revoke(refreshToken);
        ResponseCookie clearedCookie = ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/api/auth")
                .maxAge(Duration.ZERO)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearedCookie.toString())
                .body(Map.of("message", "Token revoked."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of(
                "message", "Password reset successfully. You can now login with your new password."));
    }

    private ResponseCookie refreshCookie(String token) {
        return ResponseCookie.from(REFRESH_COOKIE, token)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/api/auth")
                .maxAge(Duration.ofDays(7))
                .build();
    }
}

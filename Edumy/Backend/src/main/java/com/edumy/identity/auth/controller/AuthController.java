package com.edumy.identity.auth.controller;

import com.edumy.identity.auth.dto.*;
import com.edumy.identity.auth.service.AuthService;
import com.edumy.identity.user.model.User;
import com.edumy.identity.user.repository.UserRepository;
import com.edumy.shared.exception.BusinessException;
import com.edumy.shared.exception.ErrorCode;
import com.edumy.shared.response.ApiResponse;
import com.edumy.shared.security.SecurityUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping({"/api/Auth", "/api/auth", "/api/v1/auth"})
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final Map<String, Instant> resendCooldowns = new ConcurrentHashMap<>();

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDto model) {
        return ResponseEntity.ok(authService.register(model));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginDto loginDto, HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(loginDto, response));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponseDto> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken, response));
    }

    @PostMapping("/revoke-token")
    public ResponseEntity<?> revokeToken(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        return ResponseEntity.ok(authService.revokeToken(refreshToken));
    }

    @PostMapping("/google-login")
    public ResponseEntity<AuthResponseDto> googleLogin(@RequestBody GoogleLoginDto googleLoginDto, HttpServletResponse response) {
        return ResponseEntity.ok(authService.googleLogin(googleLoginDto, response));
    }

    public record ResendVerificationRequest(String email) {}
    public record AuthMessageResponse(String message) {}

    @PostMapping("/forgot-password")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AuthMessageResponse.class)))
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordDto model) {
        return ResponseEntity.ok(authService.forgotPassword(model));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDto model) {
        return ResponseEntity.ok(authService.resetPassword(model));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmailGet(@RequestParam("token") String token) {
        return ResponseEntity.ok(authService.verifyEmail(token));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmailPost(@RequestBody java.util.Map<String, String> body) {
        String token = body.getOrDefault("token", body.get("code"));
        return ResponseEntity.ok(authService.verifyEmail(token));
    }

    @PostMapping({"/resend-verification", "/verification-email/resend"})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AuthMessageResponse.class)))
    public ResponseEntity<?> resendVerification(@RequestBody(required = false) ResendVerificationRequest body) {
        String email = body != null ? body.email() : null;
        if (email == null || email.isBlank()) {
            Integer userId = SecurityUtils.getCurrentUserId();
            if (userId != null) {
                User u = userRepository.findById(userId).orElse(null);
                if (u != null) email = u.getEmail();
            }
        }
        if (email == null || email.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Vui lòng cung cấp địa chỉ email.");
        }

        String key = email.trim().toLowerCase();
        Instant now = Instant.now();
        Instant lastSent = resendCooldowns.get(key);
        if (lastSent != null) {
            long elapsed = Duration.between(lastSent, now).getSeconds();
            if (elapsed < 60) {
                long retryAfter = 60 - elapsed;
                Map<String, Object> errors = new LinkedHashMap<>();
                errors.put("code", "EMAIL_RESEND_TOO_SOON");
                errors.put("fields", null);
                errors.put("traceId", UUID.randomUUID().toString());
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .header(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfter))
                        .body(ApiResponse.error("Vui lòng chờ " + retryAfter + " giây trước khi gửi lại email xác thực.", errors));
            }
        }

        Map<String, Object> res = authService.resendVerification(key);
        resendCooldowns.put(key, now);
        return ResponseEntity.ok(res);
    }
}

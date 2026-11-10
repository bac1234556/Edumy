package com.edumy.modules.auth.controller;

import com.edumy.modules.auth.dto.*;
import com.edumy.modules.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/Auth", "/api/auth", "/api/v1/auth"})
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDto model) {
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

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordDto model) {
        return ResponseEntity.ok(authService.forgotPassword(model));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDto model) {
        return ResponseEntity.ok(authService.resetPassword(model));
    }
}

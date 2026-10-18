package com.edumy.backend.controller;

import com.edumy.backend.model.*;
import com.edumy.backend.dto.*;
import com.edumy.backend.repository.*;
import com.edumy.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDateTime;
import java.security.SecureRandom;
import java.util.Base64;

@RestController
@RequestMapping({"/api/Auth", "/api/auth"})
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private com.edumy.backend.security.JwtTokenProvider jwtTokenProvider;

    @Autowired
    private com.edumy.backend.service.UserActivityService userActivityService;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDto model) {
        if (userRepository.existsByEmail(model.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already in use."));
        }

        User user = new User();
        user.setFullName(model.getFullName());
        user.setEmail(model.getEmail());
        user.setRole(model.getRole() == null || model.getRole().isEmpty() ? "Student" : model.getRole());
        user.setPasswordHash(passwordEncoder.encode(model.getPassword()));
        
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "User registered successfully."));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto, HttpServletResponse response) {
        User user = userRepository.findByEmail(loginDto.getEmail());
        if (user == null || !passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid email or password."));
        }

        String token = generateJwtToken(user);
        String refreshTokenStr = generateRefreshToken();
        
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenStr);
        refreshToken.setUserId(user.getUserId());
        refreshToken.setExpires(LocalDateTime.now().plusDays(7));
        refreshToken.setCreated(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);

        setTokenCookie(response, refreshTokenStr);

        AuthResponseDto authResponse = new AuthResponseDto();
        authResponse.setToken(token);
        authResponse.setEmail(user.getEmail());
        authResponse.setFullName(user.getFullName());
        authResponse.setRole(user.getRole());
        authResponse.setMessage("Login successful.");

        userActivityService.logActivity(user.getUserId(), "LOGIN_PASSWORD", "AUTH");

        return ResponseEntity.ok(authResponse);
    }
    
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@CookieValue(name = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Token is required."));
        }

        RefreshToken refreshTokenEntity = refreshTokenRepository.findByToken(refreshToken);
        if (refreshTokenEntity == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid token."));
        }

        User user = refreshTokenEntity.getUser();

        if (!refreshTokenEntity.isActive()) {
            return ResponseEntity.status(401).body(Map.of("message", "Token is expired or revoked."));
        }

        refreshTokenEntity.setRevoked(LocalDateTime.now());
        
        String newJwtToken = generateJwtToken(user);
        String newRefreshTokenStr = generateRefreshToken();
        
        refreshTokenEntity.setReplacedByToken(newRefreshTokenStr);
        refreshTokenRepository.save(refreshTokenEntity);
        
        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setToken(newRefreshTokenStr);
        newRefreshToken.setUserId(user.getUserId());
        newRefreshToken.setExpires(LocalDateTime.now().plusDays(7));
        newRefreshToken.setCreated(LocalDateTime.now());
        refreshTokenRepository.save(newRefreshToken);
        
        setTokenCookie(response, newRefreshTokenStr);

        AuthResponseDto authResponse = new AuthResponseDto();
        authResponse.setToken(newJwtToken);
        authResponse.setEmail(user.getEmail());
        authResponse.setFullName(user.getFullName());
        authResponse.setRole(user.getRole());
        authResponse.setMessage("Token refreshed successfully.");

        return ResponseEntity.ok(authResponse);
    }
    
    @PostMapping("/revoke-token")
    public ResponseEntity<?> revokeToken(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token is required."));
        }

        RefreshToken refreshTokenEntity = refreshTokenRepository.findByToken(refreshToken);
        if (refreshTokenEntity == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid token."));
        }

        if (!refreshTokenEntity.isActive()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token is already expired or revoked."));
        }

        refreshTokenEntity.setRevoked(LocalDateTime.now());
        refreshTokenRepository.save(refreshTokenEntity);

        return ResponseEntity.ok(Map.of("message", "Token revoked."));
    }

    @PostMapping("/google-login")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginDto googleLoginDto, HttpServletResponse response) {
        try {
            String email = "google@example.com"; 
            String name = "Google User"; 
            String avatarUrl = null;

            if (googleLoginDto != null && googleLoginDto.getToken() != null && googleLoginDto.getToken().contains(".")) {
                try {
                    String[] parts = googleLoginDto.getToken().split("\\.");
                    if (parts.length >= 2) {
                        String payload = parts[1];
                        int rem = payload.length() % 4;
                        if (rem == 2) payload += "==";
                        else if (rem == 3) payload += "=";
                        byte[] decodedBytes = java.util.Base64.getUrlDecoder().decode(payload);
                        String payloadJson = new String(decodedBytes, java.nio.charset.StandardCharsets.UTF_8);
                        com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(payloadJson);
                        if (node.has("email") && !node.get("email").asText().isEmpty()) {
                            email = node.get("email").asText();
                        }
                        if (node.has("name") && !node.get("name").asText().isEmpty()) {
                            name = node.get("name").asText();
                        }
                        if (node.has("picture") && !node.get("picture").asText().isEmpty()) {
                            avatarUrl = node.get("picture").asText();
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("Google token payload parsing failed: " + ex.getMessage());
                }
            }

            User user = userRepository.findByEmail(email);
            if (user == null) {
                user = new User();
                user.setFullName(name);
                user.setEmail(email);
                user.setPasswordHash("");
                user.setRole("Student");
                user.setAvatarUrl(avatarUrl);
                user.setProvider("Google");
                user.setCreatedAt(LocalDateTime.now());
                user = userRepository.save(user);
            } else {
                boolean updated = false;
                if (name != null && !name.isEmpty() && !name.equals("Google User") && !name.equals(user.getFullName())) {
                    user.setFullName(name);
                    updated = true;
                }
                if (avatarUrl != null && !avatarUrl.isEmpty() && !avatarUrl.equals(user.getAvatarUrl())) {
                    user.setAvatarUrl(avatarUrl);
                    updated = true;
                }
                if (updated) {
                    user = userRepository.save(user);
                }
            }

            String token = generateJwtToken(user);
            String refreshTokenStr = generateRefreshToken();
            
            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setToken(refreshTokenStr);
            refreshToken.setUserId(user.getUserId());
            refreshToken.setExpires(LocalDateTime.now().plusDays(7));
            refreshToken.setCreated(LocalDateTime.now());
            refreshTokenRepository.save(refreshToken);
            
            setTokenCookie(response, refreshTokenStr);

            AuthResponseDto authResponse = new AuthResponseDto();
            authResponse.setToken(token);
            authResponse.setEmail(user.getEmail());
            authResponse.setFullName(user.getFullName());
            authResponse.setRole(user.getRole());
            authResponse.setMessage("Google login successful.");

            userActivityService.logActivity(user.getUserId(), "LOGIN_GOOGLE", "AUTH");

            return ResponseEntity.ok(authResponse);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("message", "Google token validation failed.", "error", ex.getMessage()));
        }
    }

    private String generateJwtToken(User user) {
        return jwtTokenProvider.generateToken(user.getUserId(), user.getEmail(), user.getRole(), user.getFullName());
    }
    
    private String generateRefreshToken() {
        byte[] randomBytes = new byte[64];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getEncoder().encodeToString(randomBytes);
    }
    
    private void setTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("refreshToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordDto model) {
        User user = userRepository.findByEmail(model.getEmail());
        if (user == null) {
            return ResponseEntity.ok(Map.of("message", "If the email is registered, a password reset token has been generated."));
        }

        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password reset token generated.", "resetToken", token));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDto model) {
        User user = userRepository.findByEmail(model.getEmail());
        if (user == null || user.getResetToken() == null || !user.getResetToken().equals(model.getToken()) || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired password reset token."));
        }

        user.setPasswordHash(passwordEncoder.encode(model.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password reset successfully. You can now login with your new password."));
    }
}

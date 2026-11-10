package com.edumy.modules.auth.service.impl;

import com.edumy.common.exception.BusinessException;
import com.edumy.common.exception.ErrorCode;
import com.edumy.common.security.JwtTokenProvider;
import com.edumy.modules.auth.dto.*;
import com.edumy.modules.auth.model.RefreshToken;
import com.edumy.modules.auth.repository.RefreshTokenRepository;
import com.edumy.modules.auth.service.AuthService;
import com.edumy.modules.user.model.User;
import com.edumy.modules.user.repository.UserRepository;
import com.edumy.modules.user.service.UserActivityService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserActivityService userActivityService;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           JwtTokenProvider jwtTokenProvider,
                           UserActivityService userActivityService,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userActivityService = userActivityService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Map<String, Object> register(RegisterDto model) {
        if (userRepository.existsByEmail(model.getEmail())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Email already in use.");
        }

        User user = new User();
        user.setFullName(model.getFullName());
        user.setEmail(model.getEmail());
        user.setRole(model.getRole() == null || model.getRole().isEmpty() ? "Student" : model.getRole());
        user.setPasswordHash(passwordEncoder.encode(model.getPassword()));

        userRepository.save(user);
        return Map.of("message", "User registered successfully.");
    }

    @Override
    public AuthResponseDto login(LoginDto loginDto, HttpServletResponse response) {
        User user = userRepository.findByEmail(loginDto.getEmail());
        if (user == null || !passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid email or password.");
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
        return authResponse;
    }

    @Override
    public AuthResponseDto refreshToken(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Token is required.");
        }

        RefreshToken refreshTokenEntity = refreshTokenRepository.findByToken(refreshToken);
        if (refreshTokenEntity == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid token.");
        }

        User user = refreshTokenEntity.getUser();
        if (!refreshTokenEntity.isActive()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Token is expired or revoked.");
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
        return authResponse;
    }

    @Override
    public Map<String, Object> revokeToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Token is required.");
        }

        RefreshToken refreshTokenEntity = refreshTokenRepository.findByToken(refreshToken);
        if (refreshTokenEntity == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Invalid token.");
        }

        if (!refreshTokenEntity.isActive()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Token is already expired or revoked.");
        }

        refreshTokenEntity.setRevoked(LocalDateTime.now());
        refreshTokenRepository.save(refreshTokenEntity);

        return Map.of("message", "Token revoked.");
    }

    @Override
    public AuthResponseDto googleLogin(GoogleLoginDto googleLoginDto, HttpServletResponse response) {
        if (googleLoginDto == null || googleLoginDto.getToken() == null || !googleLoginDto.getToken().contains(".")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Thiếu Google OAuth token hợp lệ.");
        }

        String email = null;
        String name = null;
        String avatarUrl = null;

        try {
            String[] parts = googleLoginDto.getToken().split("\\.");
            if (parts.length >= 2) {
                String payload = parts[1];
                int rem = payload.length() % 4;
                if (rem == 2) payload += "==";
                else if (rem == 3) payload += "=";
                byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
                String payloadJson = new String(decodedBytes, StandardCharsets.UTF_8);
                JsonNode node = new ObjectMapper().readTree(payloadJson);
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
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Token Google không hợp lệ hoặc giải mã thất bại: " + ex.getMessage());
        }

        if (email == null || email.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Không trích xuất được email từ Google token.");
        }

        if (name == null || name.trim().isEmpty()) {
            name = email.split("@")[0];
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
            if (!name.isEmpty() && !name.equals("Google User") && !name.equals(user.getFullName())) {
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
        return authResponse;
    }

    @Override
    public Map<String, Object> forgotPassword(ForgotPasswordDto model) {
        User user = userRepository.findByEmail(model.getEmail());
        if (user == null) {
            return Map.of("message", "If the email is registered, a password reset token has been generated.");
        }

        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        return Map.of("message", "Password reset token generated.", "resetToken", token);
    }

    @Override
    public Map<String, Object> resetPassword(ResetPasswordDto model) {
        User user = userRepository.findByEmail(model.getEmail());
        if (user == null || user.getResetToken() == null || !user.getResetToken().equals(model.getToken()) || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Invalid or expired password reset token.");
        }

        user.setPasswordHash(passwordEncoder.encode(model.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        return Map.of("message", "Password reset successfully. You can now login with your new password.");
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
}

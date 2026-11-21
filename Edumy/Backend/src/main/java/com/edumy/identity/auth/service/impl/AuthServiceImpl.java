package com.edumy.identity.auth.service.impl;

import com.edumy.shared.exception.BusinessException;
import com.edumy.shared.exception.ErrorCode;
import com.edumy.shared.security.JwtTokenProvider;
import com.edumy.identity.auth.dto.*;
import com.edumy.identity.auth.model.RefreshToken;
import com.edumy.identity.auth.repository.RefreshTokenRepository;
import com.edumy.identity.auth.service.AuthService;
import com.edumy.identity.user.model.User;
import com.edumy.identity.user.repository.UserRepository;
import com.edumy.identity.user.service.UserActivityService;
import com.edumy.notification.service.MailService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    private static final long LOGIN_LOCK_MINUTES = 15;
    private static final String NEUTRAL_FORGOT_PASSWORD_MESSAGE =
            "Nếu địa chỉ email tồn tại trong hệ thống, hướng dẫn đặt lại mật khẩu đã được gửi.";

    private static final class LoginAttemptState {
        int failedCount;
        Instant windowStart;
        Instant lockedUntil;
    }

    private final Map<String, LoginAttemptState> loginAttempts = new ConcurrentHashMap<>();

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserActivityService userActivityService;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    public AuthServiceImpl(UserRepository userRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           JwtTokenProvider jwtTokenProvider,
                           UserActivityService userActivityService,
                           PasswordEncoder passwordEncoder,
                           MailService mailService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userActivityService = userActivityService;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    @Override
    public Map<String, Object> register(RegisterDto model) {
        if (userRepository.existsByEmail(model.getEmail())) {
            throw new BusinessException(HttpStatus.CONFLICT, "AUTH_EMAIL_TAKEN", "Email đã được sử dụng.");
        }

        User user = new User();
        user.setFullName(model.getFullName());
        user.setEmail(model.getEmail());
        user.setRole("Student");
        user.setPasswordHash(passwordEncoder.encode(model.getPassword()));

        String verificationCode = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiry(LocalDateTime.now().plusHours(24));
        user.setIsEmailVerified(false);

        userRepository.save(user);

        // Send email verification asynchronously via Mailpit
        mailService.sendEmailVerification(user.getEmail(), user.getFullName(), verificationCode);

        return Map.of("message", "Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.");
    }

    @Override
    public AuthResponseDto login(LoginDto loginDto, HttpServletResponse response) {
        String emailKey = normalizeEmailKey(loginDto != null ? loginDto.getEmail() : null);
        checkLoginRateLimit(emailKey);

        User user = userRepository.findByEmail(loginDto.getEmail());
        if (user == null || !passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())) {
            recordFailedLogin(emailKey);
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_CREDENTIALS", "Email hoặc mật khẩu không chính xác.");
        }

        clearFailedLogin(emailKey);

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "AUTH_ACCOUNT_LOCKED", "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.");
        }

        String token = generateJwtToken(user);
        String refreshTokenStr = generateRefreshToken();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenStr);
        refreshToken.setUserId(user.getUserId());
        refreshToken.setFamilyId(UUID.randomUUID().toString());
        refreshToken.setClient("WEB");
        refreshToken.setExpires(LocalDateTime.now().plusDays(jwtTokenProvider.getExpireDays()));
        refreshToken.setCreated(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);

        setTokenCookie(response, refreshTokenStr);

        AuthResponseDto authResponse = new AuthResponseDto();
        authResponse.setToken(token);
        authResponse.setEmail(user.getEmail());
        authResponse.setFullName(user.getFullName());
        authResponse.setRole(user.getRole());
        authResponse.setMessage("Đăng nhập thành công.");

        userActivityService.logActivity(user.getUserId(), "LOGIN_PASSWORD", "AUTH");
        return authResponse;
    }

    @Override
    public AuthResponseDto refreshToken(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_REFRESH_INVALID", "Refresh token không hợp lệ.");
        }

        RefreshToken refreshTokenEntity = refreshTokenRepository.findByToken(refreshToken);
        if (refreshTokenEntity == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_REFRESH_INVALID", "Refresh token không hợp lệ.");
        }

        if (refreshTokenEntity.isRevoked() || refreshTokenEntity.getReplacedByToken() != null) {
            revokeFamilyTokens(refreshTokenEntity.getFamilyId());
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_REFRESH_REUSED", "Phát hiện sử dụng lại refresh token. Phiên đăng nhập đã bị thu hồi.");
        }

        if (refreshTokenEntity.isExpired()) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_REFRESH_INVALID", "Refresh token đã hết hạn.");
        }

        User user = refreshTokenEntity.getUser();
        if (user == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_REFRESH_INVALID", "Refresh token không hợp lệ.");
        }
        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "AUTH_ACCOUNT_LOCKED", "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.");
        }

        refreshTokenEntity.setRevoked(LocalDateTime.now());
        String newJwtToken = generateJwtToken(user);
        String newRefreshTokenStr = generateRefreshToken();

        refreshTokenEntity.setReplacedByToken(newRefreshTokenStr);
        refreshTokenRepository.save(refreshTokenEntity);

        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setToken(newRefreshTokenStr);
        newRefreshToken.setUserId(user.getUserId());
        newRefreshToken.setFamilyId(refreshTokenEntity.getFamilyId() != null ? refreshTokenEntity.getFamilyId() : UUID.randomUUID().toString());
        newRefreshToken.setClient(refreshTokenEntity.getClient() != null ? refreshTokenEntity.getClient() : "WEB");
        newRefreshToken.setExpires(LocalDateTime.now().plusDays(jwtTokenProvider.getExpireDays()));
        newRefreshToken.setCreated(LocalDateTime.now());
        refreshTokenRepository.save(newRefreshToken);

        setTokenCookie(response, newRefreshTokenStr);

        AuthResponseDto authResponse = new AuthResponseDto();
        authResponse.setToken(newJwtToken);
        authResponse.setEmail(user.getEmail());
        authResponse.setFullName(user.getFullName());
        authResponse.setRole(user.getRole());
        authResponse.setMessage("Làm mới phiên đăng nhập thành công.");
        return authResponse;
    }

    @Override
    public Map<String, Object> revokeToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            return Map.of("message", "Token revoked.");
        }

        RefreshToken refreshTokenEntity = refreshTokenRepository.findByToken(refreshToken);
        if (refreshTokenEntity != null && refreshTokenEntity.isActive()) {
            refreshTokenEntity.setRevoked(LocalDateTime.now());
            refreshTokenRepository.save(refreshTokenEntity);
        }

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

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "AUTH_ACCOUNT_LOCKED", "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.");
        }

        String token = generateJwtToken(user);
        String refreshTokenStr = generateRefreshToken();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenStr);
        refreshToken.setUserId(user.getUserId());
        refreshToken.setFamilyId(UUID.randomUUID().toString());
        refreshToken.setClient("WEB");
        refreshToken.setExpires(LocalDateTime.now().plusDays(jwtTokenProvider.getExpireDays()));
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
        User user = model != null && model.getEmail() != null ? userRepository.findByEmail(model.getEmail()) : null;
        if (user == null) {
            return Map.of("message", NEUTRAL_FORGOT_PASSWORD_MESSAGE);
        }

        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        user.setResetToken(sha256Hex(token));
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(30));
        userRepository.save(user);

        // Send password reset email via Mailpit (raw code only in email, SHA-256 hash stored in DB)
        mailService.sendPasswordReset(user.getEmail(), user.getFullName(), token);

        return Map.of("message", NEUTRAL_FORGOT_PASSWORD_MESSAGE);
    }

    @Override
    public Map<String, Object> resetPassword(ResetPasswordDto model) {
        User user = model != null && model.getEmail() != null ? userRepository.findByEmail(model.getEmail()) : null;
        String providedToken = model != null ? model.getToken() : null;
        String hashedToken = providedToken != null ? sha256Hex(providedToken) : null;
        if (user == null || user.getResetToken() == null || hashedToken == null
                || !user.getResetToken().equals(hashedToken)
                || user.getResetTokenExpiry() == null
                || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Mã đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.");
        }

        user.setPasswordHash(passwordEncoder.encode(model.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        List<RefreshToken> allTokens = refreshTokenRepository.findByUserId(user.getUserId());
        if (allTokens != null && !allTokens.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            for (RefreshToken t : allTokens) {
                if (t.isActive()) {
                    t.setRevoked(now);
                }
            }
            refreshTokenRepository.saveAll(allTokens);
        }
        clearFailedLogin(normalizeEmailKey(user.getEmail()));

        // Send password changed notification
        mailService.sendPasswordChanged(user.getEmail(), user.getFullName(), java.time.Instant.now());

        return Map.of("message", "Mật khẩu đã được thay đổi thành công. Bạn có thể đăng nhập ngay.");
    }

    @Override
    public Map<String, Object> verifyEmail(String token) {
        User user = userRepository.findAll().stream()
                .filter(u -> token != null && token.equals(u.getVerificationCode()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "Mã xác thực email không hợp lệ hoặc không tồn tại."));

        if (user.getVerificationCodeExpiry() != null && user.getVerificationCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Mã xác thực email đã hết hạn. Vui lòng yêu cầu gửi lại.");
        }

        user.setIsEmailVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);
        userRepository.save(user);

        return Map.of("message", "Xác thực email thành công! Tài khoản của bạn đã sẵn sàng sử dụng.");
    }

    @Override
    public Map<String, Object> resendVerification(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy người dùng với email này.");
        }
        if (Boolean.TRUE.equals(user.getIsEmailVerified())) {
            return Map.of("message", "Tài khoản này đã được xác thực trước đó.");
        }

        String verificationCode = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        mailService.sendEmailVerification(user.getEmail(), user.getFullName(), verificationCode);

        return Map.of("message", "Thư xác thực mới đã được gửi đến email của bạn.");
    }

    @Override
    @Transactional
    public MobileTokensDto mobileRegister(MobileRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(HttpStatus.CONFLICT, "AUTH_EMAIL_TAKEN", "Email đã được sử dụng.");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setRole("Student");
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(true);
        user.setIsEmailVerified(false);
        String verificationCode = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiry(LocalDateTime.now().plusHours(24));
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        mailService.sendEmailVerification(user.getEmail(), user.getFullName(), verificationCode);

        String jwtToken = generateJwtToken(user);
        String refreshTokenStr = generateRefreshToken();
        int refreshDays = jwtTokenProvider.getExpireDays() > 0 ? jwtTokenProvider.getExpireDays() : 7;
        long accessMinutes = jwtTokenProvider.getAccessTtlMinutes() > 0 ? jwtTokenProvider.getAccessTtlMinutes() : 60;

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenStr);
        refreshToken.setUserId(user.getUserId());
        refreshToken.setFamilyId(UUID.randomUUID().toString());
        refreshToken.setClient("MOBILE");
        refreshToken.setExpires(LocalDateTime.now().plusDays(refreshDays));
        refreshToken.setCreated(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);

        userActivityService.logActivity(user.getUserId(), "MOBILE_REGISTER", "AUTH");

        return MobileTokensDto.builder()
                .accessToken(jwtToken)
                .expiresAt(Instant.now().plus(accessMinutes, ChronoUnit.MINUTES).toString())
                .refreshToken(refreshTokenStr)
                .refreshExpiresAt(Instant.now().plus(refreshDays, ChronoUnit.DAYS).toString())
                .build();
    }

    @Override
    @Transactional
    public MobileTokensDto mobileLogin(MobileLoginRequest request) {
        String emailKey = normalizeEmailKey(request != null ? request.getEmail() : null);
        checkLoginRateLimit(emailKey);

        User user = userRepository.findByEmail(request.getEmail());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            recordFailedLogin(emailKey);
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_CREDENTIALS", "Email hoặc mật khẩu không chính xác.");
        }

        clearFailedLogin(emailKey);

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "AUTH_ACCOUNT_LOCKED", "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.");
        }

        String jwtToken = generateJwtToken(user);
        String refreshTokenStr = generateRefreshToken();
        int refreshDays = jwtTokenProvider.getExpireDays() > 0 ? jwtTokenProvider.getExpireDays() : 7;
        long accessMinutes = jwtTokenProvider.getAccessTtlMinutes() > 0 ? jwtTokenProvider.getAccessTtlMinutes() : 60;

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenStr);
        refreshToken.setUserId(user.getUserId());
        refreshToken.setFamilyId(UUID.randomUUID().toString());
        refreshToken.setClient("MOBILE");
        refreshToken.setExpires(LocalDateTime.now().plusDays(refreshDays));
        refreshToken.setCreated(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);

        userActivityService.logActivity(user.getUserId(), "MOBILE_LOGIN", "AUTH");

        return MobileTokensDto.builder()
                .accessToken(jwtToken)
                .expiresAt(Instant.now().plus(accessMinutes, ChronoUnit.MINUTES).toString())
                .refreshToken(refreshTokenStr)
                .refreshExpiresAt(Instant.now().plus(refreshDays, ChronoUnit.DAYS).toString())
                .build();
    }

    @Override
    @Transactional
    public MobileTokensDto mobileRefresh(MobileRefreshRequest request) {
        if (request == null || request.getRefreshToken() == null || request.getRefreshToken().trim().isEmpty()) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_REFRESH_INVALID", "Refresh token không hợp lệ.");
        }

        String tokenStr = request.getRefreshToken().trim();
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByToken(tokenStr);
        if (refreshTokenEntity == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_REFRESH_INVALID", "Refresh token không hợp lệ.");
        }

        // Token reuse detection: revoke only this token family (never affect other clients/sessions)
        if (refreshTokenEntity.isRevoked() || refreshTokenEntity.getReplacedByToken() != null) {
            revokeFamilyTokens(refreshTokenEntity.getFamilyId());
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_REFRESH_REUSED", "Phát hiện sử dụng lại refresh token. Phiên đăng nhập đã bị thu hồi.");
        }

        if (refreshTokenEntity.isExpired()) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_REFRESH_INVALID", "Refresh token đã hết hạn.");
        }

        User user = refreshTokenEntity.getUser();
        if (user == null || Boolean.FALSE.equals(user.getIsActive())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "AUTH_ACCOUNT_LOCKED", "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.");
        }

        int refreshDays = jwtTokenProvider.getExpireDays() > 0 ? jwtTokenProvider.getExpireDays() : 7;
        long accessMinutes = jwtTokenProvider.getAccessTtlMinutes() > 0 ? jwtTokenProvider.getAccessTtlMinutes() : 60;

        // Rotate token within same family
        refreshTokenEntity.setRevoked(LocalDateTime.now());
        String newJwtToken = generateJwtToken(user);
        String newRefreshTokenStr = generateRefreshToken();

        refreshTokenEntity.setReplacedByToken(newRefreshTokenStr);
        refreshTokenRepository.save(refreshTokenEntity);

        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setToken(newRefreshTokenStr);
        newRefreshToken.setUserId(user.getUserId());
        newRefreshToken.setFamilyId(refreshTokenEntity.getFamilyId() != null ? refreshTokenEntity.getFamilyId() : UUID.randomUUID().toString());
        newRefreshToken.setClient(refreshTokenEntity.getClient() != null ? refreshTokenEntity.getClient() : "MOBILE");
        newRefreshToken.setExpires(LocalDateTime.now().plusDays(refreshDays));
        newRefreshToken.setCreated(LocalDateTime.now());
        refreshTokenRepository.save(newRefreshToken);

        return MobileTokensDto.builder()
                .accessToken(newJwtToken)
                .expiresAt(Instant.now().plus(accessMinutes, ChronoUnit.MINUTES).toString())
                .refreshToken(newRefreshTokenStr)
                .refreshExpiresAt(Instant.now().plus(refreshDays, ChronoUnit.DAYS).toString())
                .build();
    }

    private void revokeFamilyTokens(String familyId) {
        if (familyId == null || familyId.isBlank()) {
            return;
        }
        List<RefreshToken> familyTokens = refreshTokenRepository.findByFamilyId(familyId);
        LocalDateTime now = LocalDateTime.now();
        for (RefreshToken t : familyTokens) {
            if (t.isActive()) {
                t.setRevoked(now);
            }
        }
        refreshTokenRepository.saveAll(familyTokens);
    }

    @Override
    @Transactional
    public void mobileLogout(MobileLogoutRequest request) {
        if (request == null || request.getRefreshToken() == null || request.getRefreshToken().trim().isEmpty()) {
            return;
        }

        RefreshToken refreshTokenEntity = refreshTokenRepository.findByToken(request.getRefreshToken().trim());
        if (refreshTokenEntity != null && refreshTokenEntity.isActive()) {
            refreshTokenEntity.setRevoked(LocalDateTime.now());
            refreshTokenRepository.save(refreshTokenEntity);
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

    private String normalizeEmailKey(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private void checkLoginRateLimit(String emailKey) {
        if (emailKey.isEmpty()) return;
        LoginAttemptState state = loginAttempts.get(emailKey);
        if (state == null) return;
        synchronized (state) {
            if (state.lockedUntil != null && Instant.now().isBefore(state.lockedUntil)) {
                throw new BusinessException(HttpStatus.TOO_MANY_REQUESTS, "AUTH_TOO_MANY_ATTEMPTS",
                        "Tài khoản tạm thời bị khóa do đăng nhập sai quá nhiều lần. Vui lòng thử lại sau 15 phút.");
            }
        }
    }

    private void recordFailedLogin(String emailKey) {
        if (emailKey.isEmpty()) return;
        Instant now = Instant.now();
        LoginAttemptState state = loginAttempts.computeIfAbsent(emailKey, k -> {
            LoginAttemptState s = new LoginAttemptState();
            s.windowStart = now;
            return s;
        });
        synchronized (state) {
            if (state.windowStart == null || now.isAfter(state.windowStart.plus(LOGIN_LOCK_MINUTES, ChronoUnit.MINUTES))) {
                state.windowStart = now;
                state.failedCount = 1;
                state.lockedUntil = null;
            } else {
                state.failedCount++;
            }
            if (state.failedCount >= MAX_FAILED_LOGIN_ATTEMPTS) {
                state.lockedUntil = now.plus(LOGIN_LOCK_MINUTES, ChronoUnit.MINUTES);
            }
        }
    }

    private void clearFailedLogin(String emailKey) {
        if (!emailKey.isEmpty()) {
            loginAttempts.remove(emailKey);
        }
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }
}

package com.edumy.identity.user.controller;

import com.edumy.identity.auth.model.RefreshToken;
import com.edumy.identity.auth.repository.RefreshTokenRepository;
import com.edumy.identity.user.model.User;
import com.edumy.identity.user.repository.UserRepository;
import com.edumy.notification.service.MailService;
import com.edumy.shared.exception.BusinessException;
import com.edumy.shared.exception.ErrorCode;
import com.edumy.shared.security.SecurityUtils;
import com.edumy.shared.time.TimeUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/me")
@PreAuthorize("isAuthenticated()")
public class MeController {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    @Value("${edumy.time-zone:Asia/Ho_Chi_Minh}")
    private String timeZone;

    public MeController(UserRepository userRepository,
                        RefreshTokenRepository refreshTokenRepository,
                        PasswordEncoder passwordEncoder,
                        MailService mailService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    public record MeDto(
            Integer id,
            String email,
            String fullName,
            boolean emailVerified,
            List<String> roles,
            String status,
            Instant createdAt
    ) {}

    public record UpdateMeRequest(
            @NotBlank(message = "Họ và tên không được để trống.")
            @Size(max = 150, message = "Họ và tên tối đa 150 ký tự.")
            String fullName
    ) {}

    public record ChangePasswordRequest(
            @NotBlank(message = "Vui lòng nhập mật khẩu hiện tại.")
            String currentPassword,
            @NotBlank(message = "Vui lòng nhập mật khẩu mới.")
            @Size(min = 8, max = 72, message = "Mật khẩu mới phải từ 8 đến 72 ký tự.")
            @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "Mật khẩu mới phải chứa cả chữ cái và chữ số.")
            String newPassword
    ) {}

    @GetMapping
    public ResponseEntity<MeDto> getMe() {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy người dùng."));
        return ResponseEntity.ok(toDto(user));
    }

    @PatchMapping
    public ResponseEntity<MeDto> updateMe(@Valid @RequestBody UpdateMeRequest body) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy người dùng."));
        user.setFullName(body.fullName().trim());
        userRepository.save(user);
        return ResponseEntity.ok(toDto(user));
    }

    @PutMapping("/password")
    @Transactional
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest body) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy người dùng."));

        if (user.getPasswordHash() == null || !passwordEncoder.matches(body.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "CURRENT_PASSWORD_INCORRECT",
                    "Mật khẩu hiện tại không chính xác.");
        }

        user.setPasswordHash(passwordEncoder.encode(body.newPassword()));
        userRepository.save(user);

        List<RefreshToken> allTokens = refreshTokenRepository.findByUserId(userId);
        LocalDateTime now = LocalDateTime.now();
        for (RefreshToken t : allTokens) {
            if (t.isActive()) {
                t.setRevoked(now);
            }
        }
        refreshTokenRepository.saveAll(allTokens);

        if (mailService != null && user.getEmail() != null) {
            mailService.sendPasswordChanged(user.getEmail(), user.getFullName(), Instant.now());
        }

        return ResponseEntity.noContent().build();
    }

    private MeDto toDto(User user) {
        return new MeDto(
                user.getUserId(),
                user.getEmail(),
                user.getFullName(),
                Boolean.TRUE.equals(user.getIsEmailVerified()),
                buildRolesList(user.getRole()),
                Boolean.TRUE.equals(user.getIsActive()) ? "ACTIVE" : "LOCKED",
                TimeUtils.toInstant(user.getCreatedAt(), timeZone)
        );
    }

    private List<String> buildRolesList(String role) {
        if ("Admin".equalsIgnoreCase(role)) return List.of("STUDENT", "INSTRUCTOR", "ADMIN");
        if ("Instructor".equalsIgnoreCase(role)) return List.of("STUDENT", "INSTRUCTOR");
        return List.of("STUDENT");
    }
}

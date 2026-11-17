package com.edumy.identity.user.controller;

import com.edumy.identity.user.model.User;
import com.edumy.identity.user.repository.UserRepository;
import com.edumy.shared.exception.BusinessException;
import com.edumy.shared.exception.ErrorCode;
import com.edumy.shared.security.SecurityUtils;
import com.edumy.shared.time.TimeUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/me")
@PreAuthorize("isAuthenticated()")
public class MeController {

    private final UserRepository userRepository;

    @Value("${edumy.time-zone:Asia/Ho_Chi_Minh}")
    private String timeZone;

    public MeController(UserRepository userRepository) {
        this.userRepository = userRepository;
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

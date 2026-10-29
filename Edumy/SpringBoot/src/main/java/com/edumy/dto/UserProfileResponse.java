package com.edumy.dto;

import java.time.Instant;

public record UserProfileResponse(
        int userId,
        String email,
        String fullName,
        String bio,
        String avatarUrl,
        Instant createdAt) {
}

package com.edumy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
        String token,
        String email,
        String fullName,
        String role,
        String message,
        String refreshToken) {
    public AuthResponse withoutRefreshToken() {
        return new AuthResponse(token, email, fullName, role, message, null);
    }
}

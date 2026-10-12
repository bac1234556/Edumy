package com.edumy.auth;

public record AuthenticatedUser(String userId, String email, String role) {
}

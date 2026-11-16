package com.edumy.identity.api;

import java.util.Objects;

public record PasswordResetRequested(Long userId, String email, String fullName, String rawCode) {

    public PasswordResetRequested {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(email, "email");
        Objects.requireNonNull(fullName, "fullName");
        Objects.requireNonNull(rawCode, "rawCode");
    }

    @Override
    public String toString() {
        return "PasswordResetRequested[userId=" + userId + ", email=" + email + ", rawCode=****]";
    }
}

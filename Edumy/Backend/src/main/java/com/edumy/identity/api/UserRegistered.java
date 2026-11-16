package com.edumy.identity.api;

import java.util.Objects;

public record UserRegistered(Long userId, String email, String fullName, String rawVerificationCode) {

    public UserRegistered {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(email, "email");
        Objects.requireNonNull(fullName, "fullName");
        Objects.requireNonNull(rawVerificationCode, "rawVerificationCode");
    }

    @Override
    public String toString() {
        return "UserRegistered[userId=" + userId + ", email=" + email + ", rawVerificationCode=****]";
    }
}

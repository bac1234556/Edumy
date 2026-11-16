package com.edumy.identity.api;

import java.util.Objects;

public record EmailVerificationRequested(Long userId, String email, String fullName, String rawCode) {

    public EmailVerificationRequested {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(email, "email");
        Objects.requireNonNull(fullName, "fullName");
        Objects.requireNonNull(rawCode, "rawCode");
    }

    @Override
    public String toString() {
        return "EmailVerificationRequested[userId=" + userId + ", email=" + email + ", rawCode=****]";
    }
}

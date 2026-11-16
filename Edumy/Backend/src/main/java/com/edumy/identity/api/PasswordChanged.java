package com.edumy.identity.api;

import java.time.Instant;
import java.util.Objects;

public record PasswordChanged(Long userId, String email, String fullName, Instant occurredAt) {

    public PasswordChanged {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(email, "email");
        Objects.requireNonNull(fullName, "fullName");
        Objects.requireNonNull(occurredAt, "occurredAt");
    }
}

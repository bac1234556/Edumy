package com.edumy.shared.security;

/**
 * Shared SPI checked by JwtAuthenticationFilter to verify that an authenticated user
 * is still active (not locked by an administrator). Implemented in the identity module
 * to preserve Spring Modulith boundaries.
 */
public interface UserActiveChecker {
    boolean isUserActive(Integer userId);
}

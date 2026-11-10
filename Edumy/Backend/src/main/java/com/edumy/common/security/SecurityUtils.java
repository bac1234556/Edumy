package com.edumy.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Shared Security Utility across modular monolith bounded contexts.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Integer getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getName() == null || "anonymousUser".equalsIgnoreCase(auth.getName())) {
                return null;
            }
            return Integer.parseInt(auth.getName());
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        String prefixed = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return auth.getAuthorities().stream()
                .anyMatch(a -> prefixed.equalsIgnoreCase(a.getAuthority()) || role.equalsIgnoreCase(a.getAuthority()));
    }
}

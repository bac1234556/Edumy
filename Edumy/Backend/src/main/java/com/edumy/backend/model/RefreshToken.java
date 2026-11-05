package com.edumy.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Table(name = "RefreshTokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "UserId")
    private Integer userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "courses", "enrollments", "reviews", "orders", "userRoles", "refreshTokens", "wishlists", "certificates", "quizAttempts", "cart"})
    private User user;

    @Column(name = "Token", nullable = false)
    private String token;

    @Column(name = "Expires")
    private LocalDateTime expires;

    @Column(name = "Created")
    private LocalDateTime created;

    @Column(name = "Revoked")
    private LocalDateTime revoked;

    @Column(name = "ReplacedByToken")
    private String replacedByToken;

    @Transient
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expires) || LocalDateTime.now().isEqual(expires);
    }

    @Transient
    public boolean isRevoked() {
        return revoked != null;
    }

    @Transient
    public boolean isActive() {
        return !isRevoked() && !isExpired();
    }

    @PrePersist
    protected void onCreate() {
        if (created == null) {
            created = LocalDateTime.now();
        }
    }
}

package com.edumy.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserId")
    private Integer userId;

    @Column(name = "FullName", nullable = false)
    private String fullName;

    @Column(name = "Email", nullable = false)
    private String email;

    @Column(name = "PasswordHash")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String passwordHash;

    @Column(name = "Role")
    @Builder.Default
    private String role = "Student";

    @Column(name = "Provider")
    private String provider;

    @Column(name = "ProviderUserId")
    private String providerUserId;

    @Column(name = "AvatarUrl")
    private String avatarUrl;

    @Column(name = "Bio")
    private String bio;

    @Column(name = "IsActive")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "ResetToken")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String resetToken;

    @Column(name = "ResetTokenExpiry")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private LocalDateTime resetTokenExpiry;

    @OneToMany(mappedBy = "instructor") 
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Course> courses = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Enrollment> enrollments = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Order> orders = new ArrayList<>();
    
    @OneToMany(mappedBy = "user")
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<UserRole> userRoles = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Wishlist> wishlists = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Certificate> certificates = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<QuizAttempt> quizAttempts = new ArrayList<>();

    @OneToOne(mappedBy = "user")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Cart cart;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

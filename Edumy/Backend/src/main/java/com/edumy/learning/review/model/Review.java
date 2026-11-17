package com.edumy.learning.review.model;

import com.edumy.authoring.model.Course;
import com.edumy.identity.user.model.User;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Table(name = "Reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ReviewId")
    private Integer reviewId;

    @Column(name = "UserId")
    private Integer userId;

    @Column(name = "CourseId")
    private Integer courseId;

    @Column(name = "Rating")
    private Integer rating;

    @Column(name = "Comment", nullable = false)
    private String comment;

    @Column(name = "SentimentLabel")
    private String sentimentLabel;

    @Column(name = "SentimentScore")
    private Double sentimentScore;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "VisibilityStatus", length = 32)
    @Builder.Default
    private String visibilityStatus = "VISIBLE";

    @Column(name = "ModerationReason", length = 500)
    private String moderationReason;

    @Column(name = "ModeratedAt")
    private LocalDateTime moderatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "courses", "enrollments", "reviews", "orders", "userRoles", "refreshTokens", "wishlists", "certificates", "quizAttempts", "cart"})
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CourseId", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Course course;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

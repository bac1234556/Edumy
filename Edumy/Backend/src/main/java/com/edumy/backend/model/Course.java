package com.edumy.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

@Entity
@Table(name = "Courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CourseId")
    private Integer courseId;

    @Column(name = "InstructorId")
    private Integer instructorId;

    @Column(name = "CategoryId")
    private Integer categoryId;

    @Column(name = "Title", nullable = false)
    private String title;

    @Column(name = "Slug", nullable = false)
    private String slug;

    @Column(name = "Description")
    private String description;

    @Column(name = "Price")
    private BigDecimal price;

    @Column(name = "ThumbnailUrl", length = 500)
    private String thumbnailUrl;

    @Column(name = "Level")
    @Builder.Default
    private String level = "Beginner";

    @Column(name = "Status")
    @Builder.Default
    private String status = "Draft";

    @Column(name = "AverageRating")
    @Builder.Default
    private Double averageRating = 0.0;

    @Column(name = "StudentCount")
    @Builder.Default
    private Integer studentCount = 0;

    @Column(name = "NeedsReanalysis")
    @Builder.Default
    private Boolean needsReanalysis = false;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "InstructorId", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "courses", "enrollments", "reviews", "orders", "cart", "userRoles", "refreshTokens"})
    private User instructor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CategoryId", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "courses"})
    private Category category;

    @OneToMany(mappedBy = "course")
    @Builder.Default
    private List<CourseSection> sections = new ArrayList<>();

    @OneToMany(mappedBy = "course")
    @Builder.Default
    private List<Enrollment> enrollments = new ArrayList<>();

    @OneToMany(mappedBy = "course")
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "course")
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();
    
    @OneToMany(mappedBy = "course")
    @Builder.Default
    private List<Wishlist> wishlists = new ArrayList<>();

    @OneToMany(mappedBy = "course")
    @Builder.Default
    private List<CourseCoupon> courseCoupons = new ArrayList<>();

    @OneToMany(mappedBy = "course")
    @Builder.Default
    private List<Certificate> certificates = new ArrayList<>();

    @OneToMany(mappedBy = "course")
    @Builder.Default
    private List<CourseMlAnalysis> mlAnalyses = new ArrayList<>();
    
    @OneToMany(mappedBy = "course")
    @Builder.Default
    private List<CourseTag> courseTags = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

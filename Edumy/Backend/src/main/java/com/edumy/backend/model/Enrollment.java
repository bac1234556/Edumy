package com.edumy.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Table(name = "Enrollments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EnrollmentId")
    private Integer enrollmentId;

    @Column(name = "UserId")
    private Integer userId;

    @Column(name = "CourseId")
    private Integer courseId;

    @Column(name = "EnrolledAt")
    private LocalDateTime enrolledAt;

    @Column(name = "ProgressPercentage")
    @Builder.Default
    private Integer progressPercentage = 0;

    @Column(name = "Status")
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "CompletedAt")
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CourseId", insertable = false, updatable = false)
    private Course course;

    @PrePersist
    protected void onCreate() {
        if (enrolledAt == null) {
            enrolledAt = LocalDateTime.now();
        }
    }
}

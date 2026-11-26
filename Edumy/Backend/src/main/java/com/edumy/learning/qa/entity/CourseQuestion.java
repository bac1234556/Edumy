package com.edumy.learning.qa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Integer courseId;

    @Column(name = "lesson_id")
    private Integer lessonId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @org.hibernate.annotations.Nationalized
    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @org.hibernate.annotations.Nationalized
    @Column(name = "content", nullable = false, length = 5000)
    private String content;

    @Column(name = "is_resolved", nullable = false)
    @Builder.Default
    private Boolean isResolved = false;

    @Column(name = "visibility_status", nullable = false, length = 20)
    @Builder.Default
    private String visibilityStatus = "VISIBLE";

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}

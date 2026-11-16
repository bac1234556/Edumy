package com.edumy.modules.learning.qa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "question_answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "author_id", nullable = false)
    private Integer authorId;

    @Column(name = "author_type", nullable = false, length = 20)
    @Builder.Default
    private String authorType = "STUDENT"; // STUDENT, INSTRUCTOR

    @Column(name = "content", nullable = false, length = 5000)
    private String content;

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

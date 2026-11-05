package com.edumy.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "QuestionId")
    private Integer questionId;

    @Column(name = "Content", nullable = false)
    private String content;

    @Column(name = "Points")
    @Builder.Default
    private Integer points = 1;

    @Column(name = "Explanation")
    private String explanation;
    
    @Column(name = "QuizId")
    private Integer quizId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "QuizId", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "questions", "quizAttempts", "courseSection"})
    private Quiz quiz;
    
    @OneToMany(mappedBy = "question")
    @Builder.Default
    private List<Answer> answers = new ArrayList<>();
}

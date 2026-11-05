package com.edumy.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "QuizAttemptAnswers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttemptAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "QuizAttemptAnswerId")
    private Integer quizAttemptAnswerId;
    
    @Column(name = "QuizAttemptId")
    private Integer quizAttemptId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "QuizAttemptId", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "selectedAnswers", "user", "quiz"})
    private QuizAttempt quizAttempt;
    
    @Column(name = "QuestionId")
    private Integer questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "QuestionId", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "answers", "quiz"})
    private Question question;
    
    @Column(name = "AnswerId")
    private Integer answerId;

    @Column(name = "IsCorrect")
    private Boolean isCorrect;

    public Integer getAttemptId() {
        return quizAttemptId;
    }

    public void setAttemptId(Integer attemptId) {
        this.quizAttemptId = attemptId;
    }

    public Integer getSelectedAnswerId() {
        return answerId;
    }

    public void setSelectedAnswerId(Integer selectedAnswerId) {
        this.answerId = selectedAnswerId;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AnswerId", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "question"})
    private Answer answer;
}

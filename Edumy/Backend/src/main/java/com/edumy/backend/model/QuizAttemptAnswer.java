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
    private QuizAttempt quizAttempt;
    
    @Column(name = "QuestionId")
    private Integer questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "QuestionId", insertable = false, updatable = false)
    private Question question;
    
    @Column(name = "AnswerId")
    private Integer answerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AnswerId", insertable = false, updatable = false)
    private Answer answer;
}

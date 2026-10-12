package com.edumy.backend.controller;

import com.edumy.backend.model.*;
import com.edumy.backend.dto.*;
import com.edumy.backend.repository.*;
import com.edumy.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/QuizAttempts")
@PreAuthorize("isAuthenticated()")
public class QuizAttemptsController {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    private Integer getCurrentUserId() {
        try {
            return Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());
        } catch (Exception e) {
            return null;
        }
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitQuiz(@RequestBody QuizSubmissionDto submission) {
        Integer userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();

        Quiz quiz = quizRepository.findById(submission.getQuizId()).orElse(null);

        if (quiz == null) return ResponseEntity.status(404).body("Quiz not found.");

        QuizAttempt attempt = new QuizAttempt();
        attempt.setUserId(userId);
        attempt.setQuizId(quiz.getQuizId());
        attempt.setStartedAt(LocalDateTime.now());
        attempt.setCompletedAt(LocalDateTime.now());
        attempt.setSelectedAnswers(new ArrayList<>());

        int score = 0;
        int totalPoints = 0;
        
        if (quiz.getQuestions() != null) {
            totalPoints = quiz.getQuestions().stream().mapToInt(Question::getPoints).sum();
        }
        
        QuizResultDto resultDto = new QuizResultDto();
        resultDto.setTotalPoints(totalPoints);
        resultDto.setResults(new ArrayList<>());

        if (quiz.getQuestions() != null) {
            for (Question question : quiz.getQuestions()) {
                Integer selectedAnswerId = submission.getSelectedAnswers().get(question.getQuestionId());
                if (selectedAnswerId == null) selectedAnswerId = 0;

                Answer correctAnswer = null;
                if (question.getAnswers() != null) {
                    correctAnswer = question.getAnswers().stream().filter(Answer::getIsCorrect).findFirst().orElse(null);
                }
                
                boolean isCorrect = correctAnswer != null && correctAnswer.getAnswerId().equals(selectedAnswerId);
                
                if (isCorrect) score += question.getPoints();

                if (selectedAnswerId > 0) {
                    QuizAttemptAnswer qaa = new QuizAttemptAnswer();
                    qaa.setQuestionId(question.getQuestionId());
                    qaa.setAnswerId(selectedAnswerId);
                    attempt.getSelectedAnswers().add(qaa);
                }

                QuestionResultDto qr = new QuestionResultDto();
                qr.setQuestionId(question.getQuestionId());
                qr.setSelectedAnswerId(selectedAnswerId);
                qr.setCorrectAnswerId(correctAnswer != null ? correctAnswer.getAnswerId() : 0);
                qr.setIsCorrect(isCorrect);
                qr.setExplanation(question.getExplanation());
                
                resultDto.getResults().add(qr);
            }
        }

        attempt.setScore(score);
        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);

        double percentage = totalPoints > 0 ? (double) score / totalPoints * 100 : 0;
        resultDto.setScore(score);
        resultDto.setPassed(percentage >= quiz.getPassingScore());
        resultDto.setQuizAttemptId(savedAttempt.getQuizAttemptId());

        return ResponseEntity.ok(resultDto);
    }
}

package com.edumy.modules.quiz.controller;

import com.edumy.modules.quiz.service.QuizAttemptService;
import com.edumy.modules.quiz.dto.QuizResultDto;
import com.edumy.modules.quiz.dto.QuizSubmissionDto;
import com.edumy.modules.quiz.dto.QuizSubmitRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping({"/api/v1/quiz-attempts", "/api/QuizAttempts", "/api/quizattempts", "/api/quiz-attempts"})
@PreAuthorize("isAuthenticated()")
public class QuizAttemptsController {

    @Autowired
    private QuizAttemptService quizAttemptService;

    private Integer getCurrentUserId() {
        try {
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            return (name != null && !name.equalsIgnoreCase("anonymousUser")) ? Integer.parseInt(name) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Nộp bài kiểm tra trắc nghiệm (Transactional, bảo toàn lịch sử làm bài và cấp chứng chỉ)
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitQuiz(@RequestBody QuizSubmissionDto submission) {
        try {
            Integer userId = getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Yêu cầu đăng nhập."));
            }

            if (submission == null || submission.getQuizId() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Dữ liệu nộp bài không hợp lệ."));
            }

            QuizResultDto result = quizAttemptService.submitQuiz(submission.getQuizId(), submission, userId);
            return ResponseEntity.ok(result);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Lỗi xử lý nộp bài: " + ex.getMessage()
            ));
        }
    }

    /**
     * Endpoint hỗ trợ định dạng submitQuiz(Long quizId, QuizSubmitRequest request)
     */
    @PostMapping("/{quizId}/submit")
    public ResponseEntity<?> submitQuizWithParam(@PathVariable Long quizId, @RequestBody QuizSubmitRequest request) {
        try {
            QuizResultDto result = quizAttemptService.submitQuiz(quizId, request);
            return ResponseEntity.ok(result);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Lỗi xử lý nộp bài: " + ex.getMessage()
            ));
        }
    }
}

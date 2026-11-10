package com.edumy.modules.quiz.controller;

import com.edumy.modules.quiz.dto.QuizDto;
import com.edumy.modules.quiz.service.QuizService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping({"/api/v1/quizzes", "/api/Quizzes", "/api/quizzes"})
public class QuizzesController {

    @Autowired
    private QuizService quizService;

    private Integer getCurrentUserId() {
        try {
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            return (name != null && !name.equalsIgnoreCase("anonymousUser")) ? Integer.parseInt(name) : null;
        } catch (Exception e) {
            return null;
        }
    }

    @GetMapping("/section/{sectionId}")
    public ResponseEntity<?> getQuizzesBySection(@PathVariable Integer sectionId) {
        Integer userId = getCurrentUserId();
        List<QuizDto> quizzes = quizService.getQuizzesBySection(sectionId, userId);
        return ResponseEntity.ok(quizzes);
    }

    /**
     * API lấy chi tiết câu hỏi trắc nghiệm chống IDOR và chống lộ isCorrect:
     * GET /api/v1/quizzes/{id}
     * GET /api/quizzes/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getQuizForStudent(@PathVariable Integer id) {
        try {
            Integer userId = getCurrentUserId();
            QuizDto dto = quizService.getQuizForStudent(id, userId);
            return ResponseEntity.ok(dto);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", HttpStatus.NOT_FOUND.value(),
                    "message", ex.getMessage()
            ));
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "status", HttpStatus.FORBIDDEN.value(),
                    "error", "Forbidden",
                    "message", "Bạn chưa đăng ký khóa học này nên không thể xem bài thi trắc nghiệm!"
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "error", "Internal Server Error",
                    "message", ex.getMessage()
            ));
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> createQuiz(@RequestBody QuizDto dto) {
        try {
            Integer userId = getCurrentUserId();
            QuizDto created = quizService.createQuiz(dto, userId);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> deleteQuiz(@PathVariable Integer id) {
        try {
            Integer userId = getCurrentUserId();
            quizService.deleteQuiz(id, userId);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}

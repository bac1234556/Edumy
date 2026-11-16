package com.edumy.modules.learning.qa.controller;

import com.edumy.modules.learning.qa.dto.CreateAnswerRequest;
import com.edumy.modules.learning.qa.dto.CreateQuestionRequest;
import com.edumy.modules.learning.qa.service.QaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1", "/api"})
public class QuestionController {

    private final QaService qaService;

    public QuestionController(QaService qaService) {
        this.qaService = qaService;
    }

    /**
     * List questions for a course (optionally filtered by lesson), newest first.
     */
    @GetMapping("/courses/{courseId}/questions")
    public ResponseEntity<?> listForCourse(
            @PathVariable Integer courseId,
            @RequestParam(required = false) Integer lessonId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(qaService.listForCourse(courseId, lessonId, pageable));
    }

    /**
     * Get a single question thread with all replies.
     */
    @GetMapping("/questions/{questionId}")
    public ResponseEntity<?> getThread(@PathVariable Long questionId) {
        return ResponseEntity.ok(qaService.getThread(questionId));
    }

    /**
     * Student posts a new question in a course.
     */
    @PostMapping("/courses/{courseId}/questions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> askQuestion(
            @PathVariable Integer courseId,
            @Valid @RequestBody CreateQuestionRequest request) {
        Integer userId = getCurrentUserId();
        return ResponseEntity.ok(qaService.askQuestion(userId, courseId, request));
    }

    /**
     * Post a reply to an existing question (by student or instructor).
     */
    @PostMapping("/questions/{questionId}/answers")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> postAnswer(
            @PathVariable Long questionId,
            @Valid @RequestBody CreateAnswerRequest request) {
        Integer userId = getCurrentUserId();
        String role = getCurrentUserRole();
        return ResponseEntity.ok(qaService.postAnswer(userId, role, questionId, request));
    }

    /**
     * Mark a question resolved / open.
     */
    @RequestMapping(value = "/questions/{questionId}/resolve", method = {RequestMethod.PUT, RequestMethod.PATCH})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> toggleResolve(@PathVariable Long questionId) {
        Integer userId = getCurrentUserId();
        return ResponseEntity.ok(qaService.toggleResolve(userId, questionId));
    }

    private Integer getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        try {
            return Integer.parseInt(auth.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return "Student";
        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("Student");
    }
}

package com.edumy.learning.qa.controller;

import com.edumy.learning.qa.dto.CreateAnswerRequest;
import com.edumy.learning.qa.dto.CreateQuestionRequest;
import com.edumy.learning.qa.service.QaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/api/v1", "/api"})
public class QuestionController {

    private final QaService qaService;

    public QuestionController(QaService qaService) {
        this.qaService = qaService;
    }

    /**
     * UC-S18: List questions for a course (optionally filtered by lesson, mineOnly, unansweredOnly, keyword q), newest first.
     */
    @GetMapping("/courses/{courseId}/questions")
    public ResponseEntity<?> listForCourse(
            @PathVariable Integer courseId,
            @RequestParam(required = false) Integer lessonId,
            @RequestParam(required = false, defaultValue = "false") boolean mineOnly,
            @RequestParam(required = false, defaultValue = "false") boolean unansweredOnly,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Integer currentUserId = getCurrentUserId();
        return ResponseEntity.ok(qaService.listForCourse(courseId, lessonId, currentUserId, mineOnly, unansweredOnly, q, pageable));
    }

    /**
     * UC-S18: Get a single question thread with all replies.
     */
    @GetMapping("/questions/{questionId}")
    public ResponseEntity<?> getThread(@PathVariable Long questionId) {
        return ResponseEntity.ok(qaService.getThread(questionId));
    }

    /**
     * UC-S17: Student posts a new question in a course.
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
     * UC-S19: Edit own question.
     */
    @RequestMapping(value = "/questions/{questionId}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateQuestion(
            @PathVariable Long questionId,
            @RequestBody Map<String, String> body) {
        Integer userId = getCurrentUserId();
        String role = getCurrentUserRole();
        String title = body != null ? body.get("title") : null;
        String content = body != null ? body.get("content") : null;
        return ResponseEntity.ok(qaService.updateQuestion(userId, role, questionId, title, content));
    }

    /**
     * UC-S19: Delete own question.
     */
    @DeleteMapping("/questions/{questionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long questionId) {
        Integer userId = getCurrentUserId();
        String role = getCurrentUserRole();
        qaService.deleteQuestion(userId, role, questionId);
        return ResponseEntity.ok(Map.of("message", "Đã xóa câu hỏi thành công."));
    }

    /**
     * UC-I20: Post a reply to an existing question (by student or instructor).
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
     * UC-I21 / UC-S19: Edit own answer.
     */
    @RequestMapping(value = "/answers/{answerId}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateAnswer(
            @PathVariable Long answerId,
            @RequestBody Map<String, String> body) {
        Integer userId = getCurrentUserId();
        String role = getCurrentUserRole();
        String content = body != null ? body.get("content") : null;
        return ResponseEntity.ok(qaService.updateAnswer(userId, role, answerId, content));
    }

    /**
     * UC-I21 / UC-S19: Delete own answer.
     */
    @DeleteMapping("/answers/{answerId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteAnswer(@PathVariable Long answerId) {
        Integer userId = getCurrentUserId();
        String role = getCurrentUserRole();
        qaService.deleteAnswer(userId, role, answerId);
        return ResponseEntity.ok(Map.of("message", "Đã xóa câu trả lời thành công."));
    }

    /**
     * UC-S19: Mark a question resolved / open.
     */
    @RequestMapping(value = "/questions/{questionId}/resolve", method = {RequestMethod.PUT, RequestMethod.PATCH})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> toggleResolve(@PathVariable Long questionId) {
        Integer userId = getCurrentUserId();
        return ResponseEntity.ok(qaService.toggleResolve(userId, questionId));
    }

    /**
     * UC-A19: Admin lists all questions for moderation.
     */
    @GetMapping("/admin/questions")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> listAllQuestionsForAdmin() {
        return ResponseEntity.ok(qaService.listAllForAdmin());
    }

    /**
     * UC-A19: Admin hides a violating question.
     */
    @PostMapping("/admin/questions/{questionId}/hide")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> hideQuestion(@PathVariable Long questionId) {
        return ResponseEntity.ok(qaService.moderateQuestion(questionId, "HIDDEN"));
    }

    /**
     * UC-A19: Admin restores a hidden question.
     */
    @PostMapping("/admin/questions/{questionId}/restore")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> restoreQuestion(@PathVariable Long questionId) {
        return ResponseEntity.ok(qaService.moderateQuestion(questionId, "VISIBLE"));
    }

    /**
     * UC-A19: Admin hides a violating answer.
     */
    @PostMapping("/admin/answers/{answerId}/hide")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> hideAnswer(@PathVariable Long answerId) {
        return ResponseEntity.ok(qaService.moderateAnswer(answerId, "HIDDEN"));
    }

    /**
     * UC-A19: Admin restores a hidden answer.
     */
    @PostMapping("/admin/answers/{answerId}/restore")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> restoreAnswer(@PathVariable Long answerId) {
        return ResponseEntity.ok(qaService.moderateAnswer(answerId, "VISIBLE"));
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

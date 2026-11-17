package com.edumy.learning.qa.controller;

import com.edumy.learning.qa.dto.QuestionSummaryResponse;
import com.edumy.learning.qa.service.QaService;
import com.edumy.shared.response.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Questions of the courses the caller instructs (UC-I22).
 * Oldest first queue (cũ nhất trước) for unanswered student questions.
 */
@RestController
@RequestMapping({"/api/v1/instructor/questions", "/api/instructor/questions"})
@PreAuthorize("hasAnyRole('Instructor', 'Admin')")
public class InstructorQuestionController {

    private final QaService qaService;

    public InstructorQuestionController(QaService qaService) {
        this.qaService = qaService;
    }

    /**
     * UC-I22: List questions for instructor courses, oldest first waiting queue.
     * @param courseId optional filter by course
     * @param status "all" to show all questions; default "unanswered" shows only unanswered questions
     */
    @GetMapping
    public ResponseEntity<PageResponse<QuestionSummaryResponse>> listForInstructor(
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false, defaultValue = "unanswered") String status,
            @PageableDefault(size = 20) Pageable pageable) {
        Integer instructorId = getCurrentUserId();
        boolean unansweredOnly = !"all".equalsIgnoreCase(status);
        return ResponseEntity.ok(PageResponse.from(
                qaService.listForInstructor(instructorId, courseId, unansweredOnly, pageable)));
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
}

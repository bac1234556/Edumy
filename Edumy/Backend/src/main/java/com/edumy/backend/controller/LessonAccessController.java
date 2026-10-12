package com.edumy.backend.controller;

import com.edumy.backend.dto.LessonAccessResponseDto;
import com.edumy.backend.service.LessonAccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/lessons")
public class LessonAccessController {

    @Autowired
    private LessonAccessService lessonAccessService;

    private Integer getCurrentUserId() {
        try {
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            return (name != null && !name.equalsIgnoreCase("anonymousUser")) ? Integer.parseInt(name) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * GET /api/v1/lessons/{id}/access
     * Kiểm tra quyền truy cập video bài giảng:
     * - isPreview == true -> Cho phép xem
     * - isPreview == false -> Bắt buộc xác thực JWT và có bản ghi Enrollments (status: ACTIVE)
     * - Không hợp lệ -> Trả về HTTP 403 Forbidden
     */
    @GetMapping("/{id}/access")
    public ResponseEntity<?> getLessonAccess(@PathVariable("id") Integer id) {
        try {
            Integer currentUserId = getCurrentUserId();
            LessonAccessResponseDto response = lessonAccessService.checkLessonAccess(id, currentUserId);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", HttpStatus.NOT_FOUND.value(),
                    "message", ex.getMessage(),
                    "canPlay", false
            ));
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "status", HttpStatus.FORBIDDEN.value(),
                    "error", "Forbidden",
                    "message", ex.getMessage(),
                    "canPlay", false,
                    "isPreview", false
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "error", "Internal Server Error",
                    "message", "Lỗi xử lý kiểm tra quyền truy cập bài học: " + ex.getMessage()
            ));
        }
    }
}

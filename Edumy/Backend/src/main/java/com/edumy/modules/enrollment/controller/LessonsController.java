package com.edumy.modules.enrollment.controller;

import com.edumy.modules.course.model.Lesson;
import com.edumy.modules.enrollment.dto.LessonAccessResponseDto;
import com.edumy.modules.course.repository.LessonRepository;
import com.edumy.modules.enrollment.service.LessonService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping({"/api/v1/lessons", "/api/Lessons", "/api/lessons"})
public class LessonsController {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private LessonService lessonService;

    private Integer getCurrentUserId() {
        try {
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            return (name != null && !name.equalsIgnoreCase("anonymousUser")) ? Integer.parseInt(name) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * API lấy chi tiết bài học chống IDOR:
     * GET /api/v1/lessons/{id}
     * GET /api/lessons/{id}
     */
    @GetMapping({"/{id}", "/detail/{id}"})
    public ResponseEntity<?> getLessonDetail(@PathVariable("id") Integer id) {
        try {
            Integer userId = getCurrentUserId();
            LessonAccessResponseDto accessDto = lessonService.getLessonAccess(id, userId);
            return ResponseEntity.ok(accessDto);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", HttpStatus.NOT_FOUND.value(),
                    "message", ex.getMessage()
            ));
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "status", HttpStatus.FORBIDDEN.value(),
                    "error", "Forbidden",
                    "message", "Bạn chưa đăng ký khóa học này nên không thể xem bài học!"
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "error", "Internal Server Error",
                    "message", ex.getMessage()
            ));
        }
    }

    /**
     * API stream video bài học chống IDOR:
     * GET /api/v1/lessons/{id}/stream
     * GET /api/lessons/{id}/play
     * GET /api/v1/lessons/{id}/play
     */
    @GetMapping({"/{id}/stream", "/{id}/play", "/{id}/access"})
    public ResponseEntity<?> streamLesson(@PathVariable("id") Integer id) {
        try {
            Integer userId = getCurrentUserId();
            LessonAccessResponseDto accessDto = lessonService.getLessonAccess(id, userId);
            return ResponseEntity.ok(accessDto);
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
                    "message", "Bạn chưa đăng ký khóa học này nên không thể xem bài học!",
                    "canPlay", false,
                    "isPreview", false
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "error", "Internal Server Error",
                    "message", ex.getMessage(),
                    "canPlay", false
            ));
        }
    }

    /**
     * Lấy danh sách bài học theo section, chỉ kèm videoUrl nếu học viên đã đăng ký hoặc bài xem thử
     */
    @GetMapping("/section/{sectionId}")
    public ResponseEntity<?> getSectionLessons(@PathVariable Integer sectionId) {
        try {
            Integer userId = getCurrentUserId();
            List<Lesson> lessons = lessonService.getSectionLessons(sectionId, userId);
            return ResponseEntity.ok(lessons);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> createLesson(@RequestBody Lesson lesson) {
        Lesson saved = lessonRepository.save(lesson);
        return ResponseEntity.created(java.net.URI.create("/api/Lessons/section/" + lesson.getSectionId())).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> updateLesson(@PathVariable Integer id, @RequestBody Lesson lesson) {
        if (!id.equals(lesson.getLessonId())) return ResponseEntity.badRequest().build();
        if (!lessonRepository.existsById(id)) return ResponseEntity.notFound().build();
        
        lessonRepository.save(lesson);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> deleteLesson(@PathVariable Integer id) {
        if (!lessonRepository.existsById(id)) return ResponseEntity.notFound().build();
        
        lessonRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

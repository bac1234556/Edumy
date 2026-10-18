package com.edumy.backend.controller;

import com.edumy.backend.model.*;
import com.edumy.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/Lessons", "/api/lessons"})
public class LessonsController {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private CourseSectionRepository courseSectionRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    private Integer getCurrentUserId() {
        try {
            return Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isUserInRole(String role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    private boolean hasCourseAccess(Integer courseId, Integer instructorId) {
        Integer userId = getCurrentUserId();
        if (userId == null) return false;
        if (isUserInRole("Admin")) return true;
        if (instructorId != null && instructorId.equals(userId)) return true;
        return enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    @GetMapping("/section/{sectionId}")
    public ResponseEntity<?> getSectionLessons(@PathVariable Integer sectionId) {
        CourseSection section = courseSectionRepository.findById(sectionId).orElse(null);
        if (section == null) return ResponseEntity.notFound().build();

        Course course = courseRepository.findById(section.getCourseId()).orElse(null);
        Integer instructorId = course != null ? course.getInstructorId() : null;
        boolean canAccessFull = hasCourseAccess(section.getCourseId(), instructorId);

        List<Lesson> lessons = lessonRepository.findAll().stream()
                .filter(l -> sectionId.equals(l.getSectionId()))
                .sorted(Comparator.comparing(Lesson::getOrderIndex))
                .map(l -> {
                    Lesson copy = new Lesson();
                    copy.setLessonId(l.getLessonId());
                    copy.setSectionId(l.getSectionId());
                    copy.setTitle(l.getTitle());
                    copy.setDuration(l.getDuration());
                    copy.setOrderIndex(l.getOrderIndex());
                    copy.setIsPreview(l.getIsPreview());
                    if (canAccessFull || Boolean.TRUE.equals(l.getIsPreview())) {
                        copy.setVideoUrl(l.getVideoUrl());
                    } else {
                        copy.setVideoUrl(null);
                    }
                    return copy;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(lessons);
    }

    @GetMapping("/{id}/play")
    public ResponseEntity<?> getLessonPlayback(@PathVariable Integer id) {
        Lesson lesson = lessonRepository.findById(id).orElse(null);
        if (lesson == null) return ResponseEntity.notFound().build();

        CourseSection section = courseSectionRepository.findById(lesson.getSectionId()).orElse(null);
        if (section == null) return ResponseEntity.status(404).body(Map.of("message", "Section not found"));

        Course course = courseRepository.findById(section.getCourseId()).orElse(null);
        Integer instructorId = course != null ? course.getInstructorId() : null;
        boolean canAccess = hasCourseAccess(section.getCourseId(), instructorId);

        if (!canAccess && !Boolean.TRUE.equals(lesson.getIsPreview())) {
            return ResponseEntity.status(403).body(Map.of(
                "message", "Bạn cần đăng ký khóa học để xem nội dung bài giảng này.",
                "isEnrolled", false,
                "isPreview", false
            ));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("lessonId", lesson.getLessonId());
        response.put("sectionId", lesson.getSectionId());
        response.put("courseId", section.getCourseId());
        response.put("title", lesson.getTitle());
        response.put("videoUrl", lesson.getVideoUrl());
        response.put("duration", lesson.getDuration());
        response.put("isPreview", lesson.getIsPreview());
        response.put("authorized", true);

        return ResponseEntity.ok(response);
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

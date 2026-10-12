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
import java.util.List;

@RestController
@RequestMapping("/api")
public class CurriculumController {

    @Autowired
    private CourseSectionRepository courseSectionRepository;

    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private LessonRepository lessonRepository;

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

    @GetMapping("/courses/{courseId}/curriculum")
    public ResponseEntity<?> getCurriculum(@PathVariable Integer courseId) {
        // Assume findByCourseIdOrderByOrderIndexAsc is available
        List<CourseSection> sections = courseSectionRepository.findAll().stream()
                .filter(s -> courseId.equals(s.getCourseId()))
                .sorted(java.util.Comparator.comparing(CourseSection::getOrderIndex))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(sections);
    }

    @PostMapping("/courses/{courseId}/sections")
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> createSection(@PathVariable Integer courseId, @RequestBody CourseSection section) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) return ResponseEntity.status(404).body("Course not found");

        Integer userId = getCurrentUserId();
        if (userId != null && !userId.equals(course.getInstructorId()) && !isUserInRole("Admin")) {
            return ResponseEntity.status(403).build();
        }

        section.setCourseId(courseId);
        CourseSection saved = courseSectionRepository.save(section);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/sections/{sectionId}/lessons")
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> createLesson(@PathVariable Integer sectionId, @RequestBody Lesson lesson) {
        CourseSection section = courseSectionRepository.findById(sectionId).orElse(null);
        if (section == null || section.getCourse() == null) return ResponseEntity.status(404).body("Section not found");

        Integer userId = getCurrentUserId();
        if (userId != null && !userId.equals(section.getCourse().getInstructorId()) && !isUserInRole("Admin")) {
            return ResponseEntity.status(403).build();
        }

        lesson.setSectionId(sectionId);
        Lesson saved = lessonRepository.save(lesson);
        return ResponseEntity.ok(saved);
    }
}

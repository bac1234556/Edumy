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
@RequestMapping("/api")
public class CurriculumController {

    @Autowired
    private CourseSectionRepository courseSectionRepository;

    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private LessonRepository lessonRepository;

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

    @GetMapping("/courses/{courseId}/curriculum")
    public ResponseEntity<?> getCurriculum(@PathVariable Integer courseId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) return ResponseEntity.notFound().build();

        Integer userId = getCurrentUserId();
        boolean isEnrolled = userId != null && (
            isUserInRole("Admin") || 
            userId.equals(course.getInstructorId()) || 
            enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)
        );

        List<CourseSection> sections = courseSectionRepository.findAll().stream()
                .filter(s -> courseId.equals(s.getCourseId()))
                .sorted(Comparator.comparing(CourseSection::getOrderIndex))
                .map(s -> {
                    CourseSection copySection = new CourseSection();
                    copySection.setSectionId(s.getSectionId());
                    copySection.setCourseId(s.getCourseId());
                    copySection.setTitle(s.getTitle());
                    copySection.setOrderIndex(s.getOrderIndex());
                    copySection.setQuizzes(s.getQuizzes());

                    if (s.getLessons() != null) {
                        List<Lesson> maskedLessons = s.getLessons().stream()
                                .sorted(Comparator.comparing(Lesson::getOrderIndex))
                                .map(l -> {
                                    Lesson copy = new Lesson();
                                    copy.setLessonId(l.getLessonId());
                                    copy.setSectionId(l.getSectionId());
                                    copy.setTitle(l.getTitle());
                                    copy.setDuration(l.getDuration());
                                    copy.setOrderIndex(l.getOrderIndex());
                                    copy.setIsPreview(l.getIsPreview());
                                    if (isEnrolled || Boolean.TRUE.equals(l.getIsPreview())) {
                                        copy.setVideoUrl(l.getVideoUrl());
                                    } else {
                                        copy.setVideoUrl(null);
                                    }
                                    return copy;
                                }).collect(Collectors.toList());
                        copySection.setLessons(maskedLessons);
                    }
                    return copySection;
                })
                .collect(Collectors.toList());
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

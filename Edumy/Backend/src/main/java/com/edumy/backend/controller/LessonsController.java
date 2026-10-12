package com.edumy.backend.controller;

import com.edumy.backend.model.*;
import com.edumy.backend.dto.*;
import com.edumy.backend.repository.*;
import com.edumy.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/Lessons")
public class LessonsController {

    @Autowired
    private LessonRepository lessonRepository;

    @GetMapping("/section/{sectionId}")
    public ResponseEntity<?> getSectionLessons(@PathVariable Integer sectionId) {
        List<Lesson> lessons = lessonRepository.findAll().stream()
                .filter(l -> sectionId.equals(l.getSectionId()))
                .sorted(java.util.Comparator.comparing(Lesson::getOrderIndex))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(lessons);
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

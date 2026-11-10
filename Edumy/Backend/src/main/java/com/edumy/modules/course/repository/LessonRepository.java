package com.edumy.modules.course.repository;

import com.edumy.modules.course.model.Lesson;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Integer> {
    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.section.courseId = :courseId")
    long countLessonsByCourseId(@Param("courseId") Integer courseId);
}

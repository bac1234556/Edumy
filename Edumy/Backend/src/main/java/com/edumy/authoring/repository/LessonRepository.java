package com.edumy.authoring.repository;

import com.edumy.authoring.model.Lesson;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Integer> {
    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.section.courseId = :courseId")
    long countLessonsByCourseId(@Param("courseId") Integer courseId);

    @Query("SELECT l.lessonId FROM Lesson l WHERE l.section.courseId = :courseId")
    List<Integer> findLessonIdsByCourseId(@Param("courseId") Integer courseId);

    List<Lesson> findBySectionIdInOrderByOrderIndexAsc(List<Integer> sectionIds);
}

package com.edumy.backend.repository;

import com.edumy.backend.model.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Integer> {
    List<LessonProgress> findByUserId(Integer userId);
    List<LessonProgress> findByUserIdAndCourseId(Integer userId, Integer courseId);
    LessonProgress findByUserIdAndLessonId(Integer userId, Integer lessonId);
    Optional<LessonProgress> findFirstByUserIdAndCourseIdAndLessonId(Integer userId, Integer courseId, Integer lessonId);
    boolean existsByUserIdAndCourseIdAndLessonId(Integer userId, Integer courseId, Integer lessonId);

    @Query("SELECT COUNT(lp) FROM LessonProgress lp WHERE lp.userId = :userId AND lp.courseId = :courseId")
    long countCompletedLessonsByUserAndCourse(@Param("userId") Integer userId, @Param("courseId") Integer courseId);
}

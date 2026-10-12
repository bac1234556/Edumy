package com.edumy.backend.repository;

import com.edumy.backend.model.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Integer> {
    List<LessonProgress> findByUserId(Integer userId);
    LessonProgress findByUserIdAndLessonId(Integer userId, Integer lessonId);
}

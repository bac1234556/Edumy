package com.edumy.backend.repository;

import com.edumy.backend.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Integer> {
    @Query("SELECT COUNT(q) FROM Quiz q WHERE q.courseSection.courseId = :courseId")
    long countQuizzesByCourseId(@Param("courseId") Integer courseId);

    @Query("SELECT q.quizId FROM Quiz q WHERE q.courseSection.courseId = :courseId")
    java.util.List<Integer> findQuizIdsByCourseId(@Param("courseId") Integer courseId);
}

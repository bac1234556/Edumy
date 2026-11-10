package com.edumy.modules.quiz.repository;

import com.edumy.modules.quiz.model.QuizAttempt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Integer> {
    boolean existsByQuizIdAndUserIdAndIsPassedTrue(Integer quizId, Integer userId);
    long countByUserIdAndIsPassedTrueAndQuizIdIn(Integer userId, java.util.Collection<Integer> quizIds);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT qa.quizId FROM QuizAttempt qa WHERE qa.userId = :userId AND qa.isPassed = true AND qa.quizId IN :quizIds")
    java.util.List<Integer> findPassedQuizIdsByUserAndQuizIds(@org.springframework.data.repository.query.Param("userId") Integer userId, @org.springframework.data.repository.query.Param("quizIds") java.util.Collection<Integer> quizIds);
}

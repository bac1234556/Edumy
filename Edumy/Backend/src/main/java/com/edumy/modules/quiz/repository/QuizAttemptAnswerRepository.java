package com.edumy.modules.quiz.repository;

import com.edumy.modules.quiz.model.QuizAttemptAnswer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizAttemptAnswerRepository extends JpaRepository<QuizAttemptAnswer, Integer> {
}

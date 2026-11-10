package com.edumy.modules.quiz.repository;

import com.edumy.modules.quiz.model.Answer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Integer> {
}

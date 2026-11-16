package com.edumy.learning.qa.repository;

import com.edumy.learning.qa.entity.QuestionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionAnswerRepository extends JpaRepository<QuestionAnswer, Long> {

    List<QuestionAnswer> findByQuestionIdAndVisibilityStatusOrderByCreatedAtAsc(Long questionId, String visibilityStatus);

    long countByQuestionIdAndVisibilityStatus(Long questionId, String visibilityStatus);

    boolean existsByQuestionIdAndAuthorTypeAndVisibilityStatus(Long questionId, String authorType, String visibilityStatus);
}

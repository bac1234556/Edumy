package com.edumy.learning.quiz.service;

import com.edumy.learning.quiz.dto.QuizResultDto;
import com.edumy.learning.quiz.dto.QuizSubmissionDto;
import com.edumy.learning.quiz.dto.QuizSubmitRequest;

public interface QuizAttemptService {
    QuizResultDto submitQuiz(Long quizId, QuizSubmitRequest request);
    QuizResultDto submitQuiz(Integer quizId, QuizSubmissionDto submission, Integer currentUserId);
}

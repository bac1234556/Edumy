package com.edumy.modules.quiz.service;

import com.edumy.modules.quiz.dto.QuizResultDto;
import com.edumy.modules.quiz.dto.QuizSubmissionDto;
import com.edumy.modules.quiz.dto.QuizSubmitRequest;

public interface QuizAttemptService {
    QuizResultDto submitQuiz(Long quizId, QuizSubmitRequest request);
    QuizResultDto submitQuiz(Integer quizId, QuizSubmissionDto submission, Integer currentUserId);
}

package com.edumy.backend.service;

import com.edumy.backend.dto.QuizResultDto;
import com.edumy.backend.dto.QuizSubmissionDto;
import com.edumy.backend.dto.QuizSubmitRequest;

public interface QuizAttemptService {
    QuizResultDto submitQuiz(Long quizId, QuizSubmitRequest request);
    QuizResultDto submitQuiz(Integer quizId, QuizSubmissionDto submission, Integer currentUserId);
}

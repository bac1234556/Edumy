package com.edumy.backend.service;

import com.edumy.backend.dto.QuizDto;

import java.util.List;

public interface QuizService {
    QuizDto getQuizForStudent(Integer quizId, Integer currentUserId);
    List<QuizDto> getQuizzesBySection(Integer sectionId, Integer currentUserId);
    QuizDto createQuiz(QuizDto dto, Integer currentUserId);
    void deleteQuiz(Integer quizId, Integer currentUserId);
}

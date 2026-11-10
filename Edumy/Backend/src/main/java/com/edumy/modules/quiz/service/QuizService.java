package com.edumy.modules.quiz.service;

import com.edumy.modules.quiz.dto.QuizDto;

import java.util.List;

public interface QuizService {
    QuizDto getQuizForStudent(Integer quizId, Integer currentUserId);
    List<QuizDto> getQuizzesBySection(Integer sectionId, Integer currentUserId);
    QuizDto createQuiz(QuizDto dto, Integer currentUserId);
    void deleteQuiz(Integer quizId, Integer currentUserId);
}

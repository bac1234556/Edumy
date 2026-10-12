package com.edumy.backend.controller;

import com.edumy.backend.model.*;
import com.edumy.backend.dto.*;
import com.edumy.backend.repository.*;
import com.edumy.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/Quizzes")
public class QuizzesController {

    @Autowired
    private QuizRepository quizRepository;

    @GetMapping("/section/{sectionId}")
    public ResponseEntity<?> getQuizzesBySection(@PathVariable Integer sectionId) {
        List<QuizDto> quizzes = quizRepository.findAll().stream()
                .filter(q -> sectionId.equals(q.getCourseSectionId()))
                .map(q -> {
                    QuizDto dto = new QuizDto();
                    dto.setQuizId(q.getQuizId());
                    dto.setTitle(q.getTitle());
                    dto.setDescription(q.getDescription());
                    dto.setPassingScore(q.getPassingScore());
                    dto.setTimeLimitMinutes(q.getTimeLimitMinutes());
                    dto.setCourseSectionId(q.getCourseSectionId());
                    return dto;
                }).collect(Collectors.toList());

        return ResponseEntity.ok(quizzes);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getQuizForStudent(@PathVariable Integer id) {
        Quiz quiz = quizRepository.findById(id).orElse(null);
        if (quiz == null) return ResponseEntity.notFound().build();

        QuizDto dto = new QuizDto();
        dto.setQuizId(quiz.getQuizId());
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());
        dto.setPassingScore(quiz.getPassingScore());
        dto.setTimeLimitMinutes(quiz.getTimeLimitMinutes());
        dto.setCourseSectionId(quiz.getCourseSectionId());
        
        List<QuestionDto> questions = new ArrayList<>();
        if (quiz.getQuestions() != null) {
            questions = quiz.getQuestions().stream().map(q -> {
                QuestionDto qDto = new QuestionDto();
                qDto.setQuestionId(q.getQuestionId());
                qDto.setContent(q.getContent());
                qDto.setPoints(q.getPoints());
                
                List<AnswerDto> answers = new ArrayList<>();
                if (q.getAnswers() != null) {
                    answers = q.getAnswers().stream().map(a -> {
                        AnswerDto aDto = new AnswerDto();
                        aDto.setAnswerId(a.getAnswerId());
                        aDto.setContent(a.getContent());
                        return aDto;
                    }).collect(Collectors.toList());
                }
                qDto.setAnswers(answers);
                return qDto;
            }).collect(Collectors.toList());
        }
        dto.setQuestions(questions);

        return ResponseEntity.ok(dto);
    }
}

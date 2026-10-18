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
import java.util.Map;

@RestController
@RequestMapping({"/api/Quizzes", "/api/quizzes"})
public class QuizzesController {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private CourseSectionRepository courseSectionRepository;

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
                qDto.setExplanation(q.getExplanation());
                
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

    @PostMapping
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> createQuiz(@RequestBody QuizDto dto) {
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Tiêu đề bài trắc nghiệm không được để trống."));
        }

        Quiz quiz = new Quiz();
        quiz.setTitle(dto.getTitle().trim());
        quiz.setDescription(dto.getDescription());
        quiz.setPassingScore(dto.getPassingScore() != null ? dto.getPassingScore() : 80);
        quiz.setTimeLimitMinutes(dto.getTimeLimitMinutes());
        quiz.setCourseSectionId(dto.getCourseSectionId());

        Quiz savedQuiz = quizRepository.save(quiz);

        if (dto.getQuestions() != null && !dto.getQuestions().isEmpty()) {
            for (QuestionDto qDto : dto.getQuestions()) {
                Question question = new Question();
                question.setContent(qDto.getContent());
                question.setPoints(qDto.getPoints() != null ? qDto.getPoints() : 1);
                question.setExplanation(qDto.getExplanation());
                question.setQuizId(savedQuiz.getQuizId());
                Question savedQuestion = questionRepository.save(question);

                if (qDto.getAnswers() != null && !qDto.getAnswers().isEmpty()) {
                    for (AnswerDto aDto : qDto.getAnswers()) {
                        Answer answer = new Answer();
                        answer.setContent(aDto.getContent());
                        answer.setIsCorrect(Boolean.TRUE.equals(aDto.getIsCorrect()));
                        answer.setQuestionId(savedQuestion.getQuestionId());
                        answerRepository.save(answer);
                    }
                }
            }
        }

        dto.setQuizId(savedQuiz.getQuizId());
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> deleteQuiz(@PathVariable Integer id) {
        Quiz quiz = quizRepository.findById(id).orElse(null);
        if (quiz == null) return ResponseEntity.notFound().build();

        if (quiz.getQuestions() != null) {
            for (Question q : quiz.getQuestions()) {
                if (q.getAnswers() != null) {
                    answerRepository.deleteAll(q.getAnswers());
                }
            }
            questionRepository.deleteAll(quiz.getQuestions());
        }

        quizRepository.delete(quiz);
        return ResponseEntity.noContent().build();
    }
}

package com.edumy.backend.service.impl;

import com.edumy.backend.dto.AnswerDto;
import com.edumy.backend.dto.QuestionDto;
import com.edumy.backend.dto.QuizDto;
import com.edumy.backend.model.Answer;
import com.edumy.backend.model.Course;
import com.edumy.backend.model.CourseSection;
import com.edumy.backend.model.Question;
import com.edumy.backend.model.Quiz;
import com.edumy.backend.model.User;
import com.edumy.backend.repository.AnswerRepository;
import com.edumy.backend.repository.CourseRepository;
import com.edumy.backend.repository.CourseSectionRepository;
import com.edumy.backend.repository.EnrollmentRepository;
import com.edumy.backend.repository.QuestionRepository;
import com.edumy.backend.repository.QuizRepository;
import com.edumy.backend.repository.UserRepository;
import com.edumy.backend.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class QuizServiceImpl implements QuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private CourseSectionRepository courseSectionRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    private boolean isCurrentUserAdmin(Integer userId) {
        if (userId == null) return false;
        boolean hasAdminAuthority = SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_Admin") ||
                                a.getAuthority().equalsIgnoreCase("ROLE_ADMIN") ||
                                a.getAuthority().equalsIgnoreCase("Admin"));
        if (hasAdminAuthority) return true;

        User user = userRepository.findById(userId).orElse(null);
        return user != null && "Admin".equalsIgnoreCase(user.getRole());
    }

    private void validateQuizAccess(Quiz quiz, Integer currentUserId) {
        if (currentUserId == null) {
            throw new AccessDeniedException("Bạn chưa đăng ký khóa học này nên không thể xem bài thi trắc nghiệm!");
        }

        if (isCurrentUserAdmin(currentUserId)) {
            return;
        }

        Integer sectionId = quiz.getCourseSectionId();
        CourseSection section = quiz.getCourseSection();
        if (section == null && sectionId != null) {
            section = courseSectionRepository.findById(sectionId).orElse(null);
        }

        if (section == null) {
            throw new NoSuchElementException("Không tìm thấy chương học của bài kiểm tra này.");
        }

        Integer courseId = section.getCourseId();
        Course course = section.getCourse();
        if (course == null && courseId != null) {
            course = courseRepository.findById(courseId).orElse(null);
        }

        // Nếu là Giảng viên sở hữu khóa học: Cho phép xem
        if (course != null && course.getInstructorId() != null && course.getInstructorId().equals(currentUserId)) {
            return;
        }

        // Kiểm tra học viên có đăng ký khóa học hợp lệ không (ACTIVE hoặc COMPLETED)
        boolean hasActiveEnrollment = false;
        if (courseId != null) {
            hasActiveEnrollment = enrollmentRepository.isUserEnrolledActive(currentUserId, courseId);
        }

        if (!hasActiveEnrollment) {
            throw new AccessDeniedException("Bạn chưa đăng ký khóa học này nên không thể xem bài thi trắc nghiệm!");
        }
    }

    @Override
    public QuizDto getQuizForStudent(Integer quizId, Integer currentUserId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new NoSuchElementException("Bài thi trắc nghiệm không tồn tại với ID: " + quizId));

        // 1. Xác thực quyền sở hữu / enrollment ACTIVE chống IDOR
        validateQuizAccess(quiz, currentUserId);

        // 2. Chuyển đổi sang QuizDto và TUYỆT ĐỐI KHÔNG TRẢ VỀ isCorrect để chống gian lận DevTools
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
                // Không để lộ explanation trước khi nộp bài
                qDto.setExplanation(null);

                List<AnswerDto> answers = new ArrayList<>();
                if (q.getAnswers() != null) {
                    answers = q.getAnswers().stream().map(a -> {
                        AnswerDto aDto = new AnswerDto();
                        aDto.setAnswerId(a.getAnswerId());
                        aDto.setContent(a.getContent());
                        // TUYỆT ĐỐI set isCorrect = null để loại bỏ hoàn toàn khỏi JSON response
                        aDto.setIsCorrect(null);
                        return aDto;
                    }).collect(Collectors.toList());
                }
                qDto.setAnswers(answers);
                return qDto;
            }).collect(Collectors.toList());
        }
        dto.setQuestions(questions);

        return dto;
    }

    @Override
    public List<QuizDto> getQuizzesBySection(Integer sectionId, Integer currentUserId) {
        return quizRepository.findAll().stream()
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
    }

    @Override
    @Transactional
    public QuizDto createQuiz(QuizDto dto, Integer currentUserId) {
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Tiêu đề bài trắc nghiệm không được để trống.");
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
        return dto;
    }

    @Override
    @Transactional
    public void deleteQuiz(Integer quizId, Integer currentUserId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy bài trắc nghiệm ID: " + quizId));

        if (quiz.getQuestions() != null) {
            for (Question q : quiz.getQuestions()) {
                if (q.getAnswers() != null) {
                    answerRepository.deleteAll(q.getAnswers());
                }
            }
            questionRepository.deleteAll(quiz.getQuestions());
        }

        quizRepository.delete(quiz);
    }
}

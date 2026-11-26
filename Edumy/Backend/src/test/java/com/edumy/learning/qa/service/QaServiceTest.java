package com.edumy.learning.qa.service;

import com.edumy.authoring.model.Course;
import com.edumy.authoring.repository.CourseRepository;
import com.edumy.identity.user.model.User;
import com.edumy.identity.user.repository.UserRepository;
import com.edumy.learning.enrollment.repository.EnrollmentRepository;
import com.edumy.learning.qa.dto.AnswerResponse;
import com.edumy.learning.qa.dto.CreateAnswerRequest;
import com.edumy.learning.qa.dto.QuestionSummaryResponse;
import com.edumy.learning.qa.entity.CourseQuestion;
import com.edumy.learning.qa.entity.QuestionAnswer;
import com.edumy.learning.qa.repository.CourseQuestionRepository;
import com.edumy.learning.qa.repository.QuestionAnswerRepository;
import com.edumy.notification.service.MailService;
import com.edumy.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QaServiceTest {

    @Mock
    private CourseQuestionRepository questionRepository;

    @Mock
    private QuestionAnswerRepository answerRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MailService mailService;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    private QaService qaService;

    @BeforeEach
    void setUp() {
        qaService = new QaService(questionRepository, answerRepository, courseRepository, userRepository, mailService);
        ReflectionTestUtils.setField(qaService, "enrollmentRepository", enrollmentRepository);
    }

    @Test
    @DisplayName("Enrolled student B can answer student A's question in the same course")
    void testEnrolledPeerStudentCanAnswerQuestion() {
        CourseQuestion question = CourseQuestion.builder()
                .id(10L)
                .courseId(5)
                .userId(100)
                .title("Câu hỏi về Spring")
                .content("Nội dung câu hỏi")
                .visibilityStatus("VISIBLE")
                .isResolved(false)
                .build();
        User studentB = new User();
        studentB.setUserId(200);
        studentB.setFullName("Học viên B");
        studentB.setRole("Student");

        Course course = new Course();
        course.setCourseId(5);
        course.setInstructorId(1);
        course.setTitle("Spring Boot Mastery");

        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));
        when(userRepository.findById(200)).thenReturn(Optional.of(studentB));
        when(courseRepository.findById(5)).thenReturn(Optional.of(course));
        when(enrollmentRepository.isUserEnrolledActive(200, 5)).thenReturn(true);
        when(answerRepository.save(any())).thenAnswer(inv -> {
            QuestionAnswer ans = inv.getArgument(0);
            ans.setId(99L);
            return ans;
        });

        CreateAnswerRequest req = new CreateAnswerRequest();
        req.setContent("Bạn thử kiểm tra lại annotation @Service nhé!");

        AnswerResponse response = qaService.postAnswer(200, "Student", 10L, req);

        assertThat(response.getId()).isEqualTo(99L);
        assertThat(response.getAuthorId()).isEqualTo(200);
        assertThat(response.getAuthorType()).isEqualTo("STUDENT");
        assertThat(response.getContent()).isEqualTo("Bạn thử kiểm tra lại annotation @Service nhé!");
    }

    @Test
    @DisplayName("Non-enrolled student cannot answer a question in the course")
    void testUnenrolledStudentCannotAnswerQuestion() {
        CourseQuestion question = CourseQuestion.builder()
                .id(10L)
                .courseId(5)
                .userId(100)
                .visibilityStatus("VISIBLE")
                .build();
        User stranger = new User();
        stranger.setUserId(300);
        stranger.setRole("Student");

        Course course = new Course();
        course.setCourseId(5);
        course.setInstructorId(1);

        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));
        when(userRepository.findById(300)).thenReturn(Optional.of(stranger));
        when(courseRepository.findById(5)).thenReturn(Optional.of(course));
        when(enrollmentRepository.isUserEnrolledActive(300, 5)).thenReturn(false);

        CreateAnswerRequest req = new CreateAnswerRequest();
        req.setContent("Trả lời trái phép");

        assertThatThrownBy(() -> qaService.postAnswer(300, "Student", 10L, req))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    @DisplayName("Cannot mark question as resolved when it has 0 visible answers")
    void testSetResolvedFailsWithoutAnswers() {
        CourseQuestion question = CourseQuestion.builder()
                .id(10L)
                .courseId(5)
                .userId(100)
                .visibilityStatus("VISIBLE")
                .isResolved(false)
                .build();
        User asker = new User();
        asker.setUserId(100);
        asker.setRole("Student");

        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));
        when(courseRepository.findById(5)).thenReturn(Optional.of(new Course()));
        when(userRepository.findById(100)).thenReturn(Optional.of(asker));
        when(answerRepository.countByQuestionIdAndVisibilityStatus(10L, "VISIBLE")).thenReturn(0L);

        assertThatThrownBy(() -> qaService.setResolved(100, 10L, true))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ít nhất 1 câu trả lời")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("Can mark question as resolved when at least 1 visible answer exists")
    void testSetResolvedSucceedsWithAtLeastOneAnswer() {
        CourseQuestion question = CourseQuestion.builder()
                .id(10L)
                .courseId(5)
                .userId(100)
                .visibilityStatus("VISIBLE")
                .isResolved(false)
                .createdAt(LocalDateTime.now())
                .build();
        User asker = new User();
        asker.setUserId(100);
        asker.setFullName("Học viên A");
        asker.setRole("Student");

        Course course = new Course();
        course.setCourseId(5);
        course.setTitle("Spring Boot");

        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));
        when(courseRepository.findById(5)).thenReturn(Optional.of(course));
        when(userRepository.findById(100)).thenReturn(Optional.of(asker));
        when(answerRepository.countByQuestionIdAndVisibilityStatus(10L, "VISIBLE")).thenReturn(1L);
        when(answerRepository.existsByQuestionIdAndAuthorTypeAndVisibilityStatus(10L, "INSTRUCTOR", "VISIBLE"))
                .thenReturn(false);

        QuestionSummaryResponse summary = qaService.setResolved(100, 10L, true);

        assertThat(summary.getIsResolved()).isTrue();
        verify(questionRepository).save(question);
    }

    @Test
    @DisplayName("Deleting last visible answer resets resolved flag on question")
    void testDeleteLastAnswerUnresolvesQuestion() {
        QuestionAnswer answer = QuestionAnswer.builder()
                .id(55L)
                .questionId(10L)
                .authorId(200)
                .visibilityStatus("VISIBLE")
                .build();
        CourseQuestion question = CourseQuestion.builder()
                .id(10L)
                .courseId(5)
                .userId(100)
                .isResolved(true)
                .visibilityStatus("VISIBLE")
                .build();

        when(answerRepository.findById(55L)).thenReturn(Optional.of(answer));
        when(answerRepository.countByQuestionIdAndVisibilityStatus(10L, "VISIBLE")).thenReturn(0L);
        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));

        qaService.deleteAnswer(200, "Student", 55L);

        assertThat(answer.getVisibilityStatus()).isEqualTo("DELETED");
        assertThat(question.getIsResolved()).isFalse();
        verify(questionRepository).save(question);
    }
}

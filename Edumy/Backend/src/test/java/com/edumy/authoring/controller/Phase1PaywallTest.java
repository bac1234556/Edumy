package com.edumy.authoring.controller;

import com.edumy.authoring.model.Course;
import com.edumy.authoring.model.CourseSection;
import com.edumy.authoring.model.Lesson;
import com.edumy.authoring.repository.CourseRepository;
import com.edumy.authoring.repository.CourseSectionRepository;
import com.edumy.authoring.repository.LessonRepository;
import com.edumy.commerce.order.repository.OrderRepository;
import com.edumy.identity.user.repository.UserRepository;
import com.edumy.learning.enrollment.repository.EnrollmentRepository;
import com.edumy.learning.enrollment.repository.LessonProgressRepository;
import com.edumy.learning.review.repository.ReviewRepository;
import com.edumy.notification.service.MailService;
import com.edumy.shared.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Phase1PaywallTest {

    @Mock private UserRepository userRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private CourseSectionRepository courseSectionRepository;
    @Mock private LessonRepository lessonRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private LessonProgressRepository lessonProgressRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private MailService mailService;

    private SpecUseCaseController controller;

    @BeforeEach
    void setUp() {
        controller = new SpecUseCaseController(
                userRepository, courseRepository, courseSectionRepository,
                lessonRepository, enrollmentRepository, lessonProgressRepository,
                orderRepository, reviewRepository, mailService
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(int userId, String role) {
        var auth = new UsernamePasswordAuthenticationToken(
                String.valueOf(userId),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Previewing a non-preview lesson returns 403 LESSON_NOT_PREVIEWABLE")
    void previewNonPreviewLessonRejected() {
        Course course = Course.builder().courseId(10).slug("java-101").instructorId(99).status("Published").build();
        CourseSection section = CourseSection.builder().sectionId(20).courseId(10).title("Sec 1").build();
        Lesson lesson = Lesson.builder().lessonId(30).sectionId(20).title("Secret").isPreview(false).build();

        when(courseRepository.findAll()).thenReturn(List.of(course));
        when(lessonRepository.findById(30)).thenReturn(Optional.of(lesson));
        when(courseSectionRepository.findById(20)).thenReturn(Optional.of(section));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> controller.getCatalogLessonPreview("java-101", 30));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("LESSON_NOT_PREVIEWABLE", ex.getCode());
    }

    @Test
    @DisplayName("Previewing a lesson from a different course returns 403 LESSON_NOT_PREVIEWABLE")
    void previewLessonFromWrongCourseRejected() {
        Course course = Course.builder().courseId(10).slug("java-101").instructorId(99).status("Published").build();
        CourseSection otherSection = CourseSection.builder().sectionId(21).courseId(999).title("Other").build();
        Lesson lesson = Lesson.builder().lessonId(31).sectionId(21).title("Preview Elsewhere").isPreview(true).build();

        when(courseRepository.findAll()).thenReturn(List.of(course));
        when(lessonRepository.findById(31)).thenReturn(Optional.of(lesson));
        when(courseSectionRepository.findById(21)).thenReturn(Optional.of(otherSection));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> controller.getCatalogLessonPreview("java-101", 31));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("LESSON_NOT_PREVIEWABLE", ex.getCode());
    }

    @Test
    @DisplayName("Accessing learn room without enrollment returns 403 NOT_ENROLLED")
    void learnRoomWithoutEnrollmentRejected() {
        authenticateAs(5, "Student");
        Course course = Course.builder().courseId(10).instructorId(99).status("Published").build();

        when(courseRepository.findById(10)).thenReturn(Optional.of(course));
        when(enrollmentRepository.isUserEnrolledActive(5, 10)).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> controller.getLearnCourseRoom(10));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("NOT_ENROLLED", ex.getCode());
    }

    @Test
    @DisplayName("Marking lesson completion without enrollment returns 403 NOT_ENROLLED")
    void completionWithoutEnrollmentRejected() {
        authenticateAs(5, "Student");
        Course course = Course.builder().courseId(10).instructorId(99).status("Published").build();
        CourseSection section = CourseSection.builder().sectionId(20).courseId(10).build();
        Lesson lesson = Lesson.builder().lessonId(30).sectionId(20).build();

        when(lessonRepository.findById(30)).thenReturn(Optional.of(lesson));
        when(courseSectionRepository.findById(20)).thenReturn(Optional.of(section));
        when(courseRepository.findById(10)).thenReturn(Optional.of(course));
        when(enrollmentRepository.isUserEnrolledActive(5, 10)).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> controller.setLessonCompletionV1(30, Map.of("completed", true)));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("NOT_ENROLLED", ex.getCode());
    }
}

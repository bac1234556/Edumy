package com.edumy.authoring.controller;

import com.edumy.authoring.model.Course;
import com.edumy.authoring.model.CourseCoInstructor;
import com.edumy.authoring.model.CourseSection;
import com.edumy.authoring.model.Lesson;
import com.edumy.authoring.repository.CourseCoInstructorRepository;
import com.edumy.authoring.repository.CourseRepository;
import com.edumy.authoring.repository.CourseSectionRepository;
import com.edumy.authoring.repository.LessonRepository;
import com.edumy.commerce.order.repository.OrderRepository;
import com.edumy.identity.user.model.User;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseLifecycleAndBr07QaTest {

    @Mock private UserRepository userRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private CourseSectionRepository courseSectionRepository;
    @Mock private LessonRepository lessonRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private LessonProgressRepository lessonProgressRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private MailService mailService;
    @Mock private CourseCoInstructorRepository courseCoInstructorRepository;

    private SpecUseCaseController controller;

    @BeforeEach
    void setUp() {
        controller = new SpecUseCaseController(
                userRepository, courseRepository, courseSectionRepository, lessonRepository,
                enrollmentRepository, lessonProgressRepository, orderRepository, reviewRepository, mailService
        );
        ReflectionTestUtils.setField(controller, "courseCoInstructorRepository", courseCoInstructorRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authAs(int userId, String role) {
        var auth = new UsernamePasswordAuthenticationToken(
                String.valueOf(userId), null, List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private Course validDraftCourse() {
        return Course.builder()
                .courseId(100)
                .instructorId(20)
                .categoryId(1)
                .title("Spring Boot Thực Chiến")
                .slug("spring-boot-thuc-chien")
                .description("Mô tả chi tiết khóa học Spring Boot dài hơn 10 ký tự.")
                .price(BigDecimal.valueOf(100000))
                .status("Draft")
                .build();
    }

    @Test
    @DisplayName("BR-07: Reject submit when title < 5 chars")
    void br07RejectsShortTitle() {
        authAs(20, "Instructor");
        Course c = validDraftCourse();
        c.setTitle("Abcd");
        when(courseRepository.findById(100)).thenReturn(Optional.of(c));

        BusinessException ex = assertThrows(BusinessException.class, () -> controller.submitCourseForReview(100));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    @DisplayName("BR-07: Reject submit when description < 10 chars")
    void br07RejectsShortDescription() {
        authAs(20, "Instructor");
        Course c = validDraftCourse();
        c.setDescription("123456789");
        when(courseRepository.findById(100)).thenReturn(Optional.of(c));

        BusinessException ex = assertThrows(BusinessException.class, () -> controller.submitCourseForReview(100));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    @DisplayName("BR-07: Reject submit when paid price < 10,000 or not multiple of 1,000")
    void br07RejectsInvalidPrice() {
        authAs(20, "Instructor");
        Course c = validDraftCourse();
        c.setPrice(BigDecimal.valueOf(5000));
        when(courseRepository.findById(100)).thenReturn(Optional.of(c));

        BusinessException ex1 = assertThrows(BusinessException.class, () -> controller.submitCourseForReview(100));
        assertEquals(HttpStatus.BAD_REQUEST, ex1.getStatus());

        c.setPrice(BigDecimal.valueOf(10500));
        BusinessException ex2 = assertThrows(BusinessException.class, () -> controller.submitCourseForReview(100));
        assertEquals(HttpStatus.BAD_REQUEST, ex2.getStatus());
    }

    @Test
    @DisplayName("BR-07: Reject submit when course has 0 sections or empty section or empty lesson content")
    void br07RejectsEmptyCurriculum() {
        authAs(20, "Instructor");
        Course c = validDraftCourse();
        when(courseRepository.findById(100)).thenReturn(Optional.of(c));
        when(courseSectionRepository.findAll()).thenReturn(List.of());

        assertThrows(BusinessException.class, () -> controller.submitCourseForReview(100));

        CourseSection sec = CourseSection.builder().sectionId(10).courseId(100).title("Chương 1").build();
        when(courseSectionRepository.findAll()).thenReturn(List.of(sec));
        when(lessonRepository.findAll()).thenReturn(List.of());
        assertThrows(BusinessException.class, () -> controller.submitCourseForReview(100));

        Lesson emptyLes = Lesson.builder().lessonId(1).sectionId(10).title("Bài trống").build();
        when(lessonRepository.findAll()).thenReturn(List.of(emptyLes));
        assertThrows(BusinessException.class, () -> controller.submitCourseForReview(100));
    }

    @Test
    @DisplayName("D-13 State Transition: Draft -> PendingApproval -> Draft (withdraw) -> Pending -> NeedsReview -> Pending -> Published -> Hidden -> Published")
    void fullCourseStateTransitionLifecycle() {
        Course c = validDraftCourse();
        CourseSection sec = CourseSection.builder().sectionId(10).courseId(100).title("Chương 1").build();
        Lesson les = Lesson.builder().lessonId(1).sectionId(10).title("Bài 1").contentText("Nội dung bài học").build();
        User inst = User.builder().userId(20).email("gv@edumy.test").fullName("GV A").build();

        when(courseRepository.findById(100)).thenReturn(Optional.of(c));
        when(courseSectionRepository.findAll()).thenReturn(List.of(sec));
        when(lessonRepository.findAll()).thenReturn(List.of(les));
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(20)).thenReturn(Optional.of(inst));

        // 1. Submit: Draft -> PendingApproval (as owner Instructor)
        authAs(20, "Instructor");
        ResponseEntity<?> subRes = controller.submitCourseForReview(100);
        assertEquals(HttpStatus.OK, subRes.getStatusCode());
        assertEquals("PendingApproval", c.getStatus());

        // 2. Withdraw: PendingApproval -> Draft
        ResponseEntity<?> wdRes = controller.withdrawCourseSubmission(100);
        assertEquals(HttpStatus.OK, wdRes.getStatusCode());
        assertEquals("Draft", c.getStatus());

        // 3. Re-submit -> Reject without reason blocked -> Reject with reason -> NeedsReview + email
        controller.submitCourseForReview(100);
        authAs(1, "Admin");
        assertThrows(BusinessException.class, () -> controller.rejectCourse(100, Map.of("reviewNote", "   ")));
        controller.rejectCourse(100, Map.of("reviewNote", "Thiếu ví dụ"));
        assertEquals("NeedsReview", c.getStatus());
        assertEquals("Thiếu ví dụ", c.getReviewNote());
        verify(mailService, times(1)).sendCourseRejected("gv@edumy.test", "GV A", c.getTitle(), 100L, "Thiếu ví dụ");

        // 4. Re-submit after fix -> Approve -> Published + email
        authAs(20, "Instructor");
        controller.submitCourseForReview(100);
        authAs(1, "Admin");
        controller.approveCourse(100);
        assertEquals("Published", c.getStatus());
        verify(mailService, times(1)).sendCourseApproved("gv@edumy.test", "GV A", c.getTitle(), c.getSlug());

        // 5. Hide -> Hidden + email -> Unhide -> Published
        controller.hideCourse(100, Map.of("reason", "Bảo trì nội dung"));
        assertEquals("Hidden", c.getStatus());
        verify(mailService, times(1)).sendCourseHidden("gv@edumy.test", "GV A", c.getTitle(), 100L, "Bảo trì nội dung");

        controller.unhideCourse(100);
        assertEquals("Published", c.getStatus());
    }

    @Test
    @DisplayName("DEF-W-003 IDOR: Unrelated instructor cannot submit another instructor's course")
    void unrelatedInstructorCannotSubmitCourse() {
        authAs(21, "Instructor");
        Course c = validDraftCourse();
        when(courseRepository.findById(100)).thenReturn(Optional.of(c));

        BusinessException ex = assertThrows(BusinessException.class, () -> controller.submitCourseForReview(100));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    @DisplayName("Authorization: Co-instructor cannot update shareBps (only main instructor or Admin)")
    void coInstructorBlockedFromChangingShareBps() {
        authAs(22, "Instructor"); // Co-instructor ID 22, NOT main instructor 20
        Course c = validDraftCourse();
        when(courseRepository.findById(100)).thenReturn(Optional.of(c));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> controller.updateCoInstructorShare(100, 22, Map.of("shareBps", 5000)));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    @DisplayName("BR-17: Updating co-instructor shareBps > 10000 rejected with 422")
    void shareBpsAbove10000Rejected() {
        authAs(20, "Instructor");
        Course c = validDraftCourse();
        when(courseRepository.findById(100)).thenReturn(Optional.of(c));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> controller.updateCoInstructorShare(100, 22, Map.of("shareBps", 10001)));
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatus());
    }
}

package com.edumy.learning.enrollment.service;

import com.edumy.authoring.controller.CoursesController;
import com.edumy.authoring.model.Course;
import com.edumy.authoring.repository.CourseCoInstructorRepository;
import com.edumy.authoring.repository.CourseRepository;
import com.edumy.authoring.repository.CourseSectionRepository;
import com.edumy.authoring.repository.LessonRepository;
import com.edumy.commerce.order.repository.OrderItemRepository;
import com.edumy.identity.user.controller.InstructorChannelController;
import com.edumy.identity.user.controller.InstructorController;
import com.edumy.identity.user.model.User;
import com.edumy.learning.enrollment.model.Certificate;
import com.edumy.learning.enrollment.model.Enrollment;
import com.edumy.learning.enrollment.model.LessonProgress;
import com.edumy.learning.enrollment.repository.CertificateRepository;
import com.edumy.learning.enrollment.repository.EnrollmentRepository;
import com.edumy.learning.enrollment.repository.LessonProgressRepository;
import com.edumy.learning.qa.repository.CourseQuestionRepository;
import com.edumy.learning.quiz.repository.QuizAttemptRepository;
import com.edumy.learning.quiz.repository.QuizRepository;
import com.edumy.learning.review.repository.ReviewRepository;
import com.edumy.payout.repository.EarningEntryRepository;
import com.edumy.payout.service.EarningService;
import com.edumy.shared.response.PageResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearningProgressConsistencyTest {

    @Mock private LessonRepository lessonRepository;
    @Mock private CourseSectionRepository courseSectionRepository;
    @Mock private LessonProgressRepository lessonProgressRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private QuizRepository quizRepository;
    @Mock private QuizAttemptRepository quizAttemptRepository;
    @Mock private CertificateRepository certificateRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private CourseCoInstructorRepository courseCoInstructorRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private CourseQuestionRepository courseQuestionRepository;
    @Mock private EarningEntryRepository earningEntryRepository;
    @Mock private EarningService earningService;

    private LearningProgressService learningProgressService;
    private CoursesController coursesController;

    @BeforeEach
    void setUp() {
        learningProgressService = new LearningProgressService(
                lessonRepository,
                courseSectionRepository,
                lessonProgressRepository,
                enrollmentRepository,
                quizRepository,
                quizAttemptRepository,
                certificateRepository
        );
        ReflectionTestUtils.setField(learningProgressService, "courseRepository", courseRepository);

        coursesController = new CoursesController();
        ReflectionTestUtils.setField(coursesController, "enrollmentRepository", enrollmentRepository);
        ReflectionTestUtils.setField(coursesController, "learningProgressService", learningProgressService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private List<LessonProgress> buildLessonProgresses(int userId, int courseId, List<Integer> lessonIds) {
        return lessonIds.stream().map(lid -> {
            LessonProgress lp = new LessonProgress();
            lp.setUserId(userId);
            lp.setCourseId(courseId);
            lp.setLessonId(lid);
            lp.setCompletedAt(LocalDateTime.now());
            return lp;
        }).toList();
    }

    @Test
    @DisplayName("8 completed lessons + 1 unpassed quiz => 8/9 (88%) consistently across /courses/enrolled and /courses/{id}/progress")
    @SuppressWarnings("unchecked")
    void progressWithUnpassedQuizIsConsistentAcrossEnrolledAndCourseProgress() {
        int userId = 5;
        int courseId = 1;
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(String.valueOf(userId), null,
                        List.of(new SimpleGrantedAuthority("ROLE_Student")))
        );

        List<Integer> eightLessons = IntStream.rangeClosed(1, 8).boxed().toList();
        when(lessonRepository.findLessonIdsByCourseId(courseId)).thenReturn(eightLessons);
        when(lessonProgressRepository.findByUserIdAndCourseId(userId, courseId))
                .thenReturn(buildLessonProgresses(userId, courseId, eightLessons));
        when(quizRepository.findQuizIdsByCourseId(courseId)).thenReturn(List.of(101));
        when(quizAttemptRepository.findPassedQuizIdsByUserAndQuizIds(userId, List.of(101)))
                .thenReturn(List.of());

        Course course = new Course();
        course.setCourseId(courseId);
        course.setTitle("Lập trình Java Spring Boot 3 & Microservices Thực chiến");
        course.setStatus("Published");

        Enrollment enrollment = Enrollment.builder()
                .enrollmentId(1)
                .userId(userId)
                .courseId(courseId)
                .course(course)
                .enrolledAt(LocalDateTime.now())
                .progressPercentage(100)
                .status("ACTIVE")
                .build();

        when(enrollmentRepository.findAll()).thenReturn(List.of(enrollment));
        when(enrollmentRepository.findByUserIdAndCourseId(userId, courseId)).thenReturn(enrollment);

        ResponseEntity<?> enrolledRes = coursesController.getEnrolledCourses();
        ResponseEntity<?> progressRes = coursesController.getCourseProgress(courseId);

        assertEquals(200, enrolledRes.getStatusCode().value());
        assertEquals(200, progressRes.getStatusCode().value());

        List<Map<String, Object>> enrolledList = (List<Map<String, Object>>) enrolledRes.getBody();
        assertNotNull(enrolledList);
        assertEquals(1, enrolledList.size());
        Map<String, Object> enrolledItem = enrolledList.get(0);

        Map<String, Object> progressData = (Map<String, Object>) progressRes.getBody();
        assertNotNull(progressData);

        assertEquals(88, enrolledItem.get("progressPercentage"));
        assertEquals(88, progressData.get("progressPercentage"));
        assertEquals(8L, enrolledItem.get("completedItems"));
        assertEquals(9L, enrolledItem.get("totalItems"));
        assertEquals(8L, progressData.get("completedItems"));
        assertEquals(9L, progressData.get("totalItems"));

        LearningProgressService.RecalcResult recalc = learningProgressService.recalculateAndIssueCertificate(userId, courseId);
        assertEquals(88, recalc.progressPercent());
        assertFalse(recalc.certificateJustIssued());
    }

    @Test
    @DisplayName("8 completed lessons + 1 passed quiz => 9/9 (100%) consistently across all endpoints and issues certificate")
    @SuppressWarnings("unchecked")
    void progressWithPassedQuizReaches100Consistently() {
        int userId = 5;
        int courseId = 1;
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(String.valueOf(userId), null,
                        List.of(new SimpleGrantedAuthority("ROLE_Student")))
        );

        List<Integer> eightLessons = IntStream.rangeClosed(1, 8).boxed().toList();
        when(lessonRepository.findLessonIdsByCourseId(courseId)).thenReturn(eightLessons);
        when(lessonProgressRepository.findByUserIdAndCourseId(userId, courseId))
                .thenReturn(buildLessonProgresses(userId, courseId, eightLessons));
        when(quizRepository.findQuizIdsByCourseId(courseId)).thenReturn(List.of(101));
        when(quizAttemptRepository.findPassedQuizIdsByUserAndQuizIds(userId, List.of(101)))
                .thenReturn(List.of(101));
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(inv -> {
            Certificate c = inv.getArgument(0);
            c.setId(77);
            return c;
        });

        Enrollment enrollment = Enrollment.builder()
                .enrollmentId(1)
                .userId(userId)
                .courseId(courseId)
                .enrolledAt(LocalDateTime.now())
                .progressPercentage(88)
                .status("ACTIVE")
                .build();
        when(enrollmentRepository.findAll()).thenReturn(List.of(enrollment));
        when(enrollmentRepository.findByUserIdAndCourseId(userId, courseId)).thenReturn(enrollment);

        LearningProgressService.RecalcResult recalc = learningProgressService.recalculateAndIssueCertificate(userId, courseId);
        assertEquals(100, recalc.progressPercent());
        assertTrue(recalc.certificateJustIssued());
        assertNotNull(recalc.certificateCode());

        Certificate issuedCert = Certificate.builder()
                .id(77)
                .userId(userId)
                .courseId(courseId)
                .certificateUrl(recalc.certificateCode())
                .build();
        when(certificateRepository.findByUserIdAndCourseId(userId, courseId)).thenReturn(issuedCert);

        List<Map<String, Object>> enrolledList = (List<Map<String, Object>>) coursesController.getEnrolledCourses().getBody();
        Map<String, Object> progressData = (Map<String, Object>) coursesController.getCourseProgress(courseId).getBody();

        assertEquals(100, enrolledList.get(0).get("progressPercentage"));
        assertEquals(9L, enrolledList.get(0).get("completedItems"));
        assertEquals(9L, enrolledList.get(0).get("totalItems"));
        assertEquals(100, progressData.get("progressPercentage"));
        assertEquals(77, progressData.get("certificateId"));
        assertEquals(recalc.certificateCode(), progressData.get("certificateUrl"));
    }

    @Test
    @DisplayName("Orphaned/duplicate lesson progresses, duplicate passed quiz attempts, and empty courses are handled cleanly")
    void duplicateOrphanedAndEmptyCourseCases() {
        int userId = 5;
        int courseId = 2;

        when(lessonRepository.findLessonIdsByCourseId(courseId)).thenReturn(List.of(1, 2, 3));
        when(lessonProgressRepository.findByUserIdAndCourseId(userId, courseId))
                .thenReturn(buildLessonProgresses(userId, courseId, List.of(1, 1, 2, 999)));
        when(quizRepository.findQuizIdsByCourseId(courseId)).thenReturn(List.of(201));
        when(quizAttemptRepository.findPassedQuizIdsByUserAndQuizIds(userId, List.of(201)))
                .thenReturn(List.of(201, 201));

        LearningProgressService.ProgressSnapshot snap = learningProgressService.getProgress(userId, courseId);
        assertEquals(3L, snap.totalLessons());
        assertEquals(2, snap.completedLessonIds().size());
        assertEquals(1L, snap.totalQuizzes());
        assertEquals(1L, snap.completedQuizzes());
        assertEquals(4L, snap.totalItems());
        assertEquals(3L, snap.completedItems());
        assertEquals(75, snap.progressPercentage());

        // Empty course with 0 lessons and 0 quizzes
        int emptyCourseId = 99;
        when(lessonRepository.findLessonIdsByCourseId(emptyCourseId)).thenReturn(List.of());
        when(lessonProgressRepository.findByUserIdAndCourseId(userId, emptyCourseId)).thenReturn(List.of());
        when(quizRepository.findQuizIdsByCourseId(emptyCourseId)).thenReturn(List.of());

        LearningProgressService.ProgressSnapshot emptySnap = learningProgressService.getProgress(userId, emptyCourseId);
        assertEquals(0L, emptySnap.totalItems());
        assertEquals(0, emptySnap.progressPercentage());
    }

    @Test
    @DisplayName("InstructorController and InstructorChannelController student rosters compute consistent live progress")
    @SuppressWarnings("unchecked")
    void instructorStudentRostersUseConsistentLiveProgress() {
        int instructorId = 2;
        int studentId = 5;
        int courseId = 1;
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(String.valueOf(instructorId), null,
                        List.of(new SimpleGrantedAuthority("ROLE_Instructor")))
        );

        Course course = new Course();
        course.setCourseId(courseId);
        course.setInstructorId(instructorId);
        course.setTitle("Spring Boot 3");

        User student = User.builder().userId(studentId).fullName("Đặng Quốc Hưng").build();
        Enrollment enrollment = Enrollment.builder()
                .enrollmentId(10)
                .userId(studentId)
                .user(student)
                .courseId(courseId)
                .enrolledAt(LocalDateTime.now())
                .progressPercentage(100) // stale DB value
                .status("ACTIVE")
                .build();

        List<Integer> eightLessons = IntStream.rangeClosed(1, 8).boxed().toList();
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByCourseId(courseId)).thenReturn(List.of(enrollment));
        when(enrollmentRepository.findByCourseIdOrderByEnrolledAtDesc(eq(courseId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(enrollment)));
        when(lessonRepository.findLessonIdsByCourseId(courseId)).thenReturn(eightLessons);
        when(lessonProgressRepository.findByUserIdAndCourseId(studentId, courseId))
                .thenReturn(buildLessonProgresses(studentId, courseId, eightLessons));
        when(quizRepository.findQuizIdsByCourseId(courseId)).thenReturn(List.of(101));
        when(quizAttemptRepository.findPassedQuizIdsByUserAndQuizIds(studentId, List.of(101)))
                .thenReturn(List.of());

        // 1. InstructorController with LearningProgressService injected
        InstructorController instCtrl = new InstructorController();
        ReflectionTestUtils.setField(instCtrl, "courseRepository", courseRepository);
        ReflectionTestUtils.setField(instCtrl, "enrollmentRepository", enrollmentRepository);
        ReflectionTestUtils.setField(instCtrl, "learningProgressService", learningProgressService);

        ResponseEntity<?> res1 = instCtrl.getCourseStudents(courseId);
        List<Map<String, Object>> students1 = (List<Map<String, Object>>) res1.getBody();
        assertEquals(88, students1.get(0).get("progressPercentage"));

        // Fallback branch when learningProgressService is null
        ReflectionTestUtils.setField(instCtrl, "learningProgressService", null);
        List<Map<String, Object>> studentsFallback1 = (List<Map<String, Object>>) instCtrl.getCourseStudents(courseId).getBody();
        assertEquals(100, studentsFallback1.get(0).get("progressPercentage"));

        // 2. InstructorChannelController with LearningProgressService injected
        InstructorChannelController channelCtrl = new InstructorChannelController(
                courseRepository,
                courseCoInstructorRepository,
                enrollmentRepository,
                reviewRepository,
                courseQuestionRepository,
                earningEntryRepository,
                earningService
        );
        ReflectionTestUtils.setField(channelCtrl, "timeZone", "Asia/Ho_Chi_Minh");
        ReflectionTestUtils.setField(channelCtrl, "learningProgressService", learningProgressService);

        ResponseEntity<PageResponse<InstructorChannelController.StudentRosterItemDto>> res2 =
                channelCtrl.getCourseStudents(courseId, 0, 20);
        assertEquals(88, res2.getBody().content().get(0).progressPercentage());

        // Fallback branch when learningProgressService is null
        ReflectionTestUtils.setField(channelCtrl, "learningProgressService", null);
        ResponseEntity<PageResponse<InstructorChannelController.StudentRosterItemDto>> resFallback2 =
                channelCtrl.getCourseStudents(courseId, 0, 20);
        assertEquals(100, resFallback2.getBody().content().get(0).progressPercentage());
    }
}

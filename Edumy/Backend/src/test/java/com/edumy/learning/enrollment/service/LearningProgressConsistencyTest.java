package com.edumy.learning.enrollment.service;

import com.edumy.authoring.controller.CoursesController;
import com.edumy.authoring.model.Course;
import com.edumy.authoring.repository.CourseRepository;
import com.edumy.authoring.repository.CourseSectionRepository;
import com.edumy.authoring.repository.LessonRepository;
import com.edumy.learning.enrollment.model.Certificate;
import com.edumy.learning.enrollment.model.Enrollment;
import com.edumy.learning.enrollment.model.LessonProgress;
import com.edumy.learning.enrollment.repository.CertificateRepository;
import com.edumy.learning.enrollment.repository.EnrollmentRepository;
import com.edumy.learning.enrollment.repository.LessonProgressRepository;
import com.edumy.learning.quiz.repository.QuizAttemptRepository;
import com.edumy.learning.quiz.repository.QuizRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
                .progressPercentage(100) // stale DB column from old seed
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

        // Both endpoints must return 88% (8/9 items)
        assertEquals(88, enrolledItem.get("progressPercentage"));
        assertEquals(88, progressData.get("progressPercentage"));
        assertEquals(8L, enrolledItem.get("completedItems"));
        assertEquals(9L, enrolledItem.get("totalItems"));
        assertEquals(8L, progressData.get("completedItems"));
        assertEquals(9L, progressData.get("totalItems"));

        // Recalculate also yields 88% and does not prematurely issue certificate
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
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(inv -> inv.getArgument(0));

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

        List<Map<String, Object>> enrolledList = (List<Map<String, Object>>) coursesController.getEnrolledCourses().getBody();
        Map<String, Object> progressData = (Map<String, Object>) coursesController.getCourseProgress(courseId).getBody();

        assertEquals(100, enrolledList.get(0).get("progressPercentage"));
        assertEquals(9L, enrolledList.get(0).get("completedItems"));
        assertEquals(9L, enrolledList.get(0).get("totalItems"));
        assertEquals(100, progressData.get("progressPercentage"));
        assertEquals(9L, progressData.get("completedItems"));
        assertEquals(9L, progressData.get("totalItems"));
    }

    @Test
    @DisplayName("Orphaned/duplicate lesson progresses and duplicate passed quiz attempts do not inflate progress")
    void duplicateAndOrphanedRecordsDoNotInflateProgress() {
        int userId = 5;
        int courseId = 2;

        // Course only has lessons 1, 2, 3 and quiz 201
        when(lessonRepository.findLessonIdsByCourseId(courseId)).thenReturn(List.of(1, 2, 3));
        // User has duplicate progress for lesson 1, progress for lesson 2, and orphaned progress for deleted lesson 999
        when(lessonProgressRepository.findByUserIdAndCourseId(userId, courseId))
                .thenReturn(buildLessonProgresses(userId, courseId, List.of(1, 1, 2, 999)));
        when(quizRepository.findQuizIdsByCourseId(courseId)).thenReturn(List.of(201));
        when(quizAttemptRepository.findPassedQuizIdsByUserAndQuizIds(userId, List.of(201)))
                .thenReturn(List.of(201, 201)); // even if duplicate IDs returned

        LearningProgressService.ProgressSnapshot snap = learningProgressService.getProgress(userId, courseId);
        assertEquals(3L, snap.totalLessons());
        assertEquals(2, snap.completedLessonIds().size());
        assertEquals(1L, snap.totalQuizzes());
        assertEquals(1L, snap.completedQuizzes());
        assertEquals(4L, snap.totalItems());
        assertEquals(3L, snap.completedItems());
        assertEquals(75, snap.progressPercentage());
    }
}

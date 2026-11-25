package com.edumy.learning.enrollment.service;

import com.edumy.authoring.model.CourseSection;
import com.edumy.authoring.model.Lesson;
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
import com.edumy.shared.exception.BusinessException;
import com.edumy.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class LearningProgressService {

    private final LessonRepository lessonRepository;
    private final CourseSectionRepository courseSectionRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final CertificateRepository certificateRepository;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.edumy.authoring.repository.CourseRepository courseRepository;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.edumy.identity.user.repository.UserRepository userRepository;

    public LearningProgressService(LessonRepository lessonRepository,
                                   CourseSectionRepository courseSectionRepository,
                                   LessonProgressRepository lessonProgressRepository,
                                   EnrollmentRepository enrollmentRepository,
                                   QuizRepository quizRepository,
                                   QuizAttemptRepository quizAttemptRepository,
                                   CertificateRepository certificateRepository) {
        this.lessonRepository = lessonRepository;
        this.courseSectionRepository = courseSectionRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.quizRepository = quizRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.certificateRepository = certificateRepository;
    }

    public record MarkLessonResult(
            Integer lessonId,
            boolean completed,
            int progressPercent,
            String certificateCode,
            boolean certificateJustIssued
    ) {}

    public record ProgressSnapshot(
            List<Integer> completedLessonIds,
            List<Integer> completedQuizIds,
            long totalLessons,
            long totalQuizzes,
            long completedQuizzes,
            long totalItems,
            long completedItems,
            int progressPercentage,
            Integer certificateId,
            String certificateUrl,
            Integer lastAccessedLessonId,
            LocalDateTime lastAccessedAt
    ) {}

    @Transactional
    public MarkLessonResult markLesson(Integer userId, Integer lessonId, boolean completed) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy bài học."));
        CourseSection section = lesson.getSectionId() != null
                ? courseSectionRepository.findById(lesson.getSectionId()).orElse(null)
                : null;
        if (section == null || section.getCourseId() == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy khóa học cho bài học này.");
        }
        Integer courseId = section.getCourseId();

        List<LessonProgress> existing = lessonProgressRepository.findAllByUserIdAndLessonId(userId, lessonId);
        if (completed) {
            if (existing.isEmpty()) {
                LessonProgress lp = new LessonProgress();
                lp.setUserId(userId);
                lp.setCourseId(courseId);
                lp.setLessonId(lessonId);
                lp.setCompletedAt(LocalDateTime.now());
                lessonProgressRepository.save(lp);
            }
        } else if (!existing.isEmpty()) {
            lessonProgressRepository.deleteAll(existing);
        }

        RecalcResult recalc = recalculateAndIssueCertificate(userId, courseId);
        return new MarkLessonResult(
                lessonId,
                completed,
                recalc.progressPercent(),
                recalc.certificateCode(),
                recalc.certificateJustIssued()
        );
    }

    @Transactional
    public void recordVisit(Integer userId, Integer lessonId) {
        if (userId == null || lessonId == null) return;
        Lesson lesson = lessonRepository.findById(lessonId).orElse(null);
        if (lesson == null || lesson.getSectionId() == null) return;
        CourseSection section = courseSectionRepository.findById(lesson.getSectionId()).orElse(null);
        if (section == null || section.getCourseId() == null) return;

        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, section.getCourseId());
        if (enrollment != null) {
            enrollment.setLastAccessedLessonId(lessonId);
            enrollment.setLastAccessedAt(LocalDateTime.now());
            enrollmentRepository.save(enrollment);
        }
    }

    private record ComputedProgress(
            List<Integer> completedLessonIds,
            List<Integer> completedQuizIds,
            long totalLessons,
            long totalQuizzes,
            long completedQuizzes,
            long totalItems,
            long completedItems,
            int progressPercentage
    ) {}

    /**
     * Single source of truth for course progress calculation (UC-S11 / BR-32):
     * counts distinct completed lessons belonging to the course + distinct passed quizzes belonging to the course,
     * and floors the percentage to an integer in [0, 100].
     */
    private ComputedProgress computeProgress(Integer userId, Integer courseId) {
        Set<Integer> validLessonIds = new HashSet<>(lessonRepository.findLessonIdsByCourseId(courseId));
        long totalLessons = validLessonIds.size();

        List<Integer> completedLessonIds = lessonProgressRepository.findByUserIdAndCourseId(userId, courseId).stream()
                .map(LessonProgress::getLessonId)
                .filter(validLessonIds::contains)
                .distinct()
                .toList();
        long completedLessons = completedLessonIds.size();

        List<Integer> validQuizIds = quizRepository.findQuizIdsByCourseId(courseId).stream()
                .distinct()
                .toList();
        long totalQuizzes = validQuizIds.size();

        List<Integer> completedQuizIds = !validQuizIds.isEmpty()
                ? quizAttemptRepository.findPassedQuizIdsByUserAndQuizIds(userId, validQuizIds).stream().distinct().toList()
                : Collections.emptyList();
        long completedQuizzes = completedQuizIds.size();

        long totalItems = totalLessons + totalQuizzes;
        long completedItems = completedLessons + completedQuizzes;
        int progressPercentage = totalItems > 0
                ? (int) Math.min(100, Math.floor((double) completedItems / totalItems * 100))
                : 0;

        return new ComputedProgress(
                completedLessonIds,
                completedQuizIds,
                totalLessons,
                totalQuizzes,
                completedQuizzes,
                totalItems,
                completedItems,
                progressPercentage
        );
    }

    @Transactional(readOnly = true)
    public ProgressSnapshot getProgress(Integer userId, Integer courseId) {
        ComputedProgress computed = computeProgress(userId, courseId);

        Certificate cert = certificateRepository.findByUserIdAndCourseId(userId, courseId);
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId);

        return new ProgressSnapshot(
                computed.completedLessonIds(),
                computed.completedQuizIds(),
                computed.totalLessons(),
                computed.totalQuizzes(),
                computed.completedQuizzes(),
                computed.totalItems(),
                computed.completedItems(),
                computed.progressPercentage(),
                cert != null ? cert.getId() : null,
                cert != null ? cert.getCertificateUrl() : null,
                enrollment != null ? enrollment.getLastAccessedLessonId() : null,
                enrollment != null ? enrollment.getLastAccessedAt() : null
        );
    }

    public record RecalcResult(int progressPercent, String certificateCode, boolean certificateJustIssued) {}

    @Transactional
    public RecalcResult recalculateAndIssueCertificate(Integer userId, Integer courseId) {
        ComputedProgress computed = computeProgress(userId, courseId);
        long totalItems = computed.totalItems();
        long completedItems = computed.completedItems();
        int progressPercent = computed.progressPercentage();

        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId);
        if (enrollment != null) {
            enrollment.setProgressPercentage(progressPercent);
            if (progressPercent >= 100) {
                enrollment.setStatus("COMPLETED");
                if (enrollment.getCompletedAt() == null) {
                    enrollment.setCompletedAt(LocalDateTime.now());
                }
            } else {
                enrollment.setStatus("ACTIVE");
            }
            enrollmentRepository.save(enrollment);
        }

        Certificate cert = certificateRepository.findByUserIdAndCourseId(userId, courseId);
        boolean justIssued = false;
        if (totalItems > 0 && completedItems >= totalItems && cert == null) {
            String courseTitleSnap = null;
            String instructorNameSnap = null;
            String studentNameSnap = null;
            if (courseRepository != null) {
                var c = courseRepository.findById(courseId).orElse(null);
                if (c != null) {
                    courseTitleSnap = c.getTitle();
                    if (c.getInstructor() != null) {
                        instructorNameSnap = c.getInstructor().getFullName();
                    } else if (c.getInstructorId() != null && userRepository != null) {
                        instructorNameSnap = userRepository.findById(c.getInstructorId())
                                .map(com.edumy.identity.user.model.User::getFullName)
                                .orElse(null);
                    }
                }
            }
            if (userRepository != null) {
                studentNameSnap = userRepository.findById(userId)
                        .map(com.edumy.identity.user.model.User::getFullName)
                        .orElse(null);
            }
            cert = Certificate.builder()
                    .userId(userId)
                    .courseId(courseId)
                    .courseTitleSnapshot(courseTitleSnap)
                    .studentNameSnapshot(studentNameSnap)
                    .instructorNameSnapshot(instructorNameSnap)
                    .issuedAt(LocalDateTime.now())
                    .certificateUrl("EDUMY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase())
                    .build();
            cert = certificateRepository.save(cert);
            justIssued = true;
        }

        return new RecalcResult(
                progressPercent,
                cert != null ? cert.getCertificateUrl() : null,
                justIssued
        );
    }
}

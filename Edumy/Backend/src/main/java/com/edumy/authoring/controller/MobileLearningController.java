package com.edumy.authoring.controller;

import com.edumy.authoring.model.Course;
import com.edumy.authoring.model.CourseSection;
import com.edumy.authoring.model.Lesson;
import com.edumy.authoring.repository.CourseCoInstructorRepository;
import com.edumy.authoring.repository.CourseRepository;
import com.edumy.authoring.repository.CourseSectionRepository;
import com.edumy.authoring.repository.LessonRepository;
import com.edumy.learning.enrollment.model.Enrollment;
import com.edumy.learning.enrollment.repository.EnrollmentRepository;
import com.edumy.learning.enrollment.service.LearningProgressService;
import com.edumy.learning.quiz.model.Quiz;
import com.edumy.learning.quiz.repository.QuizAttemptRepository;
import com.edumy.learning.quiz.repository.QuizRepository;
import com.edumy.shared.exception.BusinessException;
import com.edumy.shared.exception.ErrorCode;
import com.edumy.shared.response.PageResponse;
import com.edumy.shared.security.SecurityUtils;
import com.edumy.shared.time.TimeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("isAuthenticated()")
public class MobileLearningController {

    private final CourseRepository courseRepository;
    private final CourseSectionRepository courseSectionRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LearningProgressService learningProgressService;
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final CourseCoInstructorRepository courseCoInstructorRepository;

    @Value("${edumy.time-zone:Asia/Ho_Chi_Minh}")
    private String timeZone;

    public MobileLearningController(CourseRepository courseRepository,
                                    CourseSectionRepository courseSectionRepository,
                                    LessonRepository lessonRepository,
                                    EnrollmentRepository enrollmentRepository,
                                    LearningProgressService learningProgressService,
                                    QuizRepository quizRepository,
                                    QuizAttemptRepository quizAttemptRepository,
                                    CourseCoInstructorRepository courseCoInstructorRepository) {
        this.courseRepository = courseRepository;
        this.courseSectionRepository = courseSectionRepository;
        this.lessonRepository = lessonRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.learningProgressService = learningProgressService;
        this.quizRepository = quizRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.courseCoInstructorRepository = courseCoInstructorRepository;
    }

    public record EnrollmentCreatedDto(Integer enrollmentId, Integer courseId) {}

    public record CourseMiniDto(
            Integer id,
            String slug,
            String title,
            String thumbnailUrl,
            String instructorName
    ) {}

    public record MobileEnrollmentDto(
            Integer id,
            Instant enrolledAt,
            int progressPercent,
            long completedItems,
            long totalItems,
            Integer lastAccessedLessonId,
            Instant lastAccessedAt,
            String certificateCode,
            CourseMiniDto course
    ) {}

    public record ProgressSummaryDto(int percent, long completedItems, long totalItems) {}

    public record LearnLessonItemDto(
            Integer id,
            String title,
            String contentType,
            int durationSeconds,
            boolean completed
    ) {}

    public record LearnQuizItemDto(Integer id, String title, boolean passed) {}

    public record LearnChapterDto(
            Integer id,
            String title,
            List<LearnLessonItemDto> lessons,
            List<LearnQuizItemDto> quizzes
    ) {}

    public record LearnRoomDto(
            Integer courseId,
            String title,
            ProgressSummaryDto progress,
            Integer lastAccessedLessonId,
            String certificateCode,
            List<LearnChapterDto> chapters
    ) {}

    public record LearnLessonDetailDto(
            Integer id,
            Integer courseId,
            String title,
            String contentType,
            String videoUrl,
            String articleContent,
            int durationSeconds,
            Integer prevLessonId,
            Integer nextLessonId,
            boolean completed
    ) {}

    public record LessonCompletionRequest(Boolean completed) {}

    public record LessonCompletionResponseDto(
            Integer lessonId,
            boolean completed,
            int progressPercent,
            String certificateCode,
            boolean certificateJustIssued
    ) {}

    @PostMapping("/courses/{courseId}/enrollments")
    public ResponseEntity<EnrollmentCreatedDto> enrollFreeCourse(@PathVariable Integer courseId) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "COURSE_NOT_FOUND", "Không tìm thấy khóa học."));
        if (!"Published".equalsIgnoreCase(course.getStatus())) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "COURSE_NOT_FOUND", "Không tìm thấy khóa học.");
        }
        if (Objects.equals(course.getInstructorId(), userId)) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "OWN_COURSE", "Giảng viên không thể ghi danh khóa học của chính mình.");
        }
        if (course.getPrice() != null && course.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "COURSE_NOT_FREE", "Khóa học này là khóa trả phí.");
        }

        Enrollment existing = enrollmentRepository.findByUserIdAndCourseId(userId, courseId);
        if (existing != null) {
            return ResponseEntity.ok(new EnrollmentCreatedDto(existing.getEnrollmentId(), courseId));
        }

        Enrollment enrollment = Enrollment.builder()
                .userId(userId)
                .courseId(courseId)
                .enrolledAt(LocalDateTime.now())
                .progressPercentage(0)
                .status("ACTIVE")
                .build();
        enrollment = enrollmentRepository.save(enrollment);
        return ResponseEntity.ok(new EnrollmentCreatedDto(enrollment.getEnrollmentId(), courseId));
    }

    @GetMapping("/me/enrollments")
    public ResponseEntity<PageResponse<MobileEnrollmentDto>> getMyEnrollments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);

        int safePage = Math.max(0, page);
        int safeSize = Math.min(100, Math.max(1, size));
        Page<Enrollment> enrollmentPage = enrollmentRepository.findByUserIdOrderByEnrolledAtDesc(
                userId, PageRequest.of(safePage, safeSize));

        Page<MobileEnrollmentDto> mapped = enrollmentPage.map(e -> {
            Course c = e.getCourse() != null ? e.getCourse() : courseRepository.findById(e.getCourseId()).orElse(null);
            LearningProgressService.ProgressSnapshot snap = c != null
                    ? learningProgressService.getProgress(userId, c.getCourseId())
                    : null;

            CourseMiniDto courseMini = null;
            if (c != null) {
                courseMini = new CourseMiniDto(
                        c.getCourseId(),
                        resolveCourseSlug(c),
                        c.getTitle(),
                        c.getThumbnailUrl(),
                        c.getInstructor() != null ? c.getInstructor().getFullName() : "Giảng viên Edumy"
                );
            }

            return new MobileEnrollmentDto(
                    e.getEnrollmentId(),
                    TimeUtils.toInstant(e.getEnrolledAt(), timeZone),
                    snap != null ? snap.progressPercentage() : (e.getProgressPercentage() != null ? e.getProgressPercentage() : 0),
                    snap != null ? snap.completedItems() : 0L,
                    snap != null ? snap.totalItems() : 0L,
                    e.getLastAccessedLessonId(),
                    TimeUtils.toInstant(e.getLastAccessedAt(), timeZone),
                    snap != null ? snap.certificateUrl() : null,
                    courseMini
            );
        });

        return ResponseEntity.ok(PageResponse.from(mapped));
    }

    @GetMapping("/learn/courses/{courseId}")
    public ResponseEntity<LearnRoomDto> getLearnCourseRoom(@PathVariable Integer courseId) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "COURSE_NOT_FOUND", "Không tìm thấy khóa học."));
        requireCourseLearningAccess(course, userId);

        LearningProgressService.ProgressSnapshot snap = learningProgressService.getProgress(userId, courseId);
        Set<Integer> completedLessonIds = new HashSet<>(snap.completedLessonIds());
        Set<Integer> passedQuizIds = new HashSet<>(snap.completedQuizIds());

        List<CourseSection> sections = courseSectionRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        List<Integer> sectionIds = sections.stream().map(CourseSection::getSectionId).collect(Collectors.toList());
        Map<Integer, List<Lesson>> lessonsBySection = sectionIds.isEmpty()
                ? Collections.emptyMap()
                : lessonRepository.findBySectionIdInOrderByOrderIndexAsc(sectionIds).stream()
                        .collect(Collectors.groupingBy(Lesson::getSectionId, LinkedHashMap::new, Collectors.toList()));
        Map<Integer, List<Quiz>> quizzesBySection = sectionIds.isEmpty()
                ? Collections.emptyMap()
                : quizRepository.findByCourseSectionIdIn(sectionIds).stream()
                        .collect(Collectors.groupingBy(Quiz::getCourseSectionId, LinkedHashMap::new, Collectors.toList()));

        List<LearnChapterDto> chapters = new ArrayList<>();
        for (CourseSection sec : sections) {
            List<Lesson> secLessons = lessonsBySection.getOrDefault(sec.getSectionId(), Collections.emptyList());
            List<LearnLessonItemDto> lessonDtos = secLessons.stream()
                    .map(les -> new LearnLessonItemDto(
                            les.getLessonId(),
                            les.getTitle(),
                            (les.getVideoUrl() != null && !les.getVideoUrl().isBlank()) ? "video" : "article",
                            les.getDuration() != null ? les.getDuration() * 60 : 300,
                            completedLessonIds.contains(les.getLessonId())
                    ))
                    .toList();

            List<Quiz> secQuizzes = quizzesBySection.getOrDefault(sec.getSectionId(), Collections.emptyList());
            List<LearnQuizItemDto> quizDtos = secQuizzes.stream()
                    .map(q -> new LearnQuizItemDto(
                            q.getQuizId(),
                            q.getTitle(),
                            passedQuizIds.contains(q.getQuizId())
                    ))
                    .toList();

            chapters.add(new LearnChapterDto(sec.getSectionId(), sec.getTitle(), lessonDtos, quizDtos));
        }

        return ResponseEntity.ok(new LearnRoomDto(
                courseId,
                course.getTitle(),
                new ProgressSummaryDto(snap.progressPercentage(), snap.completedItems(), snap.totalItems()),
                snap.lastAccessedLessonId(),
                snap.certificateUrl(),
                chapters
        ));
    }

    @GetMapping("/learn/lessons/{lessonId}")
    public ResponseEntity<LearnLessonDetailDto> getLearnLessonDetail(@PathVariable Integer lessonId) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy bài học."));
        CourseSection section = lesson.getSectionId() != null ? courseSectionRepository.findById(lesson.getSectionId()).orElse(null) : null;
        Integer courseId = section != null ? section.getCourseId() : null;
        Course course = courseId != null ? courseRepository.findById(courseId).orElse(null) : null;
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy khóa học cho bài học này.");
        }
        requireCourseLearningAccess(course, userId);

        LearningProgressService.ProgressSnapshot snap = learningProgressService.getProgress(userId, courseId);
        boolean completed = snap.completedLessonIds().contains(lessonId);
        List<Integer> orderedLessonIds = getOrderedLessonIds(courseId);
        int idx = orderedLessonIds.indexOf(lessonId);
        Integer prevId = idx > 0 ? orderedLessonIds.get(idx - 1) : null;
        Integer nextId = (idx >= 0 && idx < orderedLessonIds.size() - 1) ? orderedLessonIds.get(idx + 1) : null;

        return ResponseEntity.ok(new LearnLessonDetailDto(
                lesson.getLessonId(),
                courseId,
                lesson.getTitle(),
                (lesson.getVideoUrl() != null && !lesson.getVideoUrl().isBlank()) ? "video" : "article",
                lesson.getVideoUrl(),
                null,
                lesson.getDuration() != null ? lesson.getDuration() * 60 : 300,
                prevId,
                nextId,
                completed
        ));
    }

    @PostMapping("/learn/lessons/{lessonId}/visit")
    public ResponseEntity<Void> recordLessonVisit(@PathVariable Integer lessonId) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy bài học."));
        CourseSection section = lesson.getSectionId() != null ? courseSectionRepository.findById(lesson.getSectionId()).orElse(null) : null;
        Integer courseId = section != null ? section.getCourseId() : null;
        Course course = courseId != null ? courseRepository.findById(courseId).orElse(null) : null;
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy khóa học cho bài học này.");
        }
        requireCourseLearningAccess(course, userId);
        learningProgressService.recordVisit(userId, lessonId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/learn/lessons/{lessonId}/completion")
    public ResponseEntity<LessonCompletionResponseDto> setLessonCompletion(
            @PathVariable Integer lessonId,
            @RequestBody(required = false) LessonCompletionRequest body) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);
        boolean completed = body == null || body.completed() == null || body.completed();

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy bài học."));
        CourseSection section = lesson.getSectionId() != null ? courseSectionRepository.findById(lesson.getSectionId()).orElse(null) : null;
        Integer courseId = section != null ? section.getCourseId() : null;
        Course course = courseId != null ? courseRepository.findById(courseId).orElse(null) : null;
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy khóa học cho bài học này.");
        }
        requireCourseLearningAccess(course, userId);

        LearningProgressService.MarkLessonResult res = learningProgressService.markLesson(userId, lessonId, completed);
        return ResponseEntity.ok(new LessonCompletionResponseDto(
                res.lessonId(),
                res.completed(),
                res.progressPercent(),
                res.certificateCode(),
                res.certificateJustIssued()
        ));
    }

    private List<Integer> getOrderedLessonIds(Integer courseId) {
        List<CourseSection> sections = courseSectionRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        List<Integer> sectionIds = sections.stream().map(CourseSection::getSectionId).collect(Collectors.toList());
        if (sectionIds.isEmpty()) return Collections.emptyList();
        Map<Integer, List<Lesson>> lessonsBySection = lessonRepository.findBySectionIdInOrderByOrderIndexAsc(sectionIds).stream()
                .collect(Collectors.groupingBy(Lesson::getSectionId, LinkedHashMap::new, Collectors.toList()));
        List<Integer> ordered = new ArrayList<>();
        for (CourseSection sec : sections) {
            for (Lesson les : lessonsBySection.getOrDefault(sec.getSectionId(), Collections.emptyList())) {
                ordered.add(les.getLessonId());
            }
        }
        return ordered;
    }

    private boolean hasCourseLearningAccess(Course course, Integer userId) {
        if (course == null || userId == null) return false;
        if (SecurityUtils.hasRole("Admin")) return true;
        if (Objects.equals(course.getInstructorId(), userId)) return true;
        boolean isCo = courseCoInstructorRepository.findFirstByCourseIdAndInstructorId(course.getCourseId(), userId)
                .map(ci -> "ACCEPTED".equalsIgnoreCase(ci.getStatus()))
                .orElse(false);
        if (isCo) return true;
        return enrollmentRepository.isUserEnrolledActive(userId, course.getCourseId());
    }

    private void requireCourseLearningAccess(Course course, Integer userId) {
        if (!hasCourseLearningAccess(course, userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "NOT_ENROLLED", "Bạn chưa ghi danh khóa học này.");
        }
    }

    private String resolveCourseSlug(Course c) {
        if (c.getSlug() != null && !c.getSlug().trim().isEmpty()) {
            return c.getSlug().trim();
        }
        return "course-" + c.getCourseId();
    }
}

package com.edumy.authoring.controller;

import com.edumy.authoring.model.Course;
import com.edumy.authoring.model.CourseSection;
import com.edumy.authoring.model.Lesson;
import com.edumy.authoring.repository.CourseRepository;
import com.edumy.authoring.repository.CourseSectionRepository;
import com.edumy.authoring.repository.LessonRepository;
import com.edumy.learning.enrollment.model.Certificate;
import com.edumy.learning.enrollment.model.Enrollment;
import com.edumy.learning.enrollment.model.LessonProgress;
import com.edumy.learning.enrollment.repository.CertificateRepository;
import com.edumy.learning.enrollment.repository.EnrollmentRepository;
import com.edumy.learning.enrollment.repository.LessonProgressRepository;
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
    private final LessonProgressRepository lessonProgressRepository;
    private final CertificateRepository certificateRepository;

    @Value("${edumy.time-zone:Asia/Ho_Chi_Minh}")
    private String timeZone;

    public MobileLearningController(CourseRepository courseRepository,
                                    CourseSectionRepository courseSectionRepository,
                                    LessonRepository lessonRepository,
                                    EnrollmentRepository enrollmentRepository,
                                    LessonProgressRepository lessonProgressRepository,
                                    CertificateRepository certificateRepository) {
        this.courseRepository = courseRepository;
        this.courseSectionRepository = courseSectionRepository;
        this.lessonRepository = lessonRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.certificateRepository = certificateRepository;
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
            long totalLessons = c != null ? lessonRepository.countLessonsByCourseId(c.getCourseId()) : 0;
            long completedLessons = c != null ? lessonProgressRepository.countCompletedLessonsByUserAndCourse(userId, c.getCourseId()) : 0;
            int pct = e.getProgressPercentage() != null ? e.getProgressPercentage() : 0;

            String certCode = null;
            if (c != null) {
                Certificate cert = certificateRepository.findByUserIdAndCourseId(userId, c.getCourseId());
                if (cert != null) certCode = cert.getCertificateUrl();
            }

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
                    pct,
                    completedLessons,
                    totalLessons,
                    null,
                    null,
                    certCode,
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

        Set<Integer> completedIds = lessonProgressRepository.findByUserIdAndCourseId(userId, courseId).stream()
                .map(LessonProgress::getLessonId)
                .collect(Collectors.toSet());

        List<CourseSection> sections = courseSectionRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        List<Integer> sectionIds = sections.stream().map(CourseSection::getSectionId).collect(Collectors.toList());
        Map<Integer, List<Lesson>> lessonsBySection = sectionIds.isEmpty()
                ? Collections.emptyMap()
                : lessonRepository.findBySectionIdInOrderByOrderIndexAsc(sectionIds).stream()
                        .collect(Collectors.groupingBy(Lesson::getSectionId, LinkedHashMap::new, Collectors.toList()));

        int totalLessons = 0;
        List<LearnChapterDto> chapters = new ArrayList<>();
        for (CourseSection sec : sections) {
            List<Lesson> secLessons = lessonsBySection.getOrDefault(sec.getSectionId(), Collections.emptyList());
            List<LearnLessonItemDto> lessonDtos = new ArrayList<>();
            for (Lesson les : secLessons) {
                totalLessons++;
                lessonDtos.add(new LearnLessonItemDto(
                        les.getLessonId(),
                        les.getTitle(),
                        (les.getVideoUrl() != null && !les.getVideoUrl().isBlank()) ? "video" : "article",
                        les.getDuration() != null ? les.getDuration() * 60 : 300,
                        completedIds.contains(les.getLessonId())
                ));
            }
            chapters.add(new LearnChapterDto(sec.getSectionId(), sec.getTitle(), lessonDtos, Collections.emptyList()));
        }

        int pct = totalLessons > 0 ? Math.min(100, (completedIds.size() * 100) / totalLessons) : 0;
        Certificate cert = certificateRepository.findByUserIdAndCourseId(userId, courseId);

        return ResponseEntity.ok(new LearnRoomDto(
                courseId,
                course.getTitle(),
                new ProgressSummaryDto(pct, completedIds.size(), totalLessons),
                null,
                cert != null ? cert.getCertificateUrl() : null,
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

        boolean completed = lessonProgressRepository.existsByUserIdAndCourseIdAndLessonId(userId, courseId, lessonId);
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

        long totalLessons = lessonRepository.countLessonsByCourseId(courseId);
        long completedLessons = lessonProgressRepository.countCompletedLessonsByUserAndCourse(userId, courseId);
        int pct = totalLessons > 0 ? (int) Math.min(100, (completedLessons * 100) / totalLessons) : 0;

        Enrollment enr = enrollmentRepository.findByUserIdAndCourseId(userId, courseId);
        if (enr != null) {
            enr.setProgressPercentage(pct);
            enrollmentRepository.save(enr);
        }

        Certificate cert = certificateRepository.findByUserIdAndCourseId(userId, courseId);
        return ResponseEntity.ok(new LessonCompletionResponseDto(
                lessonId,
                completed,
                pct,
                cert != null ? cert.getCertificateUrl() : null,
                false
        ));
    }

    private List<Integer> getOrderedLessonIds(Integer courseId) {
        List<CourseSection> sections = courseSectionRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        List<Integer> sectionIds = sections.stream().map(CourseSection::getSectionId).collect(Collectors.toList());
        if (sectionIds.isEmpty()) return Collections.emptyList();
        return lessonRepository.findBySectionIdInOrderByOrderIndexAsc(sectionIds).stream()
                .map(Lesson::getLessonId)
                .collect(Collectors.toList());
    }

    private boolean hasCourseLearningAccess(Course course, Integer userId) {
        if (course == null || userId == null) return false;
        if (SecurityUtils.hasRole("Admin")) return true;
        if (Objects.equals(course.getInstructorId(), userId)) return true;
        List<Map<String, Object>> coList = SpecUseCaseController.COURSE_CO_INSTRUCTORS.getOrDefault(course.getCourseId(), Collections.emptyList());
        for (Map<String, Object> m : coList) {
            if ("ACCEPTED".equalsIgnoreCase(String.valueOf(m.get("status")))
                    && Objects.equals(m.get("instructorId"), userId)) {
                return true;
            }
        }
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

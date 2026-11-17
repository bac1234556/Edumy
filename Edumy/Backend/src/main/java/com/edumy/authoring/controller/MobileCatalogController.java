package com.edumy.authoring.controller;

import com.edumy.authoring.model.Course;
import com.edumy.authoring.model.CourseSection;
import com.edumy.authoring.model.Lesson;
import com.edumy.authoring.repository.CategoryRepository;
import com.edumy.authoring.repository.CourseRepository;
import com.edumy.authoring.repository.CourseSectionRepository;
import com.edumy.authoring.repository.LessonRepository;
import com.edumy.learning.enrollment.repository.EnrollmentRepository;
import com.edumy.learning.quiz.repository.QuizRepository;
import com.edumy.learning.review.model.Review;
import com.edumy.learning.review.repository.ReviewRepository;
import com.edumy.shared.exception.BusinessException;
import com.edumy.shared.exception.ErrorCode;
import com.edumy.shared.response.PageResponse;
import com.edumy.shared.security.SecurityUtils;
import com.edumy.shared.time.TimeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/catalog")
public class MobileCatalogController {

    private final CategoryRepository categoryRepository;
    private final CourseRepository courseRepository;
    private final CourseSectionRepository courseSectionRepository;
    private final LessonRepository lessonRepository;
    private final ReviewRepository reviewRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final QuizRepository quizRepository;

    @Value("${edumy.time-zone:Asia/Ho_Chi_Minh}")
    private String timeZone;

    public MobileCatalogController(CategoryRepository categoryRepository,
                                   CourseRepository courseRepository,
                                   CourseSectionRepository courseSectionRepository,
                                   LessonRepository lessonRepository,
                                   ReviewRepository reviewRepository,
                                   EnrollmentRepository enrollmentRepository,
                                   QuizRepository quizRepository) {
        this.categoryRepository = categoryRepository;
        this.courseRepository = courseRepository;
        this.courseSectionRepository = courseSectionRepository;
        this.lessonRepository = lessonRepository;
        this.reviewRepository = reviewRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.quizRepository = quizRepository;
    }

    public record CategoryDto(Integer id, String name, String slug) {}

    public record CourseCardDto(
            Integer id,
            String slug,
            String title,
            String thumbnailUrl,
            long price,
            boolean isFree,
            Integer categoryId,
            String categoryName,
            String instructorName,
            Double averageRating,
            long ratingCount,
            Instant publishedAt
    ) {}

    public record CatalogCoursePageDto(
            List<CourseCardDto> content,
            PageResponse.PageMeta page,
            List<CourseCardDto> suggestions
    ) {}

    public record InstructorRefDto(Integer id, String fullName) {}

    public record LessonOutlineItemDto(
            Integer id,
            String title,
            String contentType,
            int durationSeconds,
            boolean isPreview
    ) {}

    public record ChapterOutlineDto(
            Integer id,
            String title,
            List<LessonOutlineItemDto> lessons
    ) {}

    public record ViewerStateDto(boolean owned, boolean isInstructor) {}

    public record CourseDetailDto(
            Integer id,
            String slug,
            String title,
            String description,
            String thumbnailUrl,
            long price,
            boolean isFree,
            Integer categoryId,
            String categoryName,
            String instructorName,
            InstructorRefDto mainInstructor,
            List<InstructorRefDto> coInstructors,
            Double averageRating,
            long ratingCount,
            int totalLessons,
            long totalQuizzes,
            int totalDurationSeconds,
            Instant publishedAt,
            List<ChapterOutlineDto> chapters,
            ViewerStateDto viewer
    ) {}

    public record CatalogReviewDto(
            Integer id,
            Integer rating,
            String comment,
            String reviewerName,
            Instant createdAt
    ) {}

    public record LessonContentDto(
            Integer id,
            Integer courseId,
            String title,
            String contentType,
            String videoUrl,
            String articleContent,
            int durationSeconds,
            Integer prevLessonId,
            Integer nextLessonId
    ) {}

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getCatalogCategories() {
        List<CategoryDto> result = categoryRepository.findAll().stream()
                .map(cat -> new CategoryDto(cat.getCategoryId(), cat.getName(), "cat-" + cat.getCategoryId()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/courses")
    public ResponseEntity<CatalogCoursePageDto> getCatalogCourses(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String price,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        int safePage = Math.max(0, page);
        int safeSize = Math.min(100, Math.max(1, size));
        Sort sortObj = resolveSort(sort);
        Pageable pageable = PageRequest.of(safePage, safeSize, sortObj);

        String cleanQ = (q != null && !q.trim().isEmpty()) ? q.trim() : null;
        String cleanPrice = (price != null && ("free".equalsIgnoreCase(price) || "paid".equalsIgnoreCase(price)))
                ? price.toLowerCase()
                : null;

        Page<Course> coursePage = courseRepository.findPublishedCatalog(cleanQ, categoryId, cleanPrice, pageable);
        List<Course> courses = coursePage.getContent();
        Map<Integer, Long> ratingCounts = loadRatingCounts(courses);

        List<CourseCardDto> content = courses.stream()
                .map(c -> mapCourseCard(c, ratingCounts.getOrDefault(c.getCourseId(), 0L)))
                .collect(Collectors.toList());

        List<CourseCardDto> suggestions = Collections.emptyList();
        if (content.isEmpty()) {
            List<Course> top4 = courseRepository.findTop4ByStatusIgnoreCaseOrderByCourseIdDesc("Published");
            Map<Integer, Long> sugCounts = loadRatingCounts(top4);
            suggestions = top4.stream()
                    .map(c -> mapCourseCard(c, sugCounts.getOrDefault(c.getCourseId(), 0L)))
                    .collect(Collectors.toList());
        }

        PageResponse.PageMeta meta = new PageResponse.PageMeta(
                coursePage.getNumber(),
                coursePage.getSize(),
                coursePage.getTotalElements(),
                coursePage.getTotalPages()
        );
        return ResponseEntity.ok(new CatalogCoursePageDto(content, meta, suggestions));
    }

    @GetMapping("/courses/{slugOrId}")
    public ResponseEntity<CourseDetailDto> getCatalogCourseDetail(@PathVariable String slugOrId) {
        Course course = resolveCourseBySlugOrId(slugOrId);
        if (course == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "COURSE_NOT_FOUND", "Không tìm thấy khóa học.");
        }

        Integer viewerId = SecurityUtils.getCurrentUserId();
        boolean isInst = viewerId != null && isCourseInstructorOrAdmin(course, viewerId);
        if (!"Published".equalsIgnoreCase(course.getStatus()) && !isInst) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "COURSE_NOT_FOUND", "Không tìm thấy khóa học.");
        }

        Integer courseId = course.getCourseId();
        List<CourseSection> sections = courseSectionRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        List<Integer> sectionIds = sections.stream().map(CourseSection::getSectionId).collect(Collectors.toList());
        Map<Integer, List<Lesson>> lessonsBySection = sectionIds.isEmpty()
                ? Collections.emptyMap()
                : lessonRepository.findBySectionIdInOrderByOrderIndexAsc(sectionIds).stream()
                        .collect(Collectors.groupingBy(Lesson::getSectionId, LinkedHashMap::new, Collectors.toList()));

        int totalLessons = 0;
        int totalDurationSeconds = 0;
        List<ChapterOutlineDto> chapters = new ArrayList<>();
        for (CourseSection sec : sections) {
            List<Lesson> secLessons = lessonsBySection.getOrDefault(sec.getSectionId(), Collections.emptyList());
            List<LessonOutlineItemDto> lessonDtos = new ArrayList<>();
            for (Lesson les : secLessons) {
                totalLessons++;
                int durSec = les.getDuration() != null ? les.getDuration() * 60 : 300;
                totalDurationSeconds += durSec;
                String contentType = (les.getVideoUrl() != null && !les.getVideoUrl().isBlank()) ? "video" : "article";
                lessonDtos.add(new LessonOutlineItemDto(
                        les.getLessonId(),
                        les.getTitle(),
                        contentType,
                        durSec,
                        Boolean.TRUE.equals(les.getIsPreview())
                ));
            }
            chapters.add(new ChapterOutlineDto(sec.getSectionId(), sec.getTitle(), lessonDtos));
        }

        ViewerStateDto viewer = null;
        if (viewerId != null) {
            boolean owned = enrollmentRepository.isUserEnrolledActive(viewerId, courseId);
            viewer = new ViewerStateDto(owned, isInst);
        }

        String instName = (course.getInstructor() != null && course.getInstructor().getFullName() != null)
                ? course.getInstructor().getFullName()
                : "Giảng viên Edumy";
        String catName = (course.getCategory() != null && course.getCategory().getName() != null)
                ? course.getCategory().getName()
                : "Chuyên đề";

        long ratingCount = reviewRepository.countByCourseId(courseId);
        long totalQuizzes = quizRepository.countQuizzesByCourseId(courseId);
        boolean isFree = course.getPrice() == null || course.getPrice().compareTo(BigDecimal.ZERO) <= 0;
        Double avgRating = ratingCount > 0 ? course.getAverageRating() : null;

        return ResponseEntity.ok(new CourseDetailDto(
                courseId,
                resolveCourseSlug(course),
                course.getTitle(),
                course.getDescription(),
                course.getThumbnailUrl(),
                course.getPrice() != null ? course.getPrice().longValue() : 0L,
                isFree,
                course.getCategoryId(),
                catName,
                instName,
                new InstructorRefDto(course.getInstructorId() != null ? course.getInstructorId() : 1, instName),
                Collections.emptyList(),
                avgRating,
                ratingCount,
                totalLessons,
                totalQuizzes,
                totalDurationSeconds,
                TimeUtils.toInstant(course.getCreatedAt(), timeZone),
                chapters,
                viewer
        ));
    }

    @GetMapping("/courses/{slugOrId}/reviews")
    public ResponseEntity<PageResponse<CatalogReviewDto>> getCatalogCourseReviews(
            @PathVariable String slugOrId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Course course = resolveCourseBySlugOrId(slugOrId);
        if (course == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "COURSE_NOT_FOUND", "Không tìm thấy khóa học.");
        }
        int safePage = Math.max(0, page);
        int safeSize = Math.min(100, Math.max(1, size));
        Page<Review> reviewPage = reviewRepository.findByCourseIdOrderByCreatedAtDesc(
                course.getCourseId(), PageRequest.of(safePage, safeSize));
        Page<CatalogReviewDto> mapped = reviewPage.map(r -> new CatalogReviewDto(
                r.getReviewId(),
                r.getRating(),
                r.getComment(),
                r.getUser() != null ? r.getUser().getFullName() : "Học viên Edumy",
                TimeUtils.toInstant(r.getCreatedAt(), timeZone)
        ));
        return ResponseEntity.ok(PageResponse.from(mapped));
    }

    @GetMapping("/courses/{slugOrId}/lessons/{lessonId}/preview")
    public ResponseEntity<LessonContentDto> getCatalogLessonPreview(
            @PathVariable String slugOrId,
            @PathVariable Integer lessonId) {
        Course course = resolveCourseBySlugOrId(slugOrId);
        if (course == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "COURSE_NOT_FOUND", "Không tìm thấy khóa học.");
        }
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy bài học."));

        CourseSection section = lesson.getSectionId() != null
                ? courseSectionRepository.findById(lesson.getSectionId()).orElse(null)
                : null;
        if (section == null || !Objects.equals(section.getCourseId(), course.getCourseId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "LESSON_NOT_PREVIEWABLE", "Bài học không thuộc khóa học này hoặc không cho phép xem thử.");
        }

        Integer viewerId = SecurityUtils.getCurrentUserId();
        boolean canAccess = Boolean.TRUE.equals(lesson.getIsPreview())
                || (viewerId != null && (enrollmentRepository.isUserEnrolledActive(viewerId, course.getCourseId())
                || isCourseInstructorOrAdmin(course, viewerId)));
        if (!canAccess) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "LESSON_NOT_PREVIEWABLE", "Bài học này không cho phép xem thử miễn phí.");
        }

        String contentType = (lesson.getVideoUrl() != null && !lesson.getVideoUrl().isBlank()) ? "video" : "article";
        int durSec = lesson.getDuration() != null ? lesson.getDuration() * 60 : 300;
        return ResponseEntity.ok(new LessonContentDto(
                lesson.getLessonId(),
                course.getCourseId(),
                lesson.getTitle(),
                contentType,
                lesson.getVideoUrl(),
                null,
                durSec,
                null,
                null
        ));
    }

    private Sort resolveSort(String sort) {
        if ("rating".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Order.desc("averageRating"), Sort.Order.desc("courseId"));
        }
        if ("price_asc".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Order.asc("price"), Sort.Order.desc("courseId"));
        }
        if ("price_desc".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Order.desc("price"), Sort.Order.desc("courseId"));
        }
        return Sort.by(Sort.Order.desc("courseId"));
    }

    private Map<Integer, Long> loadRatingCounts(List<Course> courses) {
        if (courses == null || courses.isEmpty()) return Collections.emptyMap();
        List<Integer> ids = courses.stream().map(Course::getCourseId).filter(Objects::nonNull).collect(Collectors.toList());
        if (ids.isEmpty()) return Collections.emptyMap();
        Map<Integer, Long> map = new HashMap<>();
        for (Object[] row : reviewRepository.countByCourseIds(ids)) {
            if (row != null && row.length >= 2 && row[0] instanceof Integer cid && row[1] instanceof Number cnt) {
                map.put(cid, cnt.longValue());
            }
        }
        return map;
    }

    private CourseCardDto mapCourseCard(Course c, long ratingCount) {
        String instName = (c.getInstructor() != null && c.getInstructor().getFullName() != null && !c.getInstructor().getFullName().isBlank())
                ? c.getInstructor().getFullName()
                : "Giảng viên Edumy";
        String catName = (c.getCategory() != null && c.getCategory().getName() != null)
                ? c.getCategory().getName()
                : "Chuyên đề";
        boolean isFree = c.getPrice() == null || c.getPrice().compareTo(BigDecimal.ZERO) <= 0;
        Double avgRating = ratingCount > 0 ? c.getAverageRating() : null;

        return new CourseCardDto(
                c.getCourseId(),
                resolveCourseSlug(c),
                c.getTitle(),
                c.getThumbnailUrl(),
                c.getPrice() != null ? c.getPrice().longValue() : 0L,
                isFree,
                c.getCategoryId(),
                catName,
                instName,
                avgRating,
                ratingCount,
                TimeUtils.toInstant(c.getCreatedAt(), timeZone)
        );
    }

    private boolean isCourseInstructorOrAdmin(Course course, Integer userId) {
        if (SecurityUtils.hasRole("Admin")) return true;
        if (Objects.equals(course.getInstructorId(), userId)) return true;
        List<Map<String, Object>> coList = SpecUseCaseController.COURSE_CO_INSTRUCTORS.getOrDefault(course.getCourseId(), Collections.emptyList());
        for (Map<String, Object> m : coList) {
            if ("ACCEPTED".equalsIgnoreCase(String.valueOf(m.get("status")))
                    && Objects.equals(m.get("instructorId"), userId)) {
                return true;
            }
        }
        return false;
    }

    private Course resolveCourseBySlugOrId(String slugOrId) {
        if (slugOrId == null || slugOrId.isBlank()) return null;
        Optional<Course> bySlug = courseRepository.findFirstBySlugIgnoreCase(slugOrId.trim());
        if (bySlug.isPresent()) return bySlug.get();

        String rawId = slugOrId.startsWith("course-") ? slugOrId.substring("course-".length()) : slugOrId;
        try {
            int id = Integer.parseInt(rawId);
            return courseRepository.findById(id).orElse(null);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String resolveCourseSlug(Course c) {
        if (c.getSlug() != null && !c.getSlug().trim().isEmpty()) {
            return c.getSlug().trim();
        }
        return "course-" + c.getCourseId();
    }
}

package com.edumy.authoring.controller;

import com.edumy.authoring.repository.CategoryRepository;
import com.edumy.learning.enrollment.model.Certificate;
import com.edumy.learning.enrollment.repository.CertificateRepository;
import com.edumy.authoring.model.Course;
import com.edumy.catalog.dto.CourseQueryDto;
import com.edumy.authoring.repository.CourseRepository;
import com.edumy.authoring.repository.CourseTagRepository;
import com.edumy.authoring.dto.CreateCourseDto;
import com.edumy.learning.enrollment.model.Enrollment;
import com.edumy.learning.enrollment.repository.EnrollmentRepository;
import com.edumy.catalog.dto.InstantCourseSearchDTO;
import com.edumy.learning.enrollment.model.LessonProgress;
import com.edumy.learning.enrollment.repository.LessonProgressRepository;
import com.edumy.authoring.repository.LessonRepository;
import com.edumy.shared.response.PagedResultDto;
import com.edumy.learning.quiz.repository.QuizAttemptRepository;
import com.edumy.learning.quiz.repository.QuizRepository;
import com.edumy.learning.review.model.Review;
import com.edumy.learning.review.repository.ReviewRepository;
import com.edumy.authoring.repository.TagRepository;
import com.edumy.identity.user.service.UserActivityService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@RestController
@RequestMapping({"/api/Courses", "/api/courses", "/api/v1/courses"})
public class CoursesController {

    @GetMapping("/instant-search")
    public ResponseEntity<List<InstantCourseSearchDTO>> instantSearch(@RequestParam(name = "q", defaultValue = "") String keyword) {
        if (keyword == null || keyword.trim().length() < 2) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        String trimmed = keyword.trim();
        org.springframework.data.domain.Pageable limit = org.springframework.data.domain.PageRequest.of(0, 6);
        List<Course> courses = courseRepository.searchInstant(trimmed, limit);

        List<InstantCourseSearchDTO> result = courses.stream().map(c -> {
            String instructorName = "Giảng viên Edumy";
            try {
                if (c.getInstructor() != null && c.getInstructor().getFullName() != null && !c.getInstructor().getFullName().isBlank()) {
                    instructorName = c.getInstructor().getFullName();
                }
            } catch (Exception ignored) {
            }
            return InstantCourseSearchDTO.builder()
                    .id(c.getCourseId())
                    .title(c.getTitle())
                    .thumbnailUrl(c.getThumbnailUrl())
                    .instructorName(instructorName)
                    .price(c.getPrice())
                    .build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private TagRepository tagRepository;
    
    @Autowired
    private CourseTagRepository courseTagRepository;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private LessonProgressRepository lessonProgressRepository;
    
    @Autowired
    private LessonRepository lessonRepository;
    
    @Autowired
    private CertificateRepository certificateRepository;
    
    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private com.edumy.identity.user.service.UserActivityService userActivityService;

    @Autowired
    private com.edumy.learning.enrollment.service.LearningProgressService learningProgressService;

    private Integer getCurrentUserId() {
        try {
            return Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isUserInRole(String role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    @GetMapping("/recommend")
    public ResponseEntity<?> getRecommendations() {
        List<Course> courses = courseRepository.findAll().stream()
                .filter(c -> "Published".equalsIgnoreCase(c.getStatus()))
                .sorted((c1, c2) -> {
                    double r1 = c1.getAverageRating() != null ? c1.getAverageRating() : 0.0;
                    double r2 = c2.getAverageRating() != null ? c2.getAverageRating() : 0.0;
                    return Double.compare(r2, r1);
                })
                .limit(4)
                .collect(Collectors.toList());
        return ResponseEntity.ok(courses);
    }

    @GetMapping
    public ResponseEntity<?> getCourses(@ModelAttribute CourseQueryDto queryDto) {
        String keyword = (queryDto.getQ() != null && !queryDto.getQ().trim().isEmpty())
                ? queryDto.getQ().trim()
                : (queryDto.getSearch() != null ? queryDto.getSearch().trim() : "");
        if (!keyword.isEmpty()) {
            userActivityService.logSearch(getCurrentUserId(), keyword);
        }

        List<Course> all = courseRepository.findAll();
        
        List<Course> filtered = all.stream().filter(c -> {
            // UC-G01: Only display public/published courses in public catalog (exclude Hidden, Draft, Rejected, PendingApproval)
            String st = c.getStatus();
            if (st != null && ("Hidden".equalsIgnoreCase(st) || "Draft".equalsIgnoreCase(st)
                    || "Rejected".equalsIgnoreCase(st) || "PendingApproval".equalsIgnoreCase(st))) {
                return false;
            }
            boolean match = true;
            if (!keyword.isEmpty()) {
                String s = keyword.toLowerCase();
                match = (c.getTitle() != null && c.getTitle().toLowerCase().contains(s)) ||
                        (c.getDescription() != null && c.getDescription().toLowerCase().contains(s));
            }
            if (match && queryDto.getCategoryId() != null) {
                match = queryDto.getCategoryId().equals(c.getCategoryId());
            }
            if (match && queryDto.getLevel() != null && !queryDto.getLevel().isEmpty()) {
                match = queryDto.getLevel().equals(c.getLevel());
            }
            // UC-G03: Filter by priceType (FREE / PAID / ALL)
            if (match && queryDto.getPriceType() != null && !queryDto.getPriceType().isEmpty()) {
                BigDecimal p = c.getPrice() != null ? c.getPrice() : BigDecimal.ZERO;
                if ("FREE".equalsIgnoreCase(queryDto.getPriceType())) {
                    match = p.compareTo(BigDecimal.ZERO) == 0;
                } else if ("PAID".equalsIgnoreCase(queryDto.getPriceType())) {
                    match = p.compareTo(BigDecimal.ZERO) > 0;
                }
            }
            if (match && queryDto.getMinPrice() != null) {
                match = c.getPrice().compareTo(queryDto.getMinPrice()) >= 0;
            }
            if (match && queryDto.getMaxPrice() != null) {
                match = c.getPrice().compareTo(queryDto.getMaxPrice()) <= 0;
            }
            return match;
        }).collect(Collectors.toList());
        
        Comparator<Course> comparator = Comparator.comparing(Course::getCreatedAt).reversed(); 
        String sortOrder = queryDto.getSortOrder() != null ? queryDto.getSortOrder().toLowerCase() : "desc";
        
        if ("price".equalsIgnoreCase(queryDto.getSortBy())) {
            comparator = Comparator.comparing(Course::getPrice);
            if ("desc".equals(sortOrder)) comparator = comparator.reversed();
        } else if ("rating".equalsIgnoreCase(queryDto.getSortBy())) {
            comparator = Comparator.comparing(c -> c.getAverageRating() == null ? 0.0 : c.getAverageRating());
            if ("desc".equals(sortOrder)) comparator = comparator.reversed();
        } else if ("newest".equalsIgnoreCase(queryDto.getSortBy())) {
            comparator = Comparator.comparing(Course::getCreatedAt);
            if ("desc".equals(sortOrder)) comparator = comparator.reversed();
        }

        filtered.sort(comparator);

        int totalItems = filtered.size();
        int pageSize = queryDto.getPageSize() != null ? queryDto.getPageSize() : 12;
        int pageNumber = queryDto.getPageNumber() != null ? queryDto.getPageNumber() : 1;
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        int skip = (pageNumber - 1) * pageSize;
        List<Course> items = filtered.stream().skip(skip).limit(pageSize).collect(Collectors.toList());

        PagedResultDto<Course> result = new PagedResultDto<>();
        result.setItems(items);
        result.setPageNumber(pageNumber);
        result.setPageSize(pageSize);
        result.setTotalItems(totalItems);
        result.setTotalPages(totalPages);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/my-courses")
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> getMyCourses() {
        Integer userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();

        List<Course> courses = courseRepository.findAll().stream()
                .filter(c -> userId.equals(c.getInstructorId()))
                .sorted(Comparator.comparing(Course::getCreatedAt).reversed())
                .collect(Collectors.toList());

        return ResponseEntity.ok(courses);
    }

    @GetMapping("/enrolled")
    @PreAuthorize("hasAnyRole('Student', 'Instructor', 'Admin')")
    public ResponseEntity<?> getEnrolledCourses() {
        Integer userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();

        List<Enrollment> enrollments = enrollmentRepository.findAll().stream()
                .filter(e -> userId.equals(e.getUserId()))
                .sorted(Comparator.comparing(Enrollment::getEnrolledAt).reversed())
                .collect(Collectors.toList());

        List<Map<String, Object>> result = enrollments.stream().map(e -> {
            Map<String, Object> m = new HashMap<>();
            String instructorName = (e.getCourse() != null && e.getCourse().getInstructor() != null) ? e.getCourse().getInstructor().getFullName() : "Giảng viên Edumy";
            Integer courseId = e.getCourseId();

            // UC-S11: Tính tiến độ dựa trên số bài học đã hoàn thành trên tổng số bài học của khóa
            long totalLessons = lessonRepository.countLessonsByCourseId(courseId);
            long completedLessons = lessonProgressRepository.countCompletedLessonsByUserAndCourse(userId, courseId);
            int actualProgress = totalLessons > 0 ? (int) Math.min(100, Math.round((double) completedLessons / totalLessons * 100)) : 0;

            m.put("courseId", courseId);
            m.put("title", e.getCourse() != null ? e.getCourse().getTitle() : null);
            m.put("thumbnailUrl", e.getCourse() != null ? e.getCourse().getThumbnailUrl() : null);
            m.put("courseStatus", e.getCourse() != null ? e.getCourse().getStatus() : "Published");
            m.put("instructorName", instructorName);
            m.put("fullName", instructorName);
            m.put("instructor", instructorName);
            m.put("enrolledAt", e.getEnrolledAt());
            m.put("completedLessons", completedLessons);
            m.put("totalLessons", totalLessons);
            m.put("progressPercentage", actualProgress);
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCourse(@PathVariable Integer id) {
        return courseRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/enrollment-status")
    public ResponseEntity<?> getEnrollmentStatus(@PathVariable Integer id) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.ok(Map.of("isEnrolled", false, "status", "NONE"));
        }
        boolean isEnrolled = enrollmentRepository.isUserEnrolledActive(userId, id);
        return ResponseEntity.ok(Map.of(
            "isEnrolled", isEnrolled,
            "status", isEnrolled ? "ACTIVE" : "NONE"
        ));
    }

    @GetMapping({"/enrolled-ids", "/my-enrolled-ids"})
    public ResponseEntity<?> getMyEnrolledCourseIds() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        List<Enrollment> userEnrollments = enrollmentRepository.findByUserId(userId);
        List<Integer> enrolledIds = userEnrollments.stream()
                .filter(e -> e.getStatus() == null || "ACTIVE".equalsIgnoreCase(e.getStatus()) || "Completed".equalsIgnoreCase(e.getStatus()))
                .map(Enrollment::getCourseId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        return ResponseEntity.ok(enrolledIds);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> createCourse(@RequestBody CreateCourseDto dto) {
        Integer userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();

        String slug = generateSlug(dto.getTitle());

        Course course = new Course();
        course.setInstructorId(userId);
        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        course.setPrice(dto.getPrice());
        course.setLevel(dto.getLevel());
        course.setThumbnailUrl(dto.getThumbnailUrl());
        course.setSlug(slug);
        course.setStatus("Draft");
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());

        if (dto.getCategoryId() != null && dto.getCategoryId() > 0) {
            course.setCategoryId(dto.getCategoryId());
        } else {
            course.setCategoryId(1);
        }

        course = courseRepository.save(course);
        return ResponseEntity.created(java.net.URI.create("/api/Courses/" + course.getCourseId())).body(course);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> updateCourse(@PathVariable Integer id, @RequestBody Course course) {
        if (!id.equals(course.getCourseId())) return ResponseEntity.badRequest().build();

        Course existing = courseRepository.findById(id).orElse(null);
        if (existing == null) return ResponseEntity.notFound().build();

        Integer userId = getCurrentUserId();
        if (userId != null && !userId.equals(existing.getInstructorId()) && !isUserInRole("Admin")) {
            return ResponseEntity.status(403).build();
        }

        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> deleteCourse(@PathVariable Integer id) {
        Course course = courseRepository.findById(id).orElse(null);
        if (course == null) return ResponseEntity.notFound().build();

        Integer userId = getCurrentUserId();
        if (userId != null && !userId.equals(course.getInstructorId()) && !isUserInRole("Admin")) {
            return ResponseEntity.status(403).build();
        }

        courseRepository.delete(course);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/{id}/status", method = {RequestMethod.POST, RequestMethod.PUT})
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> updateStatus(@PathVariable Integer id, @RequestBody Object body) {
        String newStatus = "";
        if (body instanceof Map) {
            Object s = ((Map<?, ?>) body).get("status");
            newStatus = s != null ? s.toString() : "";
        } else {
            newStatus = body != null ? body.toString().replace("\"", "").trim() : "";
        }

        List<String> validStatuses = Arrays.asList("Draft", "NeedsReview", "PendingApproval", "Published");
        if (!validStatuses.contains(newStatus)) {
            return ResponseEntity.badRequest().body("Invalid status.");
        }

        Course course = courseRepository.findById(id).orElse(null);
        if (course == null) return ResponseEntity.notFound().build();

        Integer userId = getCurrentUserId();
        if (userId != null && !userId.equals(course.getInstructorId()) && !isUserInRole("Admin")) {
            return ResponseEntity.status(403).build();
        }

        if ("Published".equals(newStatus) && !isUserInRole("Admin")) {
            return ResponseEntity.status(403).body("Only admins can publish courses.");
        }

        course.setStatus(newStatus);
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);

        return ResponseEntity.ok(Map.of("message", "Status updated to " + newStatus, "status", newStatus));
    }

    private static String generateSlug(String title) {
        if (title == null || title.isEmpty()) return "";
        String cleanSlug = com.edumy.shared.text.SlugGenerator.slugify(title, 80);
        if (cleanSlug.isEmpty()) {
            cleanSlug = "course";
        }
        return cleanSlug + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
    }

    @PostMapping("/{courseId}/lessons/{lessonId}/complete")
    @PreAuthorize("hasRole('Student')")
    public ResponseEntity<?> completeLesson(@PathVariable Integer courseId, @PathVariable Integer lessonId) {
        Integer userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();

        boolean isEnrolled = enrollmentRepository.isUserEnrolledActive(userId, courseId);
        if (!isEnrolled) {
            return ResponseEntity.status(403).body(Map.of("message", "Bạn phải có lượt ghi danh để hoàn thành bài học."));
        }

        learningProgressService.markLesson(userId, lessonId, true);
        return ResponseEntity.ok(Map.of("message", "Đã ghi nhận hoàn thành bài học thành công."));
    }

    @GetMapping("/{courseId}/progress")
    @PreAuthorize("hasRole('Student')")
    public ResponseEntity<?> getCourseProgress(@PathVariable Integer courseId) {
        Integer userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();

        var snap = learningProgressService.getProgress(userId, courseId);

        Map<String, Object> res = new HashMap<>();
        res.put("completedLessonIds", snap.completedLessonIds());
        res.put("completedQuizIds", snap.completedQuizIds());
        res.put("totalLessons", snap.totalLessons());
        res.put("totalQuizzes", snap.totalQuizzes());
        res.put("completedQuizzes", snap.completedQuizzes());
        res.put("totalItems", snap.totalItems());
        res.put("completedItems", snap.completedItems());
        res.put("progressPercentage", snap.progressPercentage());
        res.put("certificateId", snap.progressPercentage() >= 100 ? snap.certificateId() : null);
        res.put("certificateUrl", snap.progressPercentage() >= 100 ? snap.certificateUrl() : null);
        res.put("lastAccessedLessonId", snap.lastAccessedLessonId());

        return ResponseEntity.ok(res);
    }

    @PostMapping("/{id}/reviews")
    @PreAuthorize("hasRole('Student')")
    @ApiResponse(responseCode = "200", description = "Review created", content = @Content(schema = @Schema(implementation = Review.class)))
    public ResponseEntity<?> addReview(@PathVariable Integer id, @RequestBody CreateReviewDto dto) {
        Integer userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();

        Course course = courseRepository.findById(id).orElse(null);
        if (course == null) return ResponseEntity.notFound().build();

        boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseIdAndStatus(userId, id, "ACTIVE");
        if (!isEnrolled) {
            return ResponseEntity.status(403).body(Map.of("message", "Bạn phải đăng ký và có tài khoản ACTIVE mới được đánh giá khóa học."));
        }

        boolean alreadyReviewed = reviewRepository.existsByUserIdAndCourseId(userId, id);
        if (alreadyReviewed) {
            return ResponseEntity.badRequest().body(Map.of("message", "Bạn đã gửi đánh giá cho khóa học này rồi."));
        }

        if (dto.getRating() == null || dto.getRating() < 1 || dto.getRating() > 5) {
            return ResponseEntity.badRequest().body(Map.of("message", "Số sao đánh giá phải từ 1 đến 5 sao."));
        }

        Review review = new Review();
        review.setUserId(userId);
        review.setCourseId(id);
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());

        review.setSentimentLabel(dto.getRating() >= 4 ? "POSITIVE" : (dto.getRating() == 3 ? "NEUTRAL" : "NEGATIVE"));
        review.setSentimentScore(dto.getRating() >= 4 ? 0.90 : (dto.getRating() == 3 ? 0.50 : 0.20));

        review.setCreatedAt(LocalDateTime.now());
        
        review = reviewRepository.save(review);
        
        List<Review> courseReviews = reviewRepository.findByCourseId(id);
        double avg = courseReviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        course.setAverageRating(Math.round(avg * 10.0) / 10.0);
        courseRepository.save(course);

        return ResponseEntity.ok(review);
    }
}

@Data
class CreateReviewDto {
    private Integer rating;
    private String comment;
}

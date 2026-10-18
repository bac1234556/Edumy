package com.edumy.backend.controller;

import com.edumy.backend.model.*;
import com.edumy.backend.dto.*;
import com.edumy.backend.repository.*;
import com.edumy.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@RestController
@RequestMapping({"/api/Courses", "/api/courses"})
public class CoursesController {

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
    private ReviewRepository reviewRepository;
    
    @Autowired
    private CourseMlAnalysisRepository courseMlAnalysisRepository;

    @Autowired
    private com.edumy.backend.service.MachineLearningService machineLearningService;

    @Autowired
    private com.edumy.backend.service.UserActivityService userActivityService;

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
    @PreAuthorize("hasRole('Student')")
    public ResponseEntity<?> getRecommendations() {
        Integer userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();

        var recResult = machineLearningService.recommendCourses(userId);
        List<Integer> recommendedCourseIds = recResult != null ? recResult.getRecommendedCourseIds() : new ArrayList<>(); 
        
        if (!recommendedCourseIds.isEmpty()) {
            List<Course> courses = courseRepository.findAllById(recommendedCourseIds);
            
            List<Course> ordered = new ArrayList<>();
            for (Integer id : recommendedCourseIds) {
                courses.stream().filter(c -> c.getCourseId().equals(id)).findFirst().ifPresent(ordered::add);
            }
            return ResponseEntity.ok(ordered);
        }

        return ResponseEntity.ok(new ArrayList<Course>());
    }

    @GetMapping
    public ResponseEntity<?> getCourses(@ModelAttribute CourseQueryDto queryDto) {
        if (queryDto.getSearch() != null && !queryDto.getSearch().trim().isEmpty()) {
            userActivityService.logSearch(getCurrentUserId(), queryDto.getSearch().trim());
        }

        List<Course> all = courseRepository.findAll();
        
        List<Course> filtered = all.stream().filter(c -> {
            boolean match = true;
            if (queryDto.getSearch() != null && !queryDto.getSearch().isEmpty()) {
                String s = queryDto.getSearch().toLowerCase();
                match = (c.getTitle() != null && c.getTitle().toLowerCase().contains(s)) ||
                        (c.getDescription() != null && c.getDescription().toLowerCase().contains(s));
            }
            if (match && queryDto.getCategoryId() != null) {
                match = queryDto.getCategoryId().equals(c.getCategoryId());
            }
            if (match && queryDto.getLevel() != null && !queryDto.getLevel().isEmpty()) {
                match = queryDto.getLevel().equals(c.getLevel());
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
            m.put("courseId", e.getCourseId());
            m.put("title", e.getCourse() != null ? e.getCourse().getTitle() : null);
            m.put("thumbnailUrl", e.getCourse() != null ? e.getCourse().getThumbnailUrl() : null);
            m.put("instructorName", (e.getCourse() != null && e.getCourse().getInstructor() != null) ? e.getCourse().getInstructor().getFullName() : null);
            m.put("enrolledAt", e.getEnrolledAt());
            m.put("progressPercentage", e.getProgressPercentage());
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
            return ResponseEntity.ok(Map.of("isEnrolled", false));
        }
        boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseId(userId, id);
        return ResponseEntity.ok(Map.of("isEnrolled", isEnrolled));
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

        if (!Objects.equals(existing.getTitle(), course.getTitle()) || !Objects.equals(existing.getDescription(), course.getDescription())) {
            course.setNeedsReanalysis(true);
        } else {
            course.setNeedsReanalysis(existing.getNeedsReanalysis());
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

        List<String> validStatuses = Arrays.asList("Draft", "Analyzing", "NeedsReview", "PendingApproval", "Published");
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

        if ("Analyzing".equals(newStatus)) {
            course.setStatus("PendingApproval");
            course.setNeedsReanalysis(false);
            course.setUpdatedAt(LocalDateTime.now());
            courseRepository.save(course);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Analysis completed. Course set to PendingApproval.");
            response.put("categoryId", course.getCategoryId());
            return ResponseEntity.ok(response);
        }

        course.setStatus(newStatus);
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);

        return ResponseEntity.ok(Map.of("message", "Status updated to " + newStatus, "status", newStatus));
    }

    private static String generateSlug(String title) {
        if (title == null || title.isEmpty()) return "";
        String slug = title.toLowerCase().trim().replaceAll(" ", "-").replaceAll("--", "-");
        slug = slug.replaceAll("[^a-z0-9\\-]", "");
        slug += "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        return slug;
    }

    @PostMapping("/{courseId}/lessons/{lessonId}/complete")
    @PreAuthorize("hasRole('Student')")
    public ResponseEntity<?> completeLesson(@PathVariable Integer courseId, @PathVariable Integer lessonId) {
        Integer userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();

        boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseIdAndStatus(userId, courseId, "ACTIVE");
        if (!isEnrolled) {
            return ResponseEntity.status(403).body(Map.of("message", "Bạn phải có lượt ghi danh ACTIVE để hoàn thành bài học."));
        }

        boolean alreadyCompleted = lessonProgressRepository.existsByUserIdAndCourseIdAndLessonId(userId, courseId, lessonId);

        if (!alreadyCompleted) {
            LessonProgress progress = new LessonProgress();
            progress.setUserId(userId);
            progress.setCourseId(courseId);
            progress.setLessonId(lessonId);
            progress.setCompletedAt(LocalDateTime.now());
            lessonProgressRepository.save(progress);

            // Truy vấn COUNT tối ưu qua CSDL thay vì load toàn bộ bảng vào RAM
            long totalLessons = lessonRepository.countLessonsByCourseId(courseId);
            long completedLessons = lessonProgressRepository.countCompletedLessonsByUserAndCourse(userId, courseId);

            int progressPercentage = (totalLessons > 0)
                    ? (int) Math.min(100, Math.round((double) completedLessons / totalLessons * 100))
                    : 100;

            Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId);
            if (enrollment != null) {
                enrollment.setProgressPercentage(progressPercentage);
                if (progressPercentage >= 100 && enrollment.getCompletedAt() == null) {
                    enrollment.setCompletedAt(LocalDateTime.now());
                }
                enrollmentRepository.save(enrollment);
            }

            // Tự động sinh Certificate khi hoàn thành toàn bộ bài học
            if (totalLessons > 0 && completedLessons >= totalLessons) {
                boolean hasCert = certificateRepository.existsByUserIdAndCourseId(userId, courseId);
                if (!hasCert) {
                    Certificate cert = new Certificate();
                    cert.setUserId(userId);
                    cert.setCourseId(courseId);
                    cert.setIssuedAt(LocalDateTime.now());
                    cert.setCertificateUrl(UUID.randomUUID().toString().replace("-", ""));
                    certificateRepository.save(cert);
                }
            }
        }

        return ResponseEntity.ok(Map.of("message", "Đã ghi nhận hoàn thành bài học thành công."));
    }

    @GetMapping("/{courseId}/progress")
    @PreAuthorize("hasRole('Student')")
    public ResponseEntity<?> getCourseProgress(@PathVariable Integer courseId) {
        Integer userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();

        long totalLessons = lessonRepository.countLessonsByCourseId(courseId);
        List<LessonProgress> progresses = lessonProgressRepository.findByUserIdAndCourseId(userId, courseId);
        List<Integer> completedLessonIds = progresses.stream().map(LessonProgress::getLessonId).collect(Collectors.toList());

        double percentage = totalLessons > 0 ? (double) completedLessonIds.size() / totalLessons * 100 : 0;

        Certificate cert = certificateRepository.findByUserIdAndCourseId(userId, courseId);

        Map<String, Object> res = new HashMap<>();
        res.put("completedLessonIds", completedLessonIds);
        res.put("totalLessons", totalLessons);
        res.put("progressPercentage", Math.round(percentage * 100.0) / 100.0);
        res.put("certificateId", cert != null ? cert.getId() : null);
        res.put("certificateUrl", cert != null ? cert.getCertificateUrl() : null);

        return ResponseEntity.ok(res);
    }

    @PostMapping("/{id}/reviews")
    @PreAuthorize("hasRole('Student')")
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
        review.setSentimentLabel("Neutral");
        review.setSentimentScore(0.5);
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

package com.edumy.identity.user.controller;

import com.edumy.authoring.model.Course;
import com.edumy.authoring.repository.CourseRepository;
import com.edumy.learning.enrollment.model.Enrollment;
import com.edumy.learning.enrollment.repository.EnrollmentRepository;
import com.edumy.commerce.order.model.OrderItem;
import com.edumy.commerce.order.repository.OrderItemRepository;
import com.edumy.learning.review.model.Review;
import com.edumy.learning.review.repository.ReviewRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@RestController
@RequestMapping({"/api/Instructor", "/api/instructor"})
@PreAuthorize("hasAnyRole('Instructor', 'Admin')")
public class InstructorController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired(required = false)
    private com.edumy.learning.enrollment.service.LearningProgressService learningProgressService;

    private Integer getCurrentUserId() {
        try {
            return Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());
        } catch (Exception e) {
            return null;
        }
    }

    @Deprecated
    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats() {
        Integer instructorId = getCurrentUserId();
        if (instructorId == null) return ResponseEntity.status(401).build();

        List<Course> courses = courseRepository.findByInstructorId(instructorId);
        List<Integer> courseIds = courses.stream().map(Course::getCourseId).toList();
        long totalCourses = courses.size();

        long totalStudents = courseIds.isEmpty() ? 0 : enrollmentRepository.countDistinctUsersByCourseIds(courseIds);
        BigDecimal totalRevenue = courseIds.isEmpty()
                ? BigDecimal.ZERO
                : orderItemRepository.sumPaidGrossSalesByCourseIds(courseIds);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        double averageRating = courses.stream()
                .filter(c -> c.getAverageRating() != null && c.getAverageRating() > 0)
                .mapToDouble(Course::getAverageRating)
                .average().orElse(0.0);

        List<Map<String, Object>> recentReviews = courseIds.isEmpty()
                ? Collections.emptyList()
                : reviewRepository.findTop5ByCourseIdInOrderByCreatedAtDesc(courseIds).stream()
                        .map(r -> {
                            Map<String, Object> m = new HashMap<>();
                            m.put("reviewId", r.getReviewId());
                            m.put("rating", r.getRating());
                            m.put("comment", r.getComment());
                            m.put("sentimentLabel", r.getSentimentLabel());
                            m.put("courseTitle", r.getCourse() != null ? r.getCourse().getTitle() : "");
                            m.put("studentName", r.getUser() != null ? r.getUser().getFullName() : null);
                            m.put("createdAt", r.getCreatedAt());
                            return m;
                        }).collect(Collectors.toList());

        List<Object> revenueByDate = new ArrayList<>();
        List<Object> enrollmentByDate = new ArrayList<>();
        List<Object> sentimentStats = new ArrayList<>();
        
        double averageQualityScore = Math.min(100.0, Math.max(80.0, averageRating * 20.0));
        List<String> recommendations = new ArrayList<>();

        Map<String, Object> response = new HashMap<>();
        response.put("totalCourses", totalCourses);
        response.put("totalStudents", totalStudents);
        response.put("totalRevenue", totalRevenue);
        response.put("averageRating", Math.round(averageRating * 10.0) / 10.0);
        response.put("recentReviews", recentReviews);
        response.put("revenueByDate", revenueByDate);
        response.put("enrollmentByDate", enrollmentByDate);
        response.put("sentimentStats", sentimentStats);
        response.put("averageQualityScore", Math.round(averageQualityScore * 10.0) / 10.0);
        response.put("recommendations", recommendations);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/courses/{courseId}/students")
    public ResponseEntity<?> getCourseStudents(@PathVariable Integer courseId) {
        Integer instructorId = getCurrentUserId();
        if (instructorId == null) return ResponseEntity.status(401).build();

        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) return ResponseEntity.notFound().build();

        if (!instructorId.equals(course.getInstructorId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Bạn không có quyền quản lý khóa học này."));
        }

        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        List<Map<String, Object>> students = enrollments.stream().map(e -> {
            Map<String, Object> m = new HashMap<>();
            Integer progressPct = learningProgressService != null
                    ? learningProgressService.getProgress(e.getUserId(), courseId).progressPercentage()
                    : e.getProgressPercentage();
            m.put("enrollmentId", e.getEnrollmentId());
            m.put("userId", e.getUserId());
            m.put("fullName", e.getUser() != null ? e.getUser().getFullName() : "Học viên #" + e.getUserId());
            m.put("enrolledAt", e.getEnrolledAt());
            m.put("progressPercentage", progressPct);
            m.put("status", e.getStatus());
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(students);
    }
}

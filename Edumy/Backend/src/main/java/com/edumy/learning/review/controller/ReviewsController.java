package com.edumy.learning.review.controller;

import com.edumy.authoring.model.Course;
import com.edumy.authoring.repository.CourseRepository;
import com.edumy.learning.enrollment.repository.EnrollmentRepository;
import com.edumy.learning.review.model.Review;
import com.edumy.learning.review.repository.ReviewRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/Reviews", "/api/v1/reviews", "/api/Reviews", "/api/reviews"})
public class ReviewsController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

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

    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getCourseReviews(@PathVariable Integer courseId) {
        List<Review> reviews = reviewRepository.findByCourseId(courseId).stream()
                .filter(r -> !"HIDDEN_BY_ADMIN".equalsIgnoreCase(r.getVisibilityStatus()))
                .sorted(Comparator.comparing(Review::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/course/{courseId}")
    @PreAuthorize("hasRole('Student')")
    public ResponseEntity<?> createReview(@PathVariable Integer courseId, @RequestBody Review review) {
        Integer userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();

        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) return ResponseEntity.notFound().build();

        boolean isEnrolled = enrollmentRepository.isUserEnrolledActive(userId, courseId);
        if (!isEnrolled) {
            return ResponseEntity.status(403).body(Map.of("message", "Bạn phải ghi danh khóa học mới được đánh giá."));
        }

        boolean alreadyReviewed = reviewRepository.existsByUserIdAndCourseId(userId, courseId);
        if (alreadyReviewed) {
            return ResponseEntity.badRequest().body(Map.of("message", "Bạn đã đánh giá khóa học này rồi. Vui lòng cập nhật đánh giá nếu muốn thay đổi."));
        }

        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            return ResponseEntity.badRequest().body(Map.of("message", "Số sao đánh giá phải từ 1 đến 5 sao."));
        }

        int rating = review.getRating();
        review.setSentimentLabel(rating >= 4 ? "POSITIVE" : (rating == 3 ? "NEUTRAL" : "NEGATIVE"));
        review.setSentimentScore(rating / 5.0);

        review.setCourseId(courseId);
        review.setUserId(userId);
        review.setVisibilityStatus("VISIBLE");
        review.setCreatedAt(LocalDateTime.now());

        Review saved = reviewRepository.save(review);
        recalculateCourseAverageRating(courseId);

        return ResponseEntity.created(java.net.URI.create("/api/Reviews/course/" + courseId)).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @ApiResponse(responseCode = "204", description = "Review updated", content = @Content)
    public ResponseEntity<?> updateReview(@PathVariable Integer id, @RequestBody Review review) {
        if (review.getReviewId() != null && !id.equals(review.getReviewId())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Review ID mismatch."));
        }

        Review existing = reviewRepository.findById(id).orElse(null);
        if (existing == null) return ResponseEntity.notFound().build();

        Integer userId = getCurrentUserId();
        if (userId != null && !userId.equals(existing.getUserId()) && !isUserInRole("Admin")) {
            return ResponseEntity.status(403).build();
        }

        // AC-30: Đánh giá đã bị Quản trị viên ẩn thì học viên không thể chỉnh sửa
        if ("HIDDEN_BY_ADMIN".equalsIgnoreCase(existing.getVisibilityStatus()) && !isUserInRole("Admin")) {
            return ResponseEntity.status(409).body(Map.of("message", "Đánh giá đã bị ẩn bởi Quản trị viên, không thể chỉnh sửa."));
        }

        if (review.getRating() != null) {
            if (review.getRating() < 1 || review.getRating() > 5) {
                return ResponseEntity.badRequest().body(Map.of("message", "Số sao đánh giá phải từ 1 đến 5 sao."));
            }
            existing.setRating(review.getRating());
            existing.setSentimentLabel(review.getRating() >= 4 ? "POSITIVE" : (review.getRating() == 3 ? "NEUTRAL" : "NEGATIVE"));
            existing.setSentimentScore(review.getRating() / 5.0);
        }
        if (review.getComment() != null) {
            existing.setComment(review.getComment());
        }

        reviewRepository.save(existing);
        recalculateCourseAverageRating(existing.getCourseId());

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @ApiResponse(responseCode = "204", description = "Review deleted", content = @Content)
    public ResponseEntity<?> deleteReview(@PathVariable Integer id) {
        Review review = reviewRepository.findById(id).orElse(null);
        if (review == null) return ResponseEntity.notFound().build();

        Integer userId = getCurrentUserId();
        if (userId != null && !userId.equals(review.getUserId()) && !isUserInRole("Admin")) {
            return ResponseEntity.status(403).build();
        }

        Integer courseId = review.getCourseId();
        reviewRepository.delete(review);
        recalculateCourseAverageRating(courseId);

        return ResponseEntity.noContent().build();
    }

    private void recalculateCourseAverageRating(Integer courseId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) return;
        List<Review> visible = reviewRepository.findByCourseId(courseId).stream()
                .filter(r -> !"HIDDEN_BY_ADMIN".equalsIgnoreCase(r.getVisibilityStatus()))
                .collect(Collectors.toList());
        double avg = visible.isEmpty() ? 0.0 : visible.stream().mapToInt(Review::getRating).average().orElse(0.0);
        course.setAverageRating(Math.round(avg * 10.0) / 10.0);
        courseRepository.save(course);
    }
}

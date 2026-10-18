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
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

@RestController
@RequestMapping({"/api/Reviews", "/api/reviews"})
public class ReviewsController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private com.edumy.backend.service.MachineLearningService machineLearningService;

    @Autowired
    private com.edumy.backend.service.CourseAiService courseAiService;

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
        List<Review> reviews = reviewRepository.findAll().stream()
                .filter(r -> courseId.equals(r.getCourseId()))
                .sorted(Comparator.comparing(Review::getCreatedAt).reversed())
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

        boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseIdAndStatus(userId, courseId, "ACTIVE");
        if (!isEnrolled) {
            return ResponseEntity.status(403).body(Map.of("message", "Bạn phải có trạng thái ghi danh ACTIVE mới được đánh giá khóa học này."));
        }

        boolean alreadyReviewed = reviewRepository.existsByUserIdAndCourseId(userId, courseId);
        if (alreadyReviewed) {
            return ResponseEntity.badRequest().body(Map.of("message", "Bạn đã đánh giá khóa học này rồi. Vui lòng cập nhật đánh giá nếu muốn thay đổi."));
        }

        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            return ResponseEntity.badRequest().body(Map.of("message", "Số sao đánh giá phải từ 1 đến 5 sao."));
        }

        // Gọi AI Service để phân tích cảm xúc đánh giá kết hợp số sao
        com.edumy.backend.dto.SentimentAnalysisResponse sentiment = 
                courseAiService.analyzeSentiment(review.getRating(), review.getComment());
        if (sentiment != null) {
            review.setSentimentLabel(sentiment.getSentiment());
            review.setSentimentScore(sentiment.getScore());
        } else {
            review.setSentimentLabel("POSITIVE");
            review.setSentimentScore(0.85);
        }

        review.setCourseId(courseId);
        review.setUserId(userId);
        review.setCreatedAt(LocalDateTime.now());

        Review saved = reviewRepository.save(review);

        List<Review> courseReviews = reviewRepository.findByCourseId(courseId);
        double avg = courseReviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        course.setAverageRating(Math.round(avg * 10.0) / 10.0);
        courseRepository.save(course);

        return ResponseEntity.created(java.net.URI.create("/api/Reviews/course/" + courseId)).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateReview(@PathVariable Integer id, @RequestBody Review review) {
        if (!id.equals(review.getReviewId())) return ResponseEntity.badRequest().build();

        Review existing = reviewRepository.findById(id).orElse(null);
        if (existing == null) return ResponseEntity.notFound().build();

        Integer userId = getCurrentUserId();
        if (userId != null && !userId.equals(existing.getUserId()) && !isUserInRole("Admin")) {
            return ResponseEntity.status(403).build();
        }

        review.setUserId(existing.getUserId());
        review.setCourseId(existing.getCourseId());
        review.setCreatedAt(existing.getCreatedAt());
        
        reviewRepository.save(review);
        
        List<Review> courseReviews = reviewRepository.findAll().stream().filter(r -> review.getCourseId().equals(r.getCourseId())).collect(Collectors.toList());
        double avg = courseReviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        
        Course course = courseRepository.findById(review.getCourseId()).orElse(null);
        if (course != null) {
            course.setAverageRating(Math.round(avg * 10.0) / 10.0);
            courseRepository.save(course);
        }

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteReview(@PathVariable Integer id) {
        Review review = reviewRepository.findById(id).orElse(null);
        if (review == null) return ResponseEntity.notFound().build();

        Integer userId = getCurrentUserId();
        if (userId != null && !userId.equals(review.getUserId()) && !isUserInRole("Admin")) {
            return ResponseEntity.status(403).build();
        }

        Integer courseId = review.getCourseId();
        reviewRepository.delete(review);

        List<Review> remaining = reviewRepository.findAll().stream().filter(r -> courseId.equals(r.getCourseId())).collect(Collectors.toList());
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course != null) {
            double avg = remaining.isEmpty() ? 0.0 : remaining.stream().mapToInt(Review::getRating).average().orElse(0.0);
            course.setAverageRating(Math.round(avg * 10.0) / 10.0);
            courseRepository.save(course);
        }

        return ResponseEntity.noContent().build();
    }
}

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

@RestController
@RequestMapping("/api/Instructor")
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

    @Autowired
    private CourseMlAnalysisRepository courseMlAnalysisRepository;

    private Integer getCurrentUserId() {
        try {
            return Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());
        } catch (Exception e) {
            return null;
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats() {
        Integer instructorId = getCurrentUserId();
        if (instructorId == null) return ResponseEntity.status(401).build();

        long totalCourses = courseRepository.findAll().stream().filter(c -> instructorId.equals(c.getInstructorId())).count();
        
        long totalStudents = enrollmentRepository.findAll().stream()
                .filter(e -> e.getCourse() != null && instructorId.equals(e.getCourse().getInstructorId()))
                .map(Enrollment::getUserId)
                .distinct().count();

        BigDecimal totalRevenue = orderItemRepository.findAll().stream()
                .filter(oi -> oi.getCourse() != null && instructorId.equals(oi.getCourse().getInstructorId()) && oi.getOrder() != null && "Completed".equals(oi.getOrder().getStatus()))
                .map(OrderItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double averageRating = courseRepository.findAll().stream()
                .filter(c -> instructorId.equals(c.getInstructorId()) && c.getAverageRating() != null && c.getAverageRating() > 0)
                .mapToDouble(Course::getAverageRating)
                .average().orElse(0.0);

        List<Map<String, Object>> recentReviews = reviewRepository.findAll().stream()
                .filter(r -> r.getCourse() != null && instructorId.equals(r.getCourse().getInstructorId()))
                .sorted(Comparator.comparing(Review::getCreatedAt).reversed())
                .limit(5)
                .map(r -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("reviewId", r.getReviewId());
                    m.put("rating", r.getRating());
                    m.put("comment", r.getComment());
                    m.put("sentimentLabel", r.getSentimentLabel());
                    m.put("courseTitle", r.getCourse().getTitle());
                    m.put("studentName", r.getUser() != null ? r.getUser().getFullName() : null);
                    m.put("createdAt", r.getCreatedAt());
                    return m;
                }).collect(Collectors.toList());

        List<Object> revenueByDate = new ArrayList<>();
        List<Object> enrollmentByDate = new ArrayList<>();
        List<Object> sentimentStats = new ArrayList<>();
        
        List<Double> mlScores = courseMlAnalysisRepository.findAll().stream()
                .filter(ma -> ma.getCourse() != null && instructorId.equals(ma.getCourse().getInstructorId()))
                .map(CourseMlAnalysis::getQualityScore)
                .map(Integer::doubleValue)
                .collect(Collectors.toList());
        double averageQualityScore = mlScores.stream().mapToDouble(d -> d).average().orElse(0.0);

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
}

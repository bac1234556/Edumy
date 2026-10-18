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
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@RestController
@RequestMapping({"/api/Admin", "/api/admin"})
@PreAuthorize("hasRole('Admin')")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CourseMlAnalysisRepository courseMlAnalysisRepository;

    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserActivityRepository userActivityRepository;

    @GetMapping("/activities")
    public ResponseEntity<?> getActivities() {
        var list = userActivityRepository.findAll().stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(50)
                .map(a -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", a.getId());
                    m.put("userId", a.getUserId());
                    m.put("userEmail", a.getUser() != null ? a.getUser().getEmail() : "user_" + a.getUserId());
                    m.put("action", a.getActivityType());
                    m.put("resourceId", a.getResourceId());
                    m.put("timestamp", a.getTimestamp());
                    return m;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalCourses = courseRepository.count();
        
        BigDecimal totalRevenue = orderRepository.findAll().stream()
                .filter(o -> "PAID".equalsIgnoreCase(o.getStatus()) || "Completed".equalsIgnoreCase(o.getStatus()))
                .map(Order::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> response = new HashMap<>();
        // Hỗ trợ cả 2 chuẩn camelCase và PascalCase cho Frontend
        response.put("totalUsers", totalUsers);
        response.put("totalCourses", totalCourses);
        response.put("totalRevenue", totalRevenue);
        response.put("TotalUsers", totalUsers);
        response.put("TotalCourses", totalCourses);
        response.put("TotalRevenue", totalRevenue);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        List<Map<String, Object>> users = userRepository.findAll().stream()
                .sorted((u1, u2) -> u2.getCreatedAt().compareTo(u1.getCreatedAt()))
                .map(u -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("userId", u.getUserId());
                    m.put("fullName", u.getFullName());
                    m.put("email", u.getEmail());
                    m.put("role", u.getRole());
                    m.put("createdAt", u.getCreatedAt());
                    m.put("isActive", u.getIsActive());
                    return m;
                }).collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }

    @GetMapping("/courses")
    public ResponseEntity<?> getAllCourses() {
        List<Course> courses = courseRepository.findAll().stream()
                .sorted((c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(courses);
    }

    @PutMapping("/courses/{id}/status")
    public ResponseEntity<?> updateCourseStatus(@PathVariable Integer id, @RequestBody Object body) {
        Course course = courseRepository.findById(id).orElse(null);
        if (course == null) return ResponseEntity.notFound().build();

        String statusStr = "";
        if (body instanceof Map) {
            Object s = ((Map<?, ?>) body).get("status");
            statusStr = s != null ? s.toString() : "";
        } else {
            statusStr = body != null ? body.toString().replace("\"", "").trim() : "";
        }

        course.setStatus(statusStr);
        courseRepository.save(course);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Status updated to " + statusStr);
        response.put("status", statusStr);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{id}/toggle-status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Integer id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        user.setIsActive(!user.getIsActive());
        userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User status updated to " + (user.getIsActive() ? "Active" : "Blocked"));
        response.put("isActive", user.getIsActive());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Integer id, @RequestBody String newRole) {
        newRole = newRole.replace("\"", "");
        if (!newRole.equals("Admin") && !newRole.equals("Instructor") && !newRole.equals("Student")) {
            return ResponseEntity.badRequest().body("Invalid role.");
        }

        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        user.setRole(newRole);
        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User role updated to " + newRole);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ml-monitoring")
    public ResponseEntity<?> getMlMonitoring() {
        long totalAnalyses = courseMlAnalysisRepository.count();
        long highRiskCount = courseMlAnalysisRepository.countByRiskLevel("High");
        long pendingReviews = courseMlAnalysisRepository.countByStatusIn(List.of("NeedsManualReview", "InstructorConfirmationRequired"));
        
        long totalReviews = reviewRepository.count();
        long posCount = reviewRepository.findAll().stream()
                .filter(r -> "POSITIVE".equalsIgnoreCase(r.getSentimentLabel()) || "POS".equalsIgnoreCase(r.getSentimentLabel())).count();
        long negCount = reviewRepository.findAll().stream()
                .filter(r -> "NEGATIVE".equalsIgnoreCase(r.getSentimentLabel()) || "NEG".equalsIgnoreCase(r.getSentimentLabel())).count();
        long neuCount = reviewRepository.findAll().stream()
                .filter(r -> "NEUTRAL".equalsIgnoreCase(r.getSentimentLabel()) || "NEU".equalsIgnoreCase(r.getSentimentLabel())).count();

        List<Map<String, Object>> sentimentStats = List.of(
                Map.of("label", "POS", "count", posCount),
                Map.of("label", "NEG", "count", negCount),
                Map.of("label", "NEU", "count", neuCount)
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("TotalAnalyses", totalAnalyses);
        response.put("HighRiskCount", highRiskCount);
        response.put("PendingReviews", pendingReviews);
        response.put("TotalReviews", totalReviews);
        response.put("SentimentStats", sentimentStats);
        response.put("sentimentStats", sentimentStats);
        response.put("totalAnalyses", totalAnalyses);
        response.put("highRiskCount", highRiskCount);
        response.put("pendingReviews", pendingReviews);
        response.put("totalReviews", totalReviews);
        
        List<CourseMlAnalysis> history = courseMlAnalysisRepository.findTop20ByOrderByCreatedAtDesc();
        List<Map<String, Object>> analysesHistory = history.stream().map(a -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId());
            m.put("courseId", a.getCourseId());
            m.put("courseTitle", a.getCourse() != null ? a.getCourse().getTitle() : null);
            m.put("primaryCategory", a.getPrimaryCategory());
            m.put("confidence", a.getConfidence());
            m.put("qualityScore", a.getQualityScore());
            m.put("riskLevel", a.getRiskLevel());
            m.put("status", a.getStatus());
            m.put("createdAt", a.getCreatedAt());
            return m;
        }).collect(Collectors.toList());
        
        response.put("AnalysesHistory", analysesHistory);
        response.put("analysesHistory", analysesHistory);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/ml-analyses/{id}/approve")
    public ResponseEntity<?> approveMlAnalysis(@PathVariable Integer id) {
        CourseMlAnalysis analysis = courseMlAnalysisRepository.findById(id).orElse(null);
        if (analysis == null) return ResponseEntity.notFound().build();

        analysis.setStatus("Approved");
        analysis.setApprovedAt(LocalDateTime.now());

        try {
            Integer adminId = Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());
            analysis.setApprovedByUserId(adminId);
        } catch(Exception ignored) {}

        if (analysis.getCourse() != null) {
            analysis.getCourse().setStatus("PendingApproval");
        }
        
        courseMlAnalysisRepository.save(analysis);

        Map<String, String> response = new HashMap<>();
        response.put("message", "ML classification approved.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/ml-analyses/{id}/override")
    public ResponseEntity<?> overrideMlAnalysis(@PathVariable Integer id, @RequestBody OverrideMlDto dto) {
        CourseMlAnalysis analysis = courseMlAnalysisRepository.findById(id).orElse(null);
        if (analysis == null) return ResponseEntity.notFound().build();

        analysis.setStatus("Overridden");
        analysis.setApprovedAt(LocalDateTime.now());

        try {
            Integer adminId = Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());
            analysis.setApprovedByUserId(adminId);
        } catch(Exception ignored) {}

        Category cat = categoryRepository.findByName(dto.getCategoryName());
        if (cat != null && analysis.getCourse() != null) {
            analysis.getCourse().setCategoryId(cat.getCategoryId());
        }

        if (analysis.getCourse() != null) {
            analysis.getCourse().setStatus("PendingApproval");
        }
        
        courseMlAnalysisRepository.save(analysis);

        Map<String, String> response = new HashMap<>();
        response.put("message", "ML classification overridden and course updated.");
        return ResponseEntity.ok(response);
    }
}

class OverrideMlDto {
    private String categoryName = "";
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}

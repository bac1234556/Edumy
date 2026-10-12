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

@RestController
@RequestMapping("/api/Admin")
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

    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalCourses = courseRepository.count();
        
        List<Order> completedOrders = orderRepository.findByStatus("Completed");
        BigDecimal totalRevenue = completedOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> response = new HashMap<>();
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
    public ResponseEntity<?> updateCourseStatus(@PathVariable Integer id, @RequestBody String status) {
        Course course = courseRepository.findById(id).orElse(null);
        if (course == null) return ResponseEntity.notFound().build();

        course.setStatus(status.replace("\"", ""));
        courseRepository.save(course);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Status updated");
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
        
        Map<String, Object> response = new HashMap<>();
        response.put("TotalAnalyses", totalAnalyses);
        response.put("HighRiskCount", highRiskCount);
        response.put("PendingReviews", pendingReviews);
        response.put("TotalReviews", totalReviews);
        
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

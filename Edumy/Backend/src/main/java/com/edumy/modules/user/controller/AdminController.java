package com.edumy.modules.user.controller;

import com.edumy.modules.course.model.Category;
import com.edumy.modules.course.repository.CategoryRepository;
import com.edumy.modules.course.model.Course;
import com.edumy.modules.ml.model.CourseMlAnalysis;
import com.edumy.modules.ml.repository.CourseMlAnalysisRepository;
import com.edumy.modules.course.repository.CourseRepository;
import com.edumy.modules.order.model.Order;
import com.edumy.modules.order.model.OrderItem;
import com.edumy.modules.order.repository.OrderRepository;
import com.edumy.modules.review.repository.ReviewRepository;
import com.edumy.modules.user.model.User;
import com.edumy.modules.user.repository.UserActivityRepository;
import com.edumy.modules.user.repository.UserRepository;

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
@RequestMapping({"/api/v1/admin", "/api/Admin", "/api/admin"})
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

    // =========================================================================
    // QUẢN LÝ ĐƠN HÀNG TOÀN SÀN CHO ADMIN (GET /api/v1/admin/orders)
    // =========================================================================

    @GetMapping({"/orders", "/v1/orders"})
    public ResponseEntity<?> getAllOrders() {
        List<Order> allOrders = orderRepository.findAll().stream()
                .sorted(Comparator.comparing(Order::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        List<Map<String, Object>> response = allOrders.stream().map(order -> {
            Map<String, Object> m = new HashMap<>();
            m.put("orderId", order.getOrderId());
            m.put("orderCode", "EDUMY" + order.getOrderId());
            m.put("userId", order.getUserId());
            m.put("customerName", order.getUser() != null ? order.getUser().getFullName() : "Khách hàng #" + order.getUserId());
            m.put("customerEmail", order.getUser() != null ? order.getUser().getEmail() : "N/A");
            m.put("totalAmount", order.getTotalAmount());
            
            String status = order.getStatus() != null ? order.getStatus().toUpperCase() : "PENDING";
            if ("COMPLETED".equalsIgnoreCase(status)) {
                status = "PAID";
            }
            m.put("status", status);
            m.put("createdAt", order.getCreatedAt());

            List<Map<String, Object>> courseList = new ArrayList<>();
            if (order.getOrderItems() != null) {
                for (OrderItem item : order.getOrderItems()) {
                    Map<String, Object> cMap = new HashMap<>();
                    cMap.put("courseId", item.getCourseId());
                    cMap.put("title", (item.getCourse() != null && item.getCourse().getTitle() != null)
                            ? item.getCourse().getTitle()
                            : ("Khóa học #" + item.getCourseId()));
                    cMap.put("price", item.getPrice());
                    courseList.add(cMap);
                }
            }
            m.put("courses", courseList);

            // Mã tham chiếu ngân hàng VietQR / LPBank / SePay
            int refSuffix = Math.abs((order.getOrderId() != null ? order.getOrderId() : 1) * 31) % 9000 + 1000;
            String bankRef = "LPB" + order.getOrderId() + refSuffix;
            if ("PAID".equalsIgnoreCase(status)) {
                m.put("bankTransactionRef", "SEPAY-" + bankRef);
            } else {
                m.put("bankTransactionRef", "EDUMY " + order.getOrderId());
            }

            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // QUẢN LÝ DANH MỤC CHO ADMIN (CATEGORIES & STATS)
    // =========================================================================

    @GetMapping({"/categories/stats", "/v1/categories/stats"})
    public ResponseEntity<?> getCategoriesStats() {
        List<Category> categories = categoryRepository.findAll().stream()
                .sorted(Comparator.comparing(Category::getCategoryId))
                .collect(Collectors.toList());

        List<Map<String, Object>> statsList = categories.stream().map(cat -> {
            Map<String, Object> m = new HashMap<>();
            m.put("categoryId", cat.getCategoryId());
            m.put("id", cat.getCategoryId());
            m.put("name", cat.getName());
            m.put("description", cat.getDescription());
            m.put("parentCategoryId", cat.getParentCategoryId());
            
            // Tính số lượng khóa học liên kết thực tế
            long coursesCount = courseRepository.countByCategoryId(cat.getCategoryId());
            m.put("coursesCount", coursesCount);

            // Icon gợi ý thông minh
            String icon = getCategoryIcon(cat.getName());
            m.put("icon", icon);

            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(statsList);
    }

    @PostMapping({"/categories", "/v1/categories"})
    public ResponseEntity<?> createCategory(@RequestBody Category category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Tên danh mục không được để trống."));
        }

        category.setName(category.getName().trim());
        Category saved = categoryRepository.save(category);
        return ResponseEntity.ok(saved);
    }

    @PutMapping({"/categories/{id}", "/v1/categories/{id}"})
    public ResponseEntity<?> updateCategory(@PathVariable Integer id, @RequestBody Category category) {
        Category existing = categoryRepository.findById(id).orElse(null);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        if (category.getName() == null || category.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Tên danh mục không được để trống."));
        }

        existing.setName(category.getName().trim());
        existing.setDescription(category.getDescription());
        existing.setParentCategoryId(category.getParentCategoryId());

        Category updated = categoryRepository.save(existing);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping({"/categories/{id}", "/v1/categories/{id}"})
    public ResponseEntity<?> deleteCategory(@PathVariable Integer id) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category == null) {
            return ResponseEntity.notFound().build();
        }

        // CHẶN XÓA NẾU DANH MỤC ĐANG CÓ KHÓA HỌC
        long coursesCount = courseRepository.countByCategoryId(id);
        if (coursesCount > 0) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "error", "Bad Request",
                    "message", "Không thể xóa danh mục '" + category.getName() + "' vì đang có " + coursesCount + " khóa học liên kết. Vui lòng chuyển các khóa học sang danh mục khác trước!"
            ));
        }

        categoryRepository.deleteById(id);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "Đã xóa danh mục '" + category.getName() + "' thành công.",
                "categoryId", id
        ));
    }

    private String getCategoryIcon(String name) {
        if (name == null) return "bi-folder2";
        String lower = name.toLowerCase();
        if (lower.contains("dev") || lower.contains("code") || lower.contains("lập trình")) return "bi-code-slash";
        if (lower.contains("business") || lower.contains("kinh doanh")) return "bi-briefcase";
        if (lower.contains("design") || lower.contains("thiết kế")) return "bi-palette";
        if (lower.contains("marketing")) return "bi-megaphone";
        if (lower.contains("it") || lower.contains("software") || lower.contains("phần mềm")) return "bi-laptop";
        if (lower.contains("productivity") || lower.contains("văn phòng")) return "bi-kanban";
        if (lower.contains("personal") || lower.contains("phát triển bản thân")) return "bi-person-badge";
        if (lower.contains("photo") || lower.contains("nhiếp ảnh")) return "bi-camera";
        if (lower.contains("music") || lower.contains("âm nhạc")) return "bi-music-note-beamed";
        if (lower.contains("finance") || lower.contains("tài chính")) return "bi-cash-coin";
        return "bi-folder-fill";
    }

    // =========================================================================
    // ML MONITORING & GOVERNANCE
    // =========================================================================

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

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
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/Payment")
@PreAuthorize("isAuthenticated()")
public class PaymentController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private CartItemRepository cartItemRepository;

    private Integer getCurrentUserId() {
        try {
            return Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());
        } catch (Exception e) {
            return null;
        }
    }

    @PostMapping("/callback")
    public ResponseEntity<?> paymentCallback(@RequestBody PaymentResultDto result) {
        Integer userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();

        Order order = orderRepository.findById(result.getOrderId()).orElse(null);

        if (order == null || !userId.equals(order.getUserId())) return ResponseEntity.status(404).body("Order not found");

        if (!"Pending".equals(order.getStatus())) {
            return ResponseEntity.badRequest().body("Order already processed. Current status: " + order.getStatus());
        }

        if (result.isSuccess()) {
            order.setStatus("Completed");

            if (order.getOrderItems() != null) {
                for (OrderItem item : order.getOrderItems()) {
                    boolean existingEnrollment = enrollmentRepository.existsByUserIdAndCourseId(userId, item.getCourseId());
                    
                    if (!existingEnrollment) {
                        Enrollment enrollment = new Enrollment();
                        enrollment.setUserId(userId);
                        enrollment.setCourseId(item.getCourseId());
                        enrollment.setEnrolledAt(LocalDateTime.now());
                        enrollmentRepository.save(enrollment);
                    }
                    
                    Course course = item.getCourse();
                    if (course != null) {
                        course.setStudentCount(course.getStudentCount() + 1);
                        courseRepository.save(course);
                    }
                }
            }

            Cart cart = cartRepository.findByUserId(userId);

            if (cart != null && cart.getCartItems() != null && !cart.getCartItems().isEmpty()) {
                cartItemRepository.deleteAll(cart.getCartItems());
            }
        } else {
            order.setStatus("Cancelled");
        }

        orderRepository.save(order);

        return ResponseEntity.ok(Map.of(
            "message", result.isSuccess() ? "Payment successful" : "Payment cancelled",
            "status", order.getStatus()
        ));
    }
}

class PaymentResultDto {
    private Integer orderId;
    private boolean success;

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}

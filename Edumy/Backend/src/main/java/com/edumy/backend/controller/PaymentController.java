package com.edumy.backend.controller;

import com.edumy.backend.model.*;
import com.edumy.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import lombok.Data;

@RestController
@RequestMapping("/api/Payment")
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

    /**
     * Client-initiated callback from Mock Gateway / Stripe Redirect
     */
    @PostMapping("/callback")
    public ResponseEntity<?> paymentCallback(@RequestBody PaymentResultDto result) {
        Integer userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();

        Order order = orderRepository.findById(result.getOrderId()).orElse(null);
        if (order == null || !userId.equals(order.getUserId())) {
            return ResponseEntity.status(404).body("Order not found");
        }

        if (!"Pending".equals(order.getStatus())) {
            return ResponseEntity.ok(Map.of(
                "message", "Order already processed.",
                "status", order.getStatus()
            ));
        }

        processOrderCompletion(order, result.isSuccess());

        return ResponseEntity.ok(Map.of(
            "message", result.isSuccess() ? "Payment successful" : "Payment cancelled",
            "status", order.getStatus(),
            "orderId", order.getOrderId()
        ));
    }

    /**
     * Server-to-server IPN / Webhook endpoint from payment provider
     */
    @PostMapping("/webhook")
    public ResponseEntity<?> handleIpnWebhook(@RequestBody WebhookPayloadDto payload,
                                              @RequestHeader(value = "X-Webhook-Signature", required = false) String signature) {
        if (payload == null || payload.getOrderId() == null) {
            return ResponseEntity.badRequest().body("Invalid webhook payload");
        }

        Order order = orderRepository.findById(payload.getOrderId()).orElse(null);
        if (order == null) {
            return ResponseEntity.status(404).body("Order not found");
        }

        // Idempotent processing: if already completed, acknowledge success to gateway immediately
        if ("Completed".equals(order.getStatus())) {
            return ResponseEntity.ok(Map.of("status", "ALREADY_COMPLETED", "message", "Order previously processed"));
        }

        boolean success = "SUCCESS".equalsIgnoreCase(payload.getTransactionStatus()) || 
                          "COMPLETED".equalsIgnoreCase(payload.getTransactionStatus()) ||
                          payload.isSuccess();

        processOrderCompletion(order, success);

        return ResponseEntity.ok(Map.of(
            "status", "PROCESSED",
            "orderId", order.getOrderId(),
            "orderStatus", order.getStatus()
        ));
    }

    private synchronized void processOrderCompletion(Order order, boolean isSuccess) {
        if (isSuccess) {
            order.setStatus("Completed");
            Integer orderUserId = order.getUserId();

            if (order.getOrderItems() != null) {
                for (OrderItem item : order.getOrderItems()) {
                    boolean existingEnrollment = enrollmentRepository.existsByUserIdAndCourseId(orderUserId, item.getCourseId());
                    
                    if (!existingEnrollment) {
                        Enrollment enrollment = new Enrollment();
                        enrollment.setUserId(orderUserId);
                        enrollment.setCourseId(item.getCourseId());
                        enrollment.setEnrolledAt(LocalDateTime.now());
                        enrollment.setProgressPercentage(0);
                        enrollmentRepository.save(enrollment);
                    }
                    
                    Course course = courseRepository.findById(item.getCourseId()).orElse(null);
                    if (course != null) {
                        course.setStudentCount(course.getStudentCount() + 1);
                        courseRepository.save(course);
                    }
                }
            }

            Cart cart = cartRepository.findByUserId(orderUserId);
            if (cart != null && cart.getCartItems() != null && !cart.getCartItems().isEmpty()) {
                cartItemRepository.deleteAll(cart.getCartItems());
            }
        } else {
            order.setStatus("Cancelled");
        }

        orderRepository.save(order);
    }
}

@Data
class PaymentResultDto {
    private Integer orderId;
    private boolean success;
}

@Data
class WebhookPayloadDto {
    private Integer orderId;
    private String transactionId;
    private String transactionStatus;
    private boolean success;
    private Double amount;
}

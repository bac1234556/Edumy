package com.edumy.backend.controller;

import com.edumy.backend.dto.*;
import com.edumy.backend.model.Order;
import com.edumy.backend.repository.OrderRepository;
import com.edumy.backend.service.OrderService;
import com.edumy.backend.service.PaymentService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/v1/orders", "/api/orders", "/api/Orders"})
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class OrdersController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;

    private Integer getCurrentUserId() {
        try {
            return Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * POST /api/v1/orders/buy-now
     * Luồng MUA NGAY (Direct Buy Now / Single Item Checkout) - KHÔNG ĐƯỢC CHẠM VÀO GIỎ HÀNG
     */
    @PostMapping({"/buy-now", "/buynow"})
    public ResponseEntity<?> createBuyNowOrder(@Valid @RequestBody BuyNowRequest request) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Yêu cầu đăng nhập"));
        }

        try {
            OrderResponseDto response = orderService.createBuyNowOrder(userId, request);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(404).body(Map.of("message", ex.getMessage()));
        } catch (IllegalStateException | IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Lỗi tạo đơn hàng Mua ngay: " + ex.getMessage()));
        }
    }

    /**
     * POST /api/v1/orders/checkout
     * Luồng THANH TOÁN TẤT CẢ GIỎ HÀNG
     */
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody(required = false) CheckoutRequest request) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Yêu cầu đăng nhập"));
        }

        try {
            String couponCode = request != null ? request.getCouponCode() : null;
            Map<String, Object> response = orderService.checkoutCart(userId, couponCode);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException | IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Lỗi thanh toán giỏ hàng: " + ex.getMessage()));
        }
    }

    /**
     * GET /api/v1/orders/{orderId}/status
     * Polling trạng thái đơn hàng thời gian thực cho giao diện thanh toán QR
     */
    @GetMapping({"/api/v1/orders/{orderId}/status", "/{orderId}/status", "/status/{orderId}"})
    public ResponseEntity<?> getOrderStatus(@PathVariable Integer orderId) {
        try {
            Integer currentUserId = getCurrentUserId();
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                return ResponseEntity.status(404).body(Map.of("message", "Không tìm thấy đơn hàng #" + orderId));
            }
            if (currentUserId != null && !currentUserId.equals(order.getUserId())) {
                boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_Admin") || a.getAuthority().equalsIgnoreCase("Admin"));
                if (!isAdmin) {
                    return ResponseEntity.status(403).body(Map.of("message", "Bạn không có quyền xem thông tin đơn hàng này."));
                }
            }

            OrderStatusResponseDto status = paymentService.getOrderStatus(orderId);
            return ResponseEntity.ok(status);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(404).body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Lỗi kiểm tra trạng thái đơn hàng: " + ex.getMessage()));
        }
    }

    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders() {
        Integer userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();

        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> userId.equals(o.getUserId()))
                .sorted(java.util.Comparator.comparing(Order::getCreatedAt).reversed())
                .collect(Collectors.toList());

        return ResponseEntity.ok(orders);
    }
}

@Data
class CheckoutRequest {
    private String couponCode;
}

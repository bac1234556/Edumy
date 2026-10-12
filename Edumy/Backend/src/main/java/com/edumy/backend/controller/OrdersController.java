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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import lombok.Data;

@RestController
@RequestMapping("/api/Orders")
@PreAuthorize("isAuthenticated()")
public class OrdersController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CouponRepository couponRepository;

    private Integer getCurrentUserId() {
        try {
            return Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());
        } catch (Exception e) {
            return null;
        }
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequest request) {
        Integer userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();

        Cart cart = cartRepository.findByUserId(userId);

        if (cart == null || cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            return ResponseEntity.badRequest().body("Your cart is empty.");
        }

        BigDecimal totalAmount = cart.getCartItems().stream()
                .map(ci -> ci.getCourse().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (request != null && request.getCouponCode() != null && !request.getCouponCode().trim().isEmpty()) {
            Coupon coupon = couponRepository.findByCodeIgnoreCase(request.getCouponCode());
            if (coupon != null && coupon.getIsActive() && coupon.getExpiryDate().isAfter(LocalDateTime.now())) {
                BigDecimal discountPercentage = coupon.getDiscountPercentage();
                BigDecimal discountAmount = totalAmount.multiply(discountPercentage).divide(BigDecimal.valueOf(100));
                totalAmount = totalAmount.subtract(discountAmount);
            }
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus("Pending");
        order.setCreatedAt(LocalDateTime.now());
        
        List<OrderItem> items = cart.getCartItems().stream().map(ci -> {
            OrderItem oi = new OrderItem();
            oi.setCourseId(ci.getCourseId());
            oi.setPrice(ci.getCourse().getPrice());
            oi.setOrder(order);
            return oi;
        }).collect(Collectors.toList());
        
        order.setOrderItems(items);
        
        Order savedOrder = orderRepository.save(order);

        String paymentUrl = "/payment?orderId=" + savedOrder.getOrderId() + "&amount=" + totalAmount;

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Order created. Redirecting to payment gateway...");
        response.put("orderId", savedOrder.getOrderId());
        response.put("paymentUrl", paymentUrl);

        return ResponseEntity.ok(response);
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

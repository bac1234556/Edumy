package com.edumy.backend.controller;

import com.edumy.backend.model.*;
import com.edumy.backend.dto.*;
import com.edumy.backend.repository.*;
import com.edumy.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import lombok.Data;

@RestController
@RequestMapping({"/api/Orders", "/api/orders"})
@PreAuthorize("isAuthenticated()")
public class OrdersController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CourseCouponRepository courseCouponRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private PaymentService paymentService;


    private Integer getCurrentUserId() {
        try {
            return Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());
        } catch (Exception e) {
            return null;
        }
    }

    @PostMapping("/checkout")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<?> checkout(@RequestBody(required = false) CheckoutRequest request) {
        Integer userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();

        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null || cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Giỏ hàng của bạn đang trống."));
        }

        // Lấy danh sách khóa học học viên đã sở hữu để loại trừ
        List<Enrollment> userEnrollments = enrollmentRepository.findByUserId(userId);
        Set<Integer> activeEnrolledCourseIds = userEnrollments.stream()
                .filter(e -> e.getStatus() == null || "ACTIVE".equalsIgnoreCase(e.getStatus()) || "Completed".equalsIgnoreCase(e.getStatus()))
                .map(Enrollment::getCourseId)
                .collect(Collectors.toSet());

        List<CartItem> payableCartItems = cart.getCartItems().stream()
                .filter(ci -> !activeEnrolledCourseIds.contains(ci.getCourseId()))
                .collect(Collectors.toList());

        if (payableCartItems.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Tất cả khóa học trong giỏ hàng của bạn đã được thanh toán thành công."));
        }

        // Tính toán số tiền sau khi áp dụng mã giảm giá (chỉ áp dụng cho các khóa học chưa thanh toán)
        BigDecimal totalAmount = BigDecimal.ZERO;
        Coupon appliedCoupon = null;

        if (request != null && request.getCouponCode() != null && !request.getCouponCode().trim().isEmpty()) {
            Coupon coupon = couponRepository.findByCodeIgnoreCase(request.getCouponCode().trim());
            if (coupon != null && Boolean.TRUE.equals(coupon.getIsActive()) && coupon.getExpiryDate().isAfter(LocalDateTime.now())) {
                appliedCoupon = coupon;
            }
        }

        for (CartItem ci : payableCartItems) {
            BigDecimal itemPrice = (ci.getCourse() != null && ci.getCourse().getPrice() != null)
                    ? ci.getCourse().getPrice()
                    : BigDecimal.ZERO;

            if (appliedCoupon != null) {
                boolean hasCourseRestrictions = courseCouponRepository.existsByCouponId(appliedCoupon.getId());
                boolean isEligibleForCoupon = !hasCourseRestrictions || 
                        courseCouponRepository.existsByCouponIdAndCourseId(appliedCoupon.getId(), ci.getCourseId());

                if (isEligibleForCoupon) {
                    BigDecimal discountPercentage = appliedCoupon.getDiscountPercentage();
                    BigDecimal discountAmount = itemPrice.multiply(discountPercentage).divide(BigDecimal.valueOf(100));
                    itemPrice = itemPrice.subtract(discountAmount);
                    if (itemPrice.compareTo(BigDecimal.ZERO) < 0) {
                        itemPrice = BigDecimal.ZERO;
                    }
                }
            }
            totalAmount = totalAmount.add(itemPrice);
        }

        // Tạo đơn hàng
        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setCreatedAt(LocalDateTime.now());

        List<OrderItem> items = payableCartItems.stream().map(ci -> {
            OrderItem oi = new OrderItem();
            oi.setCourseId(ci.getCourseId());
            oi.setPrice(ci.getCourse() != null && ci.getCourse().getPrice() != null ? ci.getCourse().getPrice() : BigDecimal.ZERO);
            oi.setOrder(order);
            return oi;
        }).collect(Collectors.toList());

        order.setOrderItems(items);

        // VÁ LỖ HỔNG: Nếu đơn hàng có giá trị 0đ (khóa học miễn phí hoặc giảm 100%) -> Tự động kích hoạt PAID & Enrollments
        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            order.setStatus("PAID");
            Order savedOrder = orderRepository.save(order);

            for (CartItem ci : cart.getCartItems()) {
                Integer courseId = ci.getCourseId();
                boolean exists = enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
                if (!exists) {
                    Enrollment enrollment = Enrollment.builder()
                            .userId(userId)
                            .courseId(courseId)
                            .enrolledAt(LocalDateTime.now())
                            .progressPercentage(0)
                            .status("ACTIVE")
                            .build();
                    enrollmentRepository.save(enrollment);
                } else {
                    Enrollment existing = enrollmentRepository.findByUserIdAndCourseId(userId, courseId);
                    if (existing != null && !"ACTIVE".equalsIgnoreCase(existing.getStatus())) {
                        existing.setStatus("ACTIVE");
                        enrollmentRepository.save(existing);
                    }
                }

                Course course = courseRepository.findById(courseId).orElse(null);
                if (course != null) {
                    int currentCount = course.getStudentCount() != null ? course.getStudentCount() : 0;
                    course.setStudentCount(currentCount + 1);
                    courseRepository.save(course);
                }
            }

            // Dọn dẹp giỏ hàng
            cartItemRepository.deleteByCartId(cart.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đăng ký khóa học thành công! (Khóa học miễn phí)");
            response.put("orderId", savedOrder.getOrderId());
            response.put("isFree", true);
            response.put("status", "PAID");
            return ResponseEntity.ok(response);
        }

        // Nếu đơn hàng có phí -> Lưu trạng thái Pending và sinh thông tin VietQR
        order.setStatus("Pending");
        Order savedOrder = orderRepository.save(order);

        String paymentUrl = "/payment?orderId=" + savedOrder.getOrderId() + "&amount=" + totalAmount;

        VietQrGenerateResponseDto qrData = null;
        try {
            qrData = paymentService.generateVietQrPayment(savedOrder.getOrderId(), userId);
        } catch (Exception ignored) {}

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Đơn hàng đã được tạo. Vui lòng quét mã VietQR để thanh toán.");
        response.put("orderId", savedOrder.getOrderId());
        response.put("paymentUrl", paymentUrl);
        response.put("isFree", false);
        response.put("status", "Pending");
        response.put("amount", totalAmount);
        if (qrData != null) {
            response.put("qrImageUrl", qrData.getQrImageUrl());
            response.put("transferContent", qrData.getTransferContent());
            response.put("accountNo", qrData.getAccountNo());
            response.put("accountName", qrData.getAccountName());
            response.put("bankName", qrData.getBankName());
            response.put("bankId", qrData.getBankId());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/orders/{orderId}/status
     * GET /api/orders/{orderId}/status
     * Endpoint polling trạng thái đơn hàng thời gian thực cho giao diện thanh toán QR
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
        } catch (java.util.NoSuchElementException ex) {
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

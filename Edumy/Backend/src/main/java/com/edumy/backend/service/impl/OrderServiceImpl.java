package com.edumy.backend.service.impl;

import com.edumy.backend.dto.BuyNowRequest;
import com.edumy.backend.dto.OrderResponseDto;
import com.edumy.backend.dto.VietQrGenerateResponseDto;
import com.edumy.backend.model.*;
import com.edumy.backend.repository.*;
import com.edumy.backend.service.OrderService;
import com.edumy.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CourseRepository courseRepository;
    private final CouponRepository couponRepository;
    private final CourseCouponRepository courseCouponRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final PaymentService paymentService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponseDto createBuyNowOrder(Integer userId, BuyNowRequest request) {
        if (userId == null) {
            throw new IllegalArgumentException("Yêu cầu thông tin học viên để mua khóa học.");
        }
        if (request == null || request.getCourseId() == null) {
            throw new IllegalArgumentException("Mã khóa học (courseId) không được để trống.");
        }

        Integer courseId = request.getCourseId();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy khóa học với ID: " + courseId));

        if (!"Published".equalsIgnoreCase(course.getStatus())) {
            throw new IllegalStateException("Khóa học hiện chưa được phát hành để đăng ký.");
        }

        // 1. Kiểm tra nếu học viên đã sở hữu khóa học này rồi (ACTIVE hoặc Completed)
        boolean alreadyEnrolled = enrollmentRepository.isUserEnrolledActive(userId, courseId);
        if (alreadyEnrolled) {
            throw new IllegalStateException("Bạn đã sở hữu khóa học này rồi. Vui lòng vào phòng học để tiếp tục.");
        }

        // 2. Tính giá duy nhất cho khóa học này
        BigDecimal itemPrice = course.getPrice() != null ? course.getPrice() : BigDecimal.ZERO;

        // Áp dụng mã giảm giá (nếu có)
        if (request.getCouponCode() != null && !request.getCouponCode().trim().isEmpty()) {
            Coupon coupon = couponRepository.findByCodeIgnoreCase(request.getCouponCode().trim());
            if (coupon != null && Boolean.TRUE.equals(coupon.getIsActive()) && coupon.getExpiryDate().isAfter(LocalDateTime.now())) {
                boolean hasCourseRestrictions = courseCouponRepository.existsByCouponId(coupon.getId());
                boolean isEligible = !hasCourseRestrictions ||
                        courseCouponRepository.existsByCouponIdAndCourseId(coupon.getId(), courseId);

                if (isEligible) {
                    BigDecimal discountPercentage = coupon.getDiscountPercentage();
                    BigDecimal discountAmount = itemPrice.multiply(discountPercentage).divide(BigDecimal.valueOf(100));
                    itemPrice = itemPrice.subtract(discountAmount);
                    if (itemPrice.compareTo(BigDecimal.ZERO) < 0) {
                        itemPrice = BigDecimal.ZERO;
                    }
                }
            }
        }

        // 3. Khởi tạo đơn hàng Mua Ngay ĐỘC LẬP (KHÔNG liên quan hay ảnh hưởng đến giỏ hàng)
        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(itemPrice);
        order.setCreatedAt(LocalDateTime.now());

        OrderItem orderItem = new OrderItem();
        orderItem.setCourseId(courseId);
        orderItem.setPrice(course.getPrice() != null ? course.getPrice() : BigDecimal.ZERO);
        orderItem.setOrder(order);

        order.setOrderItems(Collections.singletonList(orderItem));

        // 4. Trường hợp khóa học miễn phí (0đ) -> Kích hoạt ngay lập tức
        if (itemPrice.compareTo(BigDecimal.ZERO) <= 0) {
            order.setStatus("PAID");
            Order savedOrder = orderRepository.save(order);

            // Ghi danh học viên
            Enrollment enrollment = Enrollment.builder()
                    .userId(userId)
                    .courseId(courseId)
                    .enrolledAt(LocalDateTime.now())
                    .progressPercentage(0)
                    .status("ACTIVE")
                    .build();
            enrollmentRepository.save(enrollment);

            int currentCount = course.getStudentCount() != null ? course.getStudentCount() : 0;
            course.setStudentCount(currentCount + 1);
            courseRepository.save(course);

            // Nếu khóa học này tình cờ đang có trong giỏ hàng thì loại bỏ duy nhất khóa này, giữ nguyên các khóa khác
            cleanUpCartForCourse(userId, courseId);

            return OrderResponseDto.builder()
                    .orderId(savedOrder.getOrderId())
                    .courseId(courseId)
                    .courseTitle(course.getTitle())
                    .amount(BigDecimal.ZERO)
                    .isFree(true)
                    .status("PAID")
                    .message("Đăng ký khóa học thành công! (Khóa học miễn phí)")
                    .build();
        }

        // 5. Trường hợp khóa học có phí -> Tạo đơn hàng trạng thái Pending và sinh thông tin VietQR
        order.setStatus("Pending");
        Order savedOrder = orderRepository.save(order);

        String paymentUrl = "/payment?orderId=" + savedOrder.getOrderId() + "&amount=" + itemPrice;

        VietQrGenerateResponseDto qrData = null;
        try {
            qrData = paymentService.generateVietQrPayment(savedOrder.getOrderId(), userId);
        } catch (Exception ignored) {}

        OrderResponseDto.OrderResponseDtoBuilder builder = OrderResponseDto.builder()
                .orderId(savedOrder.getOrderId())
                .courseId(courseId)
                .courseTitle(course.getTitle())
                .amount(itemPrice)
                .isFree(false)
                .status("Pending")
                .paymentUrl(paymentUrl)
                .message("Đơn hàng Mua ngay đã được khởi tạo thành công.");

        if (qrData != null) {
            builder.qrImageUrl(qrData.getQrImageUrl())
                    .transferContent(qrData.getTransferContent())
                    .accountNo(qrData.getAccountNo())
                    .accountName(qrData.getAccountName())
                    .bankName(qrData.getBankName())
                    .bankId(qrData.getBankId());
        }

        return builder.build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> checkoutCart(Integer userId, String couponCode) {
        if (userId == null) {
            throw new IllegalArgumentException("Yêu cầu thông tin học viên để thanh toán.");
        }

        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null || cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Giỏ hàng của bạn đang trống.");
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
            throw new IllegalStateException("Tất cả khóa học trong giỏ hàng của bạn đã được thanh toán thành công.");
        }

        // Tính toán số tiền sau khi áp dụng mã giảm giá
        BigDecimal totalAmount = BigDecimal.ZERO;
        Coupon appliedCoupon = null;

        if (couponCode != null && !couponCode.trim().isEmpty()) {
            Coupon coupon = couponRepository.findByCodeIgnoreCase(couponCode.trim());
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

        // Khóa học 0đ
        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            order.setStatus("PAID");
            Order savedOrder = orderRepository.save(order);

            for (CartItem ci : payableCartItems) {
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
                }

                Course course = courseRepository.findById(courseId).orElse(null);
                if (course != null) {
                    int currentCount = course.getStudentCount() != null ? course.getStudentCount() : 0;
                    course.setStudentCount(currentCount + 1);
                    courseRepository.save(course);
                }
            }

            // Dọn dẹp các món đã thanh toán trong giỏ
            for (CartItem ci : payableCartItems) {
                cleanUpCartForCourse(userId, ci.getCourseId());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đăng ký khóa học thành công! (Khóa học miễn phí)");
            response.put("orderId", savedOrder.getOrderId());
            response.put("isFree", true);
            response.put("status", "PAID");
            return response;
        }

        // Đơn hàng có phí
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

        return response;
    }

    private void cleanUpCartForCourse(Integer userId, Integer courseId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart != null && cart.getCartItems() != null) {
            cartItemRepository.deleteByCartIdAndCourseOrItemId(cart.getId(), courseId);
            cart.getCartItems().removeIf(ci -> ci.getCourseId().equals(courseId));
            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);
        }
    }
}

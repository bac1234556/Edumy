package com.edumy.backend.controller;

import com.edumy.backend.model.*;
import com.edumy.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import lombok.Data;

@RestController
@RequestMapping({"/api/Coupons", "/api/coupons"})
public class CouponsController {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CourseCouponRepository courseCouponRepository;

    @Autowired
    private CourseRepository courseRepository;

    @PostMapping("/validate")
    public ResponseEntity<?> validateCoupon(@RequestBody ValidateCouponRequest request) {
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mã giảm giá không được để trống."));
        }

        Coupon coupon = couponRepository.findByCodeIgnoreCase(request.getCode().trim());
        if (coupon == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Mã giảm giá không tồn tại."));
        }

        if (!Boolean.TRUE.equals(coupon.getIsActive())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mã giảm giá này hiện không khả dụng."));
        }

        if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mã giảm giá đã hết hạn sử dụng."));
        }

        // Kiểm tra ràng buộc áp dụng cho khóa học cụ thể
        if (request.getCourseId() != null) {
            boolean hasCourseRestrictions = courseCouponRepository.existsByCouponId(coupon.getId());
            if (hasCourseRestrictions && !courseCouponRepository.existsByCouponIdAndCourseId(coupon.getId(), request.getCourseId())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Mã giảm giá này không áp dụng cho khóa học đã chọn."));
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", coupon.getId());
        response.put("code", coupon.getCode());
        response.put("discountPercentage", coupon.getDiscountPercentage());
        response.put("message", "Áp dụng mã giảm giá thành công!");

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> getAllCoupons() {
        List<Coupon> coupons = couponRepository.findAll();
        return ResponseEntity.ok(coupons);
    }

    @PostMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> createCoupon(@RequestBody CreateCouponDto dto) {
        if (dto.getCode() == null || dto.getCode().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mã giảm giá không hợp lệ."));
        }

        Coupon existing = couponRepository.findByCodeIgnoreCase(dto.getCode().trim());
        if (existing != null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mã giảm giá này đã tồn tại."));
        }

        Coupon coupon = Coupon.builder()
                .code(dto.getCode().trim().toUpperCase())
                .discountPercentage(dto.getDiscountPercentage() != null ? dto.getDiscountPercentage() : BigDecimal.valueOf(10))
                .expiryDate(dto.getExpiryDate() != null ? dto.getExpiryDate() : LocalDateTime.now().plusMonths(1))
                .isActive(true)
                .build();

        Coupon saved = couponRepository.save(coupon);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{couponId}/assign-course/{courseId}")
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> assignCouponToCourse(@PathVariable Integer couponId, @PathVariable Integer courseId) {
        Coupon coupon = couponRepository.findById(couponId).orElse(null);
        if (coupon == null) return ResponseEntity.notFound().build();

        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) return ResponseEntity.notFound().build();

        boolean alreadyAssigned = courseCouponRepository.existsByCouponIdAndCourseId(couponId, courseId);
        if (!alreadyAssigned) {
            CourseCoupon cc = CourseCoupon.builder()
                    .couponId(couponId)
                    .courseId(courseId)
                    .build();
            courseCouponRepository.save(cc);
        }

        return ResponseEntity.ok(Map.of("message", "Gán mã giảm giá cho khóa học thành công."));
    }
}

@Data
class ValidateCouponRequest {
    private String code = "";
    private Integer courseId;
}

@Data
class CreateCouponDto {
    private String code;
    private BigDecimal discountPercentage;
    private LocalDateTime expiryDate;
}

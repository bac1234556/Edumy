package com.edumy.backend.controller;

import com.edumy.backend.model.*;
import com.edumy.backend.dto.*;
import com.edumy.backend.repository.*;
import com.edumy.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;
import lombok.Data;

@RestController
@RequestMapping("/api/Coupons")
public class CouponsController {

    @Autowired
    private CouponRepository couponRepository;

    @PostMapping("/validate")
    public ResponseEntity<?> validateCoupon(@RequestBody ValidateCouponRequest request) {
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Coupon code is required");
        }

        Coupon coupon = couponRepository.findByCodeIgnoreCase(request.getCode());

        if (coupon == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Invalid coupon code");
            return ResponseEntity.status(404).body(response);
        }

        if (!coupon.getIsActive()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "This coupon is no longer active");
            return ResponseEntity.badRequest().body(response);
        }

        if (coupon.getExpiryDate().isBefore(LocalDateTime.now())) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "This coupon has expired");
            return ResponseEntity.badRequest().body(response);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", coupon.getId());
        response.put("code", coupon.getCode());
        response.put("discountPercentage", coupon.getDiscountPercentage());
        response.put("message", "Coupon applied successfully!");

        return ResponseEntity.ok(response);
    }
}

@Data
class ValidateCouponRequest {
    private String code = "";
}

package com.edumy.backend.controller;

import com.edumy.backend.dto.CartResponseDto;
import com.edumy.backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping({"/api/v1/cart", "/api/Cart", "/api/cart"})
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    private Integer getCurrentUserId() {
        try {
            return Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());
        } catch (Exception e) {
            return null;
        }
    }

    @GetMapping
    public ResponseEntity<?> getCart() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Yêu cầu đăng nhập"));
        }

        CartResponseDto response = cartService.getCart(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping({"/add/{courseId}", "/items/{courseId}"})
    public ResponseEntity<?> addToCart(@PathVariable Integer courseId) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Yêu cầu đăng nhập"));
        }

        try {
            Map<String, String> res = cartService.addToCart(userId, courseId);
            return ResponseEntity.ok(res);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(404).body(Map.of("message", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Lỗi thêm khóa học vào giỏ: " + ex.getMessage()));
        }
    }

    @DeleteMapping({"/remove/{id}", "/items/{id}", "/courses/{id}"})
    public ResponseEntity<?> removeFromCart(@PathVariable Integer id) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Yêu cầu đăng nhập"));
        }

        try {
            Map<String, String> res = cartService.removeFromCart(userId, id);
            return ResponseEntity.ok(res);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Lỗi xóa khóa học khỏi giỏ: " + ex.getMessage()));
        }
    }
}

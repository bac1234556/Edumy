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
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping({"/api/Cart", "/api/cart"})
@PreAuthorize("isAuthenticated()")
public class CartController {

    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    private Integer getCurrentUserId() {
        try {
            return Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());
        } catch (Exception e) {
            return null;
        }
    }

    private Cart getOrCreateCart(Integer userId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            cart = new Cart();
            cart.setUserId(userId);
            cart = cartRepository.save(cart);
        }
        return cart;
    }

    @GetMapping
    public ResponseEntity<?> getCart() {
        Integer userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();

        Cart cart = getOrCreateCart(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", cart.getId());
        response.put("userId", cart.getUserId());
        response.put("totalItems", cart.getCartItems().size());
        
        BigDecimal totalPrice = cart.getCartItems().stream()
                .map(ci -> ci.getCourse().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.put("totalPrice", totalPrice);
        
        List<Map<String, Object>> items = cart.getCartItems().stream().map(ci -> {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("id", ci.getId());
            itemMap.put("courseId", ci.getCourseId());
            itemMap.put("addedAt", ci.getAddedAt());
            
            Map<String, Object> courseMap = new HashMap<>();
            courseMap.put("title", ci.getCourse().getTitle());
            courseMap.put("price", ci.getCourse().getPrice());
            courseMap.put("thumbnailUrl", ci.getCourse().getThumbnailUrl());
            courseMap.put("instructorName", ci.getCourse().getInstructor() != null ? ci.getCourse().getInstructor().getFullName() : null);
            
            itemMap.put("course", courseMap);
            return itemMap;
        }).collect(Collectors.toList());
        
        response.put("items", items);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/add/{courseId}")
    public ResponseEntity<?> addToCart(@PathVariable Integer courseId) {
        Integer userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();

        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null || !"Published".equals(course.getStatus())) {
            return ResponseEntity.status(404).body("Course not found or not published.");
        }

        boolean alreadyEnrolled = enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
        if (alreadyEnrolled) {
            return ResponseEntity.badRequest().body("You are already enrolled in this course.");
        }

        Cart cart = getOrCreateCart(userId);

        if (cart.getCartItems().stream().anyMatch(ci -> ci.getCourseId().equals(courseId))) {
            Map<String, String> res = new HashMap<>();
            res.put("message", "Course is already in your cart.");
            return ResponseEntity.ok(res);
        }

        CartItem cartItem = new CartItem();
        cartItem.setCartId(cart.getId());
        cartItem.setCourseId(courseId);
        cartItem.setAddedAt(LocalDateTime.now());
        
        cartItemRepository.save(cartItem);
        
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        Map<String, String> res = new HashMap<>();
        res.put("message", "Course added to cart successfully.");
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/remove/{courseId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Integer courseId) {
        Integer userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();

        Cart cart = getOrCreateCart(userId);
        CartItem item = cart.getCartItems().stream()
                .filter(ci -> ci.getCourseId().equals(courseId))
                .findFirst()
                .orElse(null);

        if (item != null) {
            cartItemRepository.delete(item);
            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);
        }

        Map<String, String> res = new HashMap<>();
        res.put("message", "Course removed from cart.");
        return ResponseEntity.ok(res);
    }
}

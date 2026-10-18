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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping({"/api/Wishlist", "/api/wishlist"})
@PreAuthorize("isAuthenticated()")
public class WishlistController {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private CourseRepository courseRepository;

    private Integer getCurrentUserId() {
        try {
            return Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());
        } catch (Exception e) {
            return null;
        }
    }

    @GetMapping
    public ResponseEntity<?> getMyWishlist() {
        Integer userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();

        List<Map<String, Object>> wishlist = wishlistRepository.findAll().stream()
                .filter(w -> userId.equals(w.getUserId()))
                .sorted(Comparator.comparing(Wishlist::getAddedAt).reversed())
                .map(w -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", w.getId());
                    map.put("courseId", w.getCourseId());
                    map.put("addedAt", w.getAddedAt());
                    
                    if (w.getCourse() != null) {
                        Map<String, Object> courseMap = new HashMap<>();
                        courseMap.put("title", w.getCourse().getTitle());
                        courseMap.put("thumbnailUrl", w.getCourse().getThumbnailUrl());
                        courseMap.put("price", w.getCourse().getPrice());
                        
                        if (w.getCourse().getInstructor() != null) {
                            courseMap.put("instructor", Map.of("fullName", w.getCourse().getInstructor().getFullName()));
                        }
                        map.put("course", courseMap);
                    }
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(wishlist);
    }

    @PostMapping("/add/{courseId}")
    public ResponseEntity<?> addToWishlist(@PathVariable Integer courseId) {
        Integer userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();

        boolean existing = wishlistRepository.findAll().stream().anyMatch(w -> userId.equals(w.getUserId()) && courseId.equals(w.getCourseId()));
        if (existing) return ResponseEntity.badRequest().body(Map.of("message", "Course is already in your wishlist"));

        if (!courseRepository.existsById(courseId)) {
            return ResponseEntity.status(404).body(Map.of("message", "Course not found"));
        }

        Wishlist w = new Wishlist();
        w.setUserId(userId);
        w.setCourseId(courseId);
        w.setAddedAt(LocalDateTime.now());
        wishlistRepository.save(w);

        return ResponseEntity.ok(Map.of("message", "Added to wishlist successfully"));
    }

    @DeleteMapping("/remove/{courseId}")
    public ResponseEntity<?> removeFromWishlist(@PathVariable Integer courseId) {
        Integer userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();

        Wishlist item = wishlistRepository.findAll().stream()
                .filter(w -> userId.equals(w.getUserId()) && courseId.equals(w.getCourseId()))
                .findFirst().orElse(null);

        if (item == null) return ResponseEntity.status(404).body(Map.of("message", "Course is not in your wishlist"));

        wishlistRepository.delete(item);

        return ResponseEntity.ok(Map.of("message", "Removed from wishlist"));
    }

    @GetMapping("/check/{courseId}")
    public ResponseEntity<?> checkWishlistStatus(@PathVariable Integer courseId) {
        Integer userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.ok(Map.of("inWishlist", false));

        boolean existing = wishlistRepository.findAll().stream().anyMatch(w -> userId.equals(w.getUserId()) && courseId.equals(w.getCourseId()));

        return ResponseEntity.ok(Map.of("inWishlist", existing));
    }
}

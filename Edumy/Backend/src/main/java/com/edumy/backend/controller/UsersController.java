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
import lombok.Data;

@RestController
@RequestMapping("/api/Users")
@PreAuthorize("isAuthenticated()")
public class UsersController {

    @Autowired
    private UserRepository userRepository;

    private Integer getCurrentUserId() {
        try {
            return Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());
        } catch (Exception e) {
            return null;
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        Integer userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        Map<String, Object> res = new HashMap<>();
        res.put("userId", user.getUserId());
        res.put("email", user.getEmail());
        res.put("fullName", user.getFullName());
        res.put("bio", user.getBio());
        res.put("createdAt", user.getCreatedAt());

        return ResponseEntity.ok(res);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request) {
        Integer userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName());
        }
        
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        userRepository.save(user);

        Map<String, Object> res = new HashMap<>();
        res.put("userId", user.getUserId());
        res.put("email", user.getEmail());
        res.put("fullName", user.getFullName());
        res.put("bio", user.getBio());

        return ResponseEntity.ok(res);
    }
}

@Data
class UpdateProfileRequest {
    private String fullName;
    private String bio;
}

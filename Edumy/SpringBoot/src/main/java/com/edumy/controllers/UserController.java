package com.edumy.controllers;

import com.edumy.auth.AuthenticatedUser;
import com.edumy.dto.UpdateProfileRequest;
import com.edumy.dto.UserProfileResponse;
import com.edumy.interceptors.AuthInterceptor;
import com.edumy.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(HttpServletRequest request) {
        int userId = resolveUserId(request);
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            HttpServletRequest request,
            @Valid @RequestBody UpdateProfileRequest body) {
        int userId = resolveUserId(request);
        return ResponseEntity.ok(userService.updateProfile(userId, body));
    }

    private int resolveUserId(HttpServletRequest request) {
        AuthenticatedUser user =
                (AuthenticatedUser) request.getAttribute(AuthInterceptor.AUTHENTICATED_USER_ATTRIBUTE);
        return Integer.parseInt(user.userId());
    }
}

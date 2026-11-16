package com.edumy.identity.auth.controller;

import com.edumy.identity.auth.dto.*;
import com.edumy.identity.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/auth/mobile", "/api/auth/mobile"})
public class MobileAuthController {

    private final AuthService authService;

    public MobileAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<MobileTokensDto> register(@Valid @RequestBody MobileRegisterRequest request) {
        MobileTokensDto tokens = authService.mobileRegister(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(tokens);
    }

    @PostMapping("/login")
    public ResponseEntity<MobileTokensDto> login(@Valid @RequestBody MobileLoginRequest request) {
        MobileTokensDto tokens = authService.mobileLogin(request);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<MobileTokensDto> refresh(@Valid @RequestBody MobileRefreshRequest request) {
        MobileTokensDto tokens = authService.mobileRefresh(request);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody(required = false) MobileLogoutRequest request) {
        authService.mobileLogout(request != null ? request : new MobileLogoutRequest());
        return ResponseEntity.noContent().build();
    }
}

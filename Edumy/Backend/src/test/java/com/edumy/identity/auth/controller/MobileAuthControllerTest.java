package com.edumy.identity.auth.controller;

import com.edumy.identity.auth.dto.*;
import com.edumy.identity.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class MobileAuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private MobileAuthController mobileAuthController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(mobileAuthController).build();
    }

    @Test
    @DisplayName("POST /api/v1/auth/mobile/register - should return 201 and mobile tokens")
    void testMobileRegister() throws Exception {
        MobileRegisterRequest request = MobileRegisterRequest.builder()
                .email("student@edumy.com")
                .password("Password123!")
                .fullName("Nguyen Van A")
                .build();

        MobileTokensDto tokens = MobileTokensDto.builder()
                .accessToken("mock-access-token")
                .expiresAt("2026-11-24T00:00:00Z")
                .refreshToken("mock-refresh-token")
                .refreshExpiresAt("2026-11-24T00:00:00Z")
                .build();

        when(authService.mobileRegister(any(MobileRegisterRequest.class))).thenReturn(tokens);

        mockMvc.perform(post("/api/v1/auth/mobile/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("mock-refresh-token"))
                .andExpect(jsonPath("$.expiresAt").isNotEmpty())
                .andExpect(jsonPath("$.refreshExpiresAt").isNotEmpty());

        verify(authService, times(1)).mobileRegister(any(MobileRegisterRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/auth/mobile/login - should return 200 and mobile tokens")
    void testMobileLogin() throws Exception {
        MobileLoginRequest request = MobileLoginRequest.builder()
                .email("student@edumy.com")
                .password("Password123!")
                .build();

        MobileTokensDto tokens = MobileTokensDto.builder()
                .accessToken("mock-login-access-token")
                .expiresAt("2026-11-24T00:00:00Z")
                .refreshToken("mock-login-refresh-token")
                .refreshExpiresAt("2026-11-24T00:00:00Z")
                .build();

        when(authService.mobileLogin(any(MobileLoginRequest.class))).thenReturn(tokens);

        mockMvc.perform(post("/api/v1/auth/mobile/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock-login-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("mock-login-refresh-token"));

        verify(authService, times(1)).mobileLogin(any(MobileLoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/auth/mobile/refresh - should rotate and return new tokens")
    void testMobileRefresh() throws Exception {
        MobileRefreshRequest request = MobileRefreshRequest.builder()
                .refreshToken("old-refresh-token")
                .build();

        MobileTokensDto newTokens = MobileTokensDto.builder()
                .accessToken("new-rotated-access-token")
                .expiresAt("2026-11-24T00:00:00Z")
                .refreshToken("new-rotated-refresh-token")
                .refreshExpiresAt("2026-11-24T00:00:00Z")
                .build();

        when(authService.mobileRefresh(any(MobileRefreshRequest.class))).thenReturn(newTokens);

        mockMvc.perform(post("/api/v1/auth/mobile/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-rotated-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-rotated-refresh-token"));

        verify(authService, times(1)).mobileRefresh(any(MobileRefreshRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/auth/mobile/logout - should revoke refresh token and return 204")
    void testMobileLogout() throws Exception {
        MobileLogoutRequest request = MobileLogoutRequest.builder()
                .refreshToken("token-to-revoke")
                .build();

        doNothing().when(authService).mobileLogout(any(MobileLogoutRequest.class));

        mockMvc.perform(post("/api/v1/auth/mobile/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(authService, times(1)).mobileLogout(any(MobileLogoutRequest.class));
    }
}

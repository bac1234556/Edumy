package com.edumy.identity.auth.service;

import com.edumy.identity.auth.dto.*;
import com.edumy.identity.auth.model.RefreshToken;
import com.edumy.identity.auth.repository.RefreshTokenRepository;
import com.edumy.identity.auth.service.impl.AuthServiceImpl;
import com.edumy.identity.user.model.User;
import com.edumy.identity.user.repository.UserRepository;
import com.edumy.identity.user.service.UserActivityService;
import com.edumy.notification.service.MailService;
import com.edumy.shared.exception.BusinessException;
import com.edumy.shared.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserActivityService userActivityService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MailService mailService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .userId(1)
                .email("student@edumy.com")
                .passwordHash("hashed-password")
                .fullName("Nguyen Van A")
                .role("Student")
                .isActive(true)
                .isEmailVerified(true)
                .build();
    }

    @Test
    @DisplayName("mobileRegister - successful registration returns tokens")
    void testMobileRegister_Success() {
        MobileRegisterRequest request = MobileRegisterRequest.builder()
                .email("newstudent@edumy.com")
                .password("Password123!")
                .fullName("Tran Van B")
                .build();

        when(userRepository.existsByEmail("newstudent@edumy.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded-pass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setUserId(2);
            return u;
        });
        when(jwtTokenProvider.generateToken(any(), any(), any(), any())).thenReturn("mock-jwt");

        MobileTokensDto result = authService.mobileRegister(request);

        assertNotNull(result);
        assertEquals("mock-jwt", result.getAccessToken());
        assertNotNull(result.getRefreshToken());
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("mobileLogin - locked account throws FORBIDDEN")
    void testMobileLogin_AccountLocked() {
        sampleUser.setIsActive(false);
        MobileLoginRequest request = MobileLoginRequest.builder()
                .email("student@edumy.com")
                .password("Password123!")
                .build();

        when(userRepository.findByEmail("student@edumy.com")).thenReturn(sampleUser);
        when(passwordEncoder.matches("Password123!", "hashed-password")).thenReturn(true);

        assertThrows(BusinessException.class, () -> authService.mobileLogin(request));
    }

    @Test
    @DisplayName("mobileRefresh - token reuse detection revokes all tokens")
    void testMobileRefresh_ReuseDetection() {
        RefreshToken reusedToken = RefreshToken.builder()
                .id(10)
                .userId(1)
                .token("stolen-token")
                .revoked(LocalDateTime.now().minusHours(1))
                .replacedByToken("replacement-token")
                .expires(LocalDateTime.now().plusDays(5))
                .build();

        RefreshToken activeToken = RefreshToken.builder()
                .id(11)
                .userId(1)
                .token("current-active-token")
                .expires(LocalDateTime.now().plusDays(5))
                .build();

        when(refreshTokenRepository.findByToken("stolen-token")).thenReturn(reusedToken);
        when(refreshTokenRepository.findByUserId(1)).thenReturn(List.of(reusedToken, activeToken));

        MobileRefreshRequest request = MobileRefreshRequest.builder()
                .refreshToken("stolen-token")
                .build();

        assertThrows(BusinessException.class, () -> authService.mobileRefresh(request));
        verify(refreshTokenRepository, times(1)).saveAll(any());
        assertTrue(activeToken.isRevoked());
    }

    @Test
    @DisplayName("mobileLogout - revokes active refresh token")
    void testMobileLogout_Success() {
        RefreshToken token = RefreshToken.builder()
                .id(1)
                .token("active-token")
                .expires(LocalDateTime.now().plusDays(7))
                .build();

        when(refreshTokenRepository.findByToken("active-token")).thenReturn(token);

        MobileLogoutRequest request = MobileLogoutRequest.builder()
                .refreshToken("active-token")
                .build();

        authService.mobileLogout(request);

        assertTrue(token.isRevoked());
        verify(refreshTokenRepository, times(1)).save(token);
    }
}

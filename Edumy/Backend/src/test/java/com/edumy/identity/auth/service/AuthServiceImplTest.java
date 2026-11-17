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
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
        when(jwtTokenProvider.getAccessTtlMinutes()).thenReturn(60L);
        when(jwtTokenProvider.getExpireDays()).thenReturn(7);

        MobileTokensDto result = authService.mobileRegister(request);

        assertNotNull(result);
        assertEquals("mock-jwt", result.getAccessToken());
        assertNotNull(result.getRefreshToken());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        assertFalse(Boolean.TRUE.equals(userCaptor.getValue().getIsEmailVerified()));
        assertEquals("Student", userCaptor.getValue().getRole());
        verify(mailService, times(1)).sendEmailVerification(eq("newstudent@edumy.com"), eq("Tran Van B"), any());

        ArgumentCaptor<RefreshToken> rtCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, times(1)).save(rtCaptor.capture());
        assertEquals("MOBILE", rtCaptor.getValue().getClient());
        assertNotNull(rtCaptor.getValue().getFamilyId());
    }

    @Test
    @DisplayName("register - always assigns Student role")
    void testRegister_AlwaysStudent() {
        RegisterDto dto = new RegisterDto();
        dto.setFullName("Test User");
        dto.setEmail("test@edumy.com");
        dto.setPassword("Password123");

        when(userRepository.existsByEmail("test@edumy.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("encoded");

        Map<String, Object> res = authService.register(dto);
        assertNotNull(res.get("message"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("Student", userCaptor.getValue().getRole());
        assertFalse(Boolean.TRUE.equals(userCaptor.getValue().getIsEmailVerified()));
    }

    @Test
    @DisplayName("login - locked account throws 403 AUTH_ACCOUNT_LOCKED")
    void testLogin_AccountLocked() {
        sampleUser.setIsActive(false);
        LoginDto dto = new LoginDto();
        dto.setEmail("student@edumy.com");
        dto.setPassword("Password123!");

        when(userRepository.findByEmail("student@edumy.com")).thenReturn(sampleUser);
        when(passwordEncoder.matches("Password123!", "hashed-password")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(dto, mock(jakarta.servlet.http.HttpServletResponse.class)));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("AUTH_ACCOUNT_LOCKED", ex.getCode());
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
    @DisplayName("mobileRefresh - token reuse detection revokes only mobile family, leaves web session intact")
    void testMobileRefresh_ReuseDetection() {
        RefreshToken reusedToken = RefreshToken.builder()
                .id(10)
                .userId(1)
                .familyId("fam-mobile-1")
                .client("MOBILE")
                .token("stolen-token")
                .revoked(LocalDateTime.now().minusHours(1))
                .replacedByToken("replacement-token")
                .expires(LocalDateTime.now().plusDays(5))
                .build();

        RefreshToken activeMobileToken = RefreshToken.builder()
                .id(11)
                .userId(1)
                .familyId("fam-mobile-1")
                .client("MOBILE")
                .token("current-active-token")
                .expires(LocalDateTime.now().plusDays(5))
                .build();

        RefreshToken activeWebToken = RefreshToken.builder()
                .id(12)
                .userId(1)
                .familyId("fam-web-1")
                .client("WEB")
                .token("web-active-token")
                .expires(LocalDateTime.now().plusDays(5))
                .build();

        when(refreshTokenRepository.findByToken("stolen-token")).thenReturn(reusedToken);
        when(refreshTokenRepository.findByFamilyId("fam-mobile-1")).thenReturn(List.of(reusedToken, activeMobileToken));

        MobileRefreshRequest request = MobileRefreshRequest.builder()
                .refreshToken("stolen-token")
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.mobileRefresh(request));
        assertEquals("AUTH_REFRESH_REUSED", ex.getCode());
        verify(refreshTokenRepository, times(1)).saveAll(any());
        verify(refreshTokenRepository, never()).findByUserId(any());
        assertTrue(activeMobileToken.isRevoked());
        assertFalse(activeWebToken.isRevoked());
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

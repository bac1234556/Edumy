package com.edumy.modules.auth.service;

import com.edumy.modules.auth.dto.*;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

public interface AuthService {
    Map<String, Object> register(RegisterDto model);
    AuthResponseDto login(LoginDto loginDto, HttpServletResponse response);
    AuthResponseDto refreshToken(String refreshToken, HttpServletResponse response);
    Map<String, Object> revokeToken(String refreshToken);
    AuthResponseDto googleLogin(GoogleLoginDto googleLoginDto, HttpServletResponse response);
    Map<String, Object> forgotPassword(ForgotPasswordDto model);
    Map<String, Object> resetPassword(ResetPasswordDto model);
}

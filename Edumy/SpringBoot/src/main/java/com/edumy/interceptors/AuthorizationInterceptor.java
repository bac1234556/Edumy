package com.edumy.interceptors;

import com.edumy.annotations.RequiresRoles;
import com.edumy.common.api.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthorizationInterceptor implements HandlerInterceptor {
    private final ObjectMapper objectMapper;

    public AuthorizationInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws IOException {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequiresRoles requiredRoles = AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getMethod(), RequiresRoles.class);
        if (requiredRoles == null) {
            requiredRoles = AnnotatedElementUtils.findMergedAnnotation(
                    handlerMethod.getBeanType(), RequiresRoles.class);
        }
        if (requiredRoles == null) {
            return true;
        }

        if (request.getUserPrincipal() == null) {
            writeError(response, request, HttpStatus.UNAUTHORIZED, "Authentication is required.");
            return false;
        }
        if (Arrays.stream(requiredRoles.value()).noneMatch(request::isUserInRole)) {
            writeError(response, request, HttpStatus.FORBIDDEN, "Insufficient permissions.");
            return false;
        }
        return true;
    }

    private void writeError(
            HttpServletResponse response,
            HttpServletRequest request,
            HttpStatus status,
            String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()));
    }
}

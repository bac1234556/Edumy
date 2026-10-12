package com.edumy.interceptors;

import com.edumy.annotations.RequiresRoles;
import com.edumy.auth.AuthenticatedUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    public static final String AUTHENTICATED_USER_ATTRIBUTE =
            AuthInterceptor.class.getName() + ".authenticatedUser";

    private final ObjectMapper objectMapper;

    public AuthInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        AuthenticatedUser authenticatedUser =
                (AuthenticatedUser) request.getAttribute(AUTHENTICATED_USER_ATTRIBUTE);
        if (authenticatedUser == null) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication is required.");
            return false;
        }

        if (handler instanceof HandlerMethod handlerMethod) {
            RequiresRoles requirement = AnnotatedElementUtils.findMergedAnnotation(
                    handlerMethod.getMethod(), RequiresRoles.class);
            if (requirement == null) {
                requirement = AnnotatedElementUtils.findMergedAnnotation(
                        handlerMethod.getBeanType(), RequiresRoles.class);
            }

            if (requirement != null
                    && Arrays.stream(requirement.value()).noneMatch(authenticatedUser.role()::equals)) {
                writeError(response, HttpServletResponse.SC_FORBIDDEN, "Insufficient permissions.");
                return false;
            }
        }
        return true;
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), Map.of("message", message));
    }
}

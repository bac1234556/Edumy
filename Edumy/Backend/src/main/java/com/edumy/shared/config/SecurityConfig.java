package com.edumy.shared.config;

import com.edumy.shared.response.ApiResponse;
import com.edumy.shared.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${edumy.cors.allowed-origins:http://localhost:3000,http://localhost:5173,http://localhost:8081,http://localhost:19006}")
    private String allowedOrigins;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(org.springframework.security.config.Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/api/Auth/**", "/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/catalog/**", "/api/catalog/**").permitAll()
                .requestMatchers("/api/courses", "/api/Courses", "/api/v1/courses").permitAll()
                .requestMatchers("/api/courses/**", "/api/Courses/**", "/api/v1/courses/**").permitAll()
                .requestMatchers("/api/categories/**", "/api/Categories/**", "/api/v1/categories/**").permitAll()
                .requestMatchers("/api/v1/certificates/verify/**", "/api/certificates/verify/**").permitAll()
                .requestMatchers("/api/reviews/course/**", "/api/Reviews/course/**", "/api/v1/reviews/course/**").permitAll()
                .requestMatchers("/api/quizzes/**", "/api/Quizzes/**").permitAll()
                .requestMatchers("/api/v1/questions/**", "/api/questions/**").permitAll()
                // Lesson access endpoint: allow permitAll so unauthenticated users can access preview lessons;
                // Secure check is performed in service/controller based on JWT / enrollment.
                .requestMatchers("/api/v1/lessons/*/access", "/api/v1/lessons/**", "/api/Lessons/**").permitAll()
                // Payment callbacks, Webhooks & Order Status polling only (create-qr and create-url require auth)
                .requestMatchers(
                    "/api/v1/payments/webhook", "/api/v1/payments/webhook/**",
                    "/api/payments/webhook", "/api/payments/webhook/**",
                    "/api/payment/webhook", "/api/payment/webhook/**",
                    "/api/v1/payment/webhook", "/api/v1/payment/webhook/**",
                    "/api/Payment/webhook", "/api/Payment/webhook/**",
                    "/api/Payments/webhook", "/api/Payments/webhook/**",
                    "/api/webhook/**", "/webhook/**",
                    "/api/v1/payments/vnpay-ipn", "/api/Payment/ipn", "/api/payment/ipn",
                    "/api/v1/payments/vnpay-return", "/api/Payment/vnpay-return", "/api/payment/vnpay-return",
                    "/api/v1/payments/orders/*/status"
                ).permitAll()
                .requestMatchers("/api/v1/orders/*/status", "/api/orders/*/status", "/api/Orders/*/status").permitAll()
                .requestMatchers("/api/coupons/validate", "/api/Coupons/validate").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/health", "/actuator/prometheus").permitAll()
                .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> writeErrorResponse(
                response,
                HttpServletResponse.SC_UNAUTHORIZED,
                "UNAUTHORIZED",
                "Chưa xác thực hoặc phiên đăng nhập đã hết hạn."
        );
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> writeErrorResponse(
                response,
                HttpServletResponse.SC_FORBIDDEN,
                "FORBIDDEN",
                "Bạn không có quyền truy cập tài nguyên này."
        );
    }

    private void writeErrorResponse(HttpServletResponse response, int status, String code, String message)
            throws java.io.IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> errors = new LinkedHashMap<>();
        errors.put("code", code);
        errors.put("fields", null);
        errors.put("traceId", UUID.randomUUID().toString());

        ApiResponse<Void> body = ApiResponse.error(message, errors);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        if (allowedOrigins != null && !allowedOrigins.isBlank()) {
            for (String origin : allowedOrigins.split(",")) {
                String trimmed = origin.trim();
                if (!trimmed.isEmpty()) {
                    config.addAllowedOrigin(trimmed);
                }
            }
        }
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}

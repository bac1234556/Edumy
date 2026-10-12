package com.edumy.backend.config;

import com.edumy.backend.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(org.springframework.security.config.Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/api/Auth/**").permitAll()
                .requestMatchers("/api/courses", "/api/Courses").permitAll()
                .requestMatchers("/api/courses/**", "/api/Courses/**").permitAll()
                .requestMatchers("/api/categories/**", "/api/Categories/**").permitAll()
                .requestMatchers("/api/reviews/course/**", "/api/Reviews/course/**").permitAll()
                .requestMatchers("/api/quizzes/**", "/api/Quizzes/**").permitAll()
                .requestMatchers("/api/mltest/**", "/api/MLTest/**").permitAll()
                // Lesson access endpoint: allow permitAll so unauthenticated users can access preview lessons;
                // Secure check is performed in service/controller based on JWT / enrollment.
                .requestMatchers("/api/v1/lessons/*/access", "/api/v1/lessons/**", "/api/Lessons/**").permitAll()
                // Payment callbacks & Webhooks: public endpoints called by payment gateways
                .requestMatchers("/api/v1/payments/vnpay-ipn", "/api/v1/payments/vnpay-return", "/api/v1/payments/webhook", 
                                 "/api/payment/webhook", "/api/Payment/webhook", "/api/Payment/ipn/**").permitAll()
                .requestMatchers("/api/coupons/validate", "/api/Coupons/validate").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}

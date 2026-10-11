package com.edumy;

import com.edumy.annotations.RequiresRoles;
import com.edumy.auth.JwtTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "edumy.jwt.secret=architecture-test-only-secret-32-bytes-minimum",
        "edumy.auth.expose-reset-token=true",
        "spring.datasource.url=jdbc:h2:mem:auth-integration;DB_CLOSE_DELAY=-1"
})
@AutoConfigureMockMvc
@Import(AuthFlowIntegrationTest.ProtectedTestController.class)
class AuthFlowIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerLoginRefreshAndRevoke() throws Exception {
        String email = "auth-" + System.nanoTime() + "@example.test";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Test User","email":"%s","password":"secret123","role":"Admin"}
                                """.formatted(email)))
                .andExpect(status().isOk());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"secret123"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().secure("refreshToken", true))
                .andExpect(jsonPath("$.role").value("Student"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andReturn();

        String accessToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();
        assertEquals("Student", jwtTokenService.parse(accessToken).role());

        Cookie oldRefreshCookie = loginResult.getResponse().getCookie("refreshToken");
        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh-token").cookie(oldRefreshCookie))
                .andExpect(status().isOk())
                .andReturn();
        Cookie newRefreshCookie = refreshResult.getResponse().getCookie("refreshToken");
        assertNotEquals(oldRefreshCookie.getValue(), newRefreshCookie.getValue());
        mockMvc.perform(post("/api/auth/refresh-token").cookie(oldRefreshCookie))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/revoke-token").cookie(newRefreshCookie))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("refreshToken", 0));
        mockMvc.perform(post("/api/auth/refresh-token").cookie(newRefreshCookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void resetsPasswordWithShortLivedToken() throws Exception {
        String email = "reset-" + System.nanoTime() + "@example.test";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Reset User","email":"%s","password":"secret123"}
                                """.formatted(email)))
                .andExpect(status().isOk());

        MvcResult forgotResult = mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();
        String resetToken = objectMapper.readTree(forgotResult.getResponse().getContentAsString())
                .get("resetToken").asText();

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","token":"%s","newPassword":"newsecret123"}
                                """.formatted(email, resetToken)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"newsecret123"}
                                """.formatted(email)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","token":"%s","newPassword":"newsecret123"}
                                """.formatted(email, resetToken)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void interceptorRequiresAuthenticationAndChecksRoles() throws Exception {
        mockMvc.perform(get("/api/test/admin"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(containsString("Authentication")));

        String email = "role-" + System.nanoTime() + "@example.test";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Student","email":"%s","password":"secret123"}
                                """.formatted(email)))
                .andExpect(status().isOk());
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"secret123"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();
        String accessToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();

        mockMvc.perform(get("/api/test/admin").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());
    }

    @RestController
    @RequestMapping("/api/test")
    static class ProtectedTestController {
        @GetMapping("/admin")
        @RequiresRoles("Admin")
        String adminOnly() {
            return "admin";
        }
    }
}

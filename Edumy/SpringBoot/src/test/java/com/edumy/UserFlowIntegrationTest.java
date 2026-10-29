package com.edumy;

import com.edumy.auth.JwtTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:user-integration;DB_CLOSE_DELAY=-1"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    void unauthenticatedRequestsAreRejected() throws Exception {
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(containsString("Authentication")));

        mockMvc.perform(put("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Anonymous"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(containsString("Authentication")));
    }

    @Test
    void getProfileAndFullUpdateProfileFlow() throws Exception {
        String email = "user-" + System.nanoTime() + "@example.test";
        String token = registerAndLogin(email, "Initial Name", "secret123");

        // 1. Get initial profile
        mockMvc.perform(get("/api/users/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.fullName").value("Initial Name"))
                .andExpect(jsonPath("$.bio").doesNotExist())
                .andExpect(jsonPath("$.createdAt").exists());

        // 2. Update profile (both fullName and bio)
        mockMvc.perform(put("/api/users/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Updated Name","bio":"Developer passionate about backend systems."}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.fullName").value("Updated Name"))
                .andExpect(jsonPath("$.bio").value("Developer passionate about backend systems."));

        // 3. Verify changes persisted in DB
        mockMvc.perform(get("/api/users/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated Name"))
                .andExpect(jsonPath("$.bio").value("Developer passionate about backend systems."));

        // 4. Verify Monitoring Aspect records metrics for user operations
        assertNotNull(meterRegistry.find("edumy.method.execution")
                .tag("operation", "user.get-profile")
                .timer());
        assertNotNull(meterRegistry.find("edumy.method.execution")
                .tag("operation", "user.update-profile")
                .timer());
    }

    @Test
    void partialUpdatePreservesExistingFields() throws Exception {
        String email = "partial-" + System.nanoTime() + "@example.test";
        String token = registerAndLogin(email, "John Doe", "secret123");

        // Set initial bio
        mockMvc.perform(put("/api/users/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"bio":"Original Bio"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.bio").value("Original Bio"));

        // Update only fullName (bio omitted/null)
        mockMvc.perform(put("/api/users/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Johnathan Doe"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Johnathan Doe"))
                .andExpect(jsonPath("$.bio").value("Original Bio"));
    }

    @Test
    void nonExistentUserReturns404() throws Exception {
        String ghostToken = jwtTokenService.issueToken("999999", "ghost@example.test", "Ghost", "Student");

        mockMvc.perform(get("/api/users/profile")
                        .header("Authorization", "Bearer " + ghostToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void validationRejectsOversizedInput() throws Exception {
        String email = "val-" + System.nanoTime() + "@example.test";
        String token = registerAndLogin(email, "Validation Test", "secret123");

        String oversizedName = "a".repeat(101);
        mockMvc.perform(put("/api/users/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"%s"}
                                """.formatted(oversizedName)))
                .andExpect(status().isBadRequest());
    }

    private String registerAndLogin(String email, String fullName, String password) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"%s","email":"%s","password":"%s"}
                                """.formatted(fullName, email, password)))
                .andExpect(status().isOk());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();
    }
}

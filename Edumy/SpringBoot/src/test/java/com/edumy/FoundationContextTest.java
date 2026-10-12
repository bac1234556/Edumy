package com.edumy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.micrometer.core.instrument.MeterRegistry;
import com.edumy.annotations.Monitored;
import com.edumy.annotations.RequiresRoles;
import com.edumy.auth.AuthenticatedUser;
import com.edumy.interceptors.AuthInterceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({FoundationContextTest.ProtectedTestController.class, FoundationContextTest.FoundationProbeService.class})
class FoundationContextTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FoundationProbeService foundationProbeService;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    void startsWithTheIsolatedTestDatabaseAndExposesHealth() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-ID"));
    }

    @Test
    void replacesUnsafeRequestIds() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/actuator/health")
                        .header("X-Request-ID", "invalid\r\nheader"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-ID"));
    }

    @Test
    void enforcesRoleRequirementsInTheInterceptor() throws Exception {
        mockMvc.perform(get("/api/foundation/admin"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/foundation/admin")
                        .requestAttr(
                                AuthInterceptor.AUTHENTICATED_USER_ATTRIBUTE,
                                new AuthenticatedUser("1", "test@example.test", "Student")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/foundation/admin")
                        .requestAttr(
                                AuthInterceptor.AUTHENTICATED_USER_ATTRIBUTE,
                                new AuthenticatedUser("1", "test@example.test", "Admin")))
                .andExpect(status().isOk());
    }

    @Test
    void recordsMetricsThroughTheAspect() {
        assertEquals("ok", foundationProbeService.probe());
        assertNotNull(meterRegistry.find("edumy.method.execution")
                .tag("operation", "foundation.probe")
                .timer());
        assertEquals(1, meterRegistry.find("edumy.method.execution")
                .tag("operation", "foundation.probe")
                .timer()
                .count());
    }

    @RestController
    @RequestMapping("/api/foundation")
    static class ProtectedTestController {
        @GetMapping("/admin")
        @RequiresRoles("Admin")
        String adminOnly() {
            return "ok";
        }
    }

    @Service
    static class FoundationProbeService {
        @Monitored("foundation.probe")
        public String probe() {
            return "ok";
        }
    }
}

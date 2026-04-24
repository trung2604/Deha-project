package com.deha.HumanResourceManagement.controller.security;

import com.deha.HumanResourceManagement.config.audit.AuditLoggingInterceptor;
import com.deha.HumanResourceManagement.config.security.GoogleOAuth2FailureHandler;
import com.deha.HumanResourceManagement.config.security.GoogleOAuth2SuccessHandler;
import com.deha.HumanResourceManagement.config.security.JwtUtil;
import com.deha.HumanResourceManagement.config.security.SecurityConfig;
import com.deha.HumanResourceManagement.controller.UserController;
import com.deha.HumanResourceManagement.dto.user.UserResponse;
import com.deha.HumanResourceManagement.service.IUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "spring.security.oauth2.client.registration.google.client-id=test-client-id",
        "spring.security.oauth2.client.registration.google.client-secret=test-client-secret",
        "spring.security.oauth2.client.provider.google.authorization-uri=https://accounts.google.com/o/oauth2/v2/auth",
        "spring.security.oauth2.client.provider.google.token-uri=https://oauth2.googleapis.com/token",
        "spring.security.oauth2.client.provider.google.user-info-uri=https://openidconnect.googleapis.com/v1/userinfo"
})
class UserControllerSecurityWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IUserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private GoogleOAuth2SuccessHandler googleOAuth2SuccessHandler;

    @MockitoBean
    private GoogleOAuth2FailureHandler googleOAuth2FailureHandler;

    @MockitoBean
    private AuditLoggingInterceptor auditLoggingInterceptor;

    @Test
    void getUsersWithoutAuthReturns401() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getUsersAsEmployeeReturns403() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANAGER_DEPARTMENT")
    void getUsersAsDepartmentManagerReturns200() throws Exception {
        when(userService.getUsersWithFilters(any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MANAGER_DEPARTMENT")
    void createUserAsDepartmentManagerReturns403() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"A\",\"lastName\":\"B\",\"email\":\"a@b.com\",\"password\":\"secret\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserByIdAsAdminReturns200() throws Exception {
        when(userService.getUserById(any(UUID.class))).thenReturn(new UserResponse());

        mockMvc.perform(get("/api/users/{id}", UUID.randomUUID()))
                .andExpect(status().isOk());
    }
}


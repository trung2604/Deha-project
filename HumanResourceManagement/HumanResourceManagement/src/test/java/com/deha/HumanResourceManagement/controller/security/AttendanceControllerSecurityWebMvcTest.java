package com.deha.HumanResourceManagement.controller.security;

import com.deha.HumanResourceManagement.config.AuditLoggingInterceptor;
import com.deha.HumanResourceManagement.config.security.AccessScopeService;
import com.deha.HumanResourceManagement.config.security.GoogleOAuth2FailureHandler;
import com.deha.HumanResourceManagement.config.security.GoogleOAuth2SuccessHandler;
import com.deha.HumanResourceManagement.config.security.JwtUtil;
import com.deha.HumanResourceManagement.config.security.SecurityConfig;
import com.deha.HumanResourceManagement.controller.AttendanceController;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.mapper.attendance.AttendanceMapper;
import com.deha.HumanResourceManagement.service.IAttendanceService;
import com.deha.HumanResourceManagement.service.support.ClientIpResolverService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AttendanceController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "spring.security.oauth2.client.registration.google.client-id=test-client-id",
        "spring.security.oauth2.client.registration.google.client-secret=test-client-secret",
        "spring.security.oauth2.client.provider.google.authorization-uri=https://accounts.google.com/o/oauth2/v2/auth",
        "spring.security.oauth2.client.provider.google.token-uri=https://oauth2.googleapis.com/token",
        "spring.security.oauth2.client.provider.google.user-info-uri=https://openidconnect.googleapis.com/v1/userinfo"
})
class AttendanceControllerSecurityWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IAttendanceService attendanceService;

    @MockitoBean
    private AccessScopeService accessScopeService;

    @MockitoBean
    private ClientIpResolverService clientIpResolverService;

    @MockitoBean
    private AttendanceMapper attendanceMapper;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private GoogleOAuth2SuccessHandler googleOAuth2SuccessHandler;

    @MockitoBean
    private GoogleOAuth2FailureHandler googleOAuth2FailureHandler;

    @MockitoBean
    private AuditLoggingInterceptor auditLoggingInterceptor;

    @Test
    void checkInWithoutAuthReturns401() throws Exception {
        mockMvc.perform(post("/api/attendance/check-in"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void officeTodayAsEmployeeReturns403() throws Exception {
        mockMvc.perform(get("/api/attendance/office/today"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANAGER_OFFICE")
    void officeTodayAsOfficeManagerReturns200() throws Exception {
        when(accessScopeService.currentUserOrThrow()).thenReturn(new User());
        when(attendanceService.getOfficeTodayLogsOrEmpty(any(User.class), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/attendance/office/today"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void checkInAsEmployeeReturns200() throws Exception {
        when(accessScopeService.currentUserOrThrow()).thenReturn(new User());
        when(clientIpResolverService.extractClientIps(any())).thenReturn(List.of("127.0.0.1"));

        mockMvc.perform(post("/api/attendance/check-in"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void departmentTodayAsAdminReturns403() throws Exception {
        mockMvc.perform(get("/api/attendance/department/today"))
                .andExpect(status().isForbidden());
    }
}


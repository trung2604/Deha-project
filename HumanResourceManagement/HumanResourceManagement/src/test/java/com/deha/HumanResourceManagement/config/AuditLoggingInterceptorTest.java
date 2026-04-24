package com.deha.HumanResourceManagement.config;

import com.deha.HumanResourceManagement.config.audit.AuditLoggingInterceptor;
import com.deha.HumanResourceManagement.service.support.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class AuditLoggingInterceptorTest {

    private AuditLogService auditLogService;
    private AuditLoggingInterceptor interceptor;

    @BeforeEach
    void setUp() {
        auditLogService = mock(AuditLogService.class);
        interceptor = new AuditLoggingInterceptor(auditLogService);
    }

    @Test
    void shouldAuditAuthLogin() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.preHandle(request, response, new Object());

        assertNotNull(request.getAttribute(AuditLogService.ATTR_AUDIT_START_NANOS));
        interceptor.afterCompletion(request, response, new Object(), null);

        verify(auditLogService, times(1)).logHttpWrite(any(HttpServletRequest.class), anyInt(), anyLong());
    }

    @Test
    void shouldAuditDeleteAcrossApi() {
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/notifications/1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.preHandle(request, response, new Object());

        assertNotNull(request.getAttribute(AuditLogService.ATTR_AUDIT_START_NANOS));
        interceptor.afterCompletion(request, response, new Object(), null);

        verify(auditLogService, times(1)).logHttpWrite(any(HttpServletRequest.class), anyInt(), anyLong());
    }

    @Test
    void shouldNotAuditNonSystemWriteApi() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/chat/send");
        MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.preHandle(request, response, new Object());

        assertNull(request.getAttribute(AuditLogService.ATTR_AUDIT_START_NANOS));
        interceptor.afterCompletion(request, response, new Object(), null);

        verify(auditLogService, never()).logHttpWrite(any(HttpServletRequest.class), anyInt(), anyLong());
    }

    @Test
    void shouldNotAuditReadApi() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users");
        MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.preHandle(request, response, new Object());
        interceptor.afterCompletion(request, response, new Object(), null);

        verify(auditLogService, never()).logHttpWrite(any(HttpServletRequest.class), anyInt(), anyLong());
    }
}



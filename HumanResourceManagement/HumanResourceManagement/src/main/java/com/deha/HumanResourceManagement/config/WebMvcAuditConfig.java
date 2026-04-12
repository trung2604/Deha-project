package com.deha.HumanResourceManagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcAuditConfig implements WebMvcConfigurer {
    private final AuditLoggingInterceptor auditLoggingInterceptor;

    public WebMvcAuditConfig(AuditLoggingInterceptor auditLoggingInterceptor) {
        this.auditLoggingInterceptor = auditLoggingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(auditLoggingInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/swagger-ui/**", "/v3/api-docs/**");
    }
}


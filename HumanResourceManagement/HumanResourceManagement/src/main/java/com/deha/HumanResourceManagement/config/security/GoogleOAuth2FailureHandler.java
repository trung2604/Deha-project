package com.deha.HumanResourceManagement.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class GoogleOAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        String callbackBase = resolveCallbackBase();
        getRedirectStrategy().sendRedirect(request, response,
                UriComponentsBuilder.fromUriString(callbackBase)
                        .replaceQuery(null)
                        .queryParam("error", "oauth2_failed")
                        .build(true)
                        .toUriString());
    }

    private String resolveCallbackBase() {
        if (redirectUri == null || redirectUri.isBlank()) {
            return fallbackCallbackUrl();
        }

        String normalized = redirectUri.toLowerCase();
        if (normalized.contains("/login/oauth2/code/google")) {
            // Prevent infinite redirects when REDIRECT_URI points to backend OAuth callback URL.
            return fallbackCallbackUrl();
        }

        return redirectUri;
    }

    private String fallbackCallbackUrl() {
        if (frontendUrl == null || frontendUrl.isBlank()) {
            return "/auth/callback";
        }
        return frontendUrl.endsWith("/") ? frontendUrl + "auth/callback" : frontendUrl + "/auth/callback";
    }
}
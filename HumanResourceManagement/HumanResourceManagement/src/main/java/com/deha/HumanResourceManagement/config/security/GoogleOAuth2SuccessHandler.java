package com.deha.HumanResourceManagement.config.security;

import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.repository.UserRepository;
import com.deha.HumanResourceManagement.service.support.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class GoogleOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final AuthService authService;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public GoogleOAuth2SuccessHandler(
            UserRepository userRepository,
            AuthService authService
    ) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
            String callbackBase = resolveCallbackBase();
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        Object emailAttr = oAuth2User.getAttributes().get("email");
        String email = emailAttr != null ? emailAttr.toString() : null;
        if (email == null || email.isBlank()) {
            getRedirectStrategy().sendRedirect(request, response,
                    withQuery(callbackBase, "error", "oauth2_email_missing"));
            return;
        }

        User user = userRepository.findByEmail(email)
                .orElse(null);

        if (user == null) {
            getRedirectStrategy().sendRedirect(request, response,
                    withQuery(callbackBase, "error", "account_not_found"));
            return;
        }

        if (!user.isActive()) {
            getRedirectStrategy().sendRedirect(request, response,
                    withQuery(callbackBase, "error", "account_inactive"));
            return;
        }

        String code = authService.createOAuth2ExchangeCode(user.getId());

        getRedirectStrategy().sendRedirect(request, response,
                withQuery(callbackBase, "code", URLEncoder.encode(code, StandardCharsets.UTF_8)));
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

    private String withQuery(String baseUrl, String key, String value) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .replaceQuery(null)
                .queryParam(key, value)
                .build(true)
                .toUriString();
    }
}
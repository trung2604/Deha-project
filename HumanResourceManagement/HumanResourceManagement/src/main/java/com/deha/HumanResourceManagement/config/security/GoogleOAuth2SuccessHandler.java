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

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class GoogleOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final AuthService authService;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

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
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        Object emailAttr = oAuth2User.getAttributes().get("email");
        String email = emailAttr != null ? emailAttr.toString() : null;
        if (email == null || email.isBlank()) {
            getRedirectStrategy().sendRedirect(request, response,
                    redirectUri + "?error=oauth2_email_missing");
            return;
        }

        User user = userRepository.findByEmail(email)
                .orElse(null);

        if (user == null) {
            getRedirectStrategy().sendRedirect(request, response,
                    redirectUri + "?error=account_not_found");
            return;
        }

        if (!user.isActive()) {
            getRedirectStrategy().sendRedirect(request, response,
                    redirectUri + "?error=account_inactive");
            return;
        }

        String code = authService.createOAuth2ExchangeCode(user.getId());

        getRedirectStrategy().sendRedirect(request, response,
                redirectUri + "?code=" + URLEncoder.encode(code, StandardCharsets.UTF_8));
    }
}
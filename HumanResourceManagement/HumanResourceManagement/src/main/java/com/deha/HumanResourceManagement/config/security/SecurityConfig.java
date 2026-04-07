package com.deha.HumanResourceManagement.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, GoogleOAuth2SuccessHandler googleOAuth2SuccessHandler, GoogleOAuth2FailureHandler googleOAuth2FailureHandler) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(googleOAuth2SuccessHandler)
                        .failureHandler(googleOAuth2FailureHandler))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder(jwtUtil))
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(JwtUtil jwtUtil) {
        return NimbusJwtDecoder.withSecretKey(jwtUtil.getSecretKey())
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        return jwtConverter;
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<String> roles = claimAsStringList(jwt, "roles");
        List<String> permissions = claimAsStringList(jwt, "permissions");

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.addAll(
                roles.stream()
                        .filter(s -> !s.isBlank())
                        .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                        .map(SimpleGrantedAuthority::new)
                        .toList()
        );
        authorities.addAll(
                permissions.stream()
                        .filter(s -> !s.isBlank())
                        .map(SimpleGrantedAuthority::new)
                        .toList()
        );
        return authorities;
    }

    private List<String> claimAsStringList(Jwt jwt, String claimName) {
        Object claim = jwt.getClaims().get(claimName);
        if (claim == null) {
            return List.of();
        }
        if (claim instanceof String value) {
            return List.of(value);
        }
        if (claim instanceof Collection<?> values) {
            return values.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }
        return List.of(claim.toString());
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(
            @Value("${spring.security.oauth2.client.registration.google.client-id}") String clientId,
            @Value("${spring.security.oauth2.client.registration.google.client-secret}") String clientSecret,
            @Value("${spring.security.oauth2.provider.google.authorization-uri}") String authorizationUri,
            @Value("${spring.security.oauth2.provider.google.token-uri}") String tokenUri,
            @Value("${spring.security.oauth2.provider.google.user-info-uri}") String userInfoUri
    ) {
        ClientRegistration google = ClientRegistration
                .withRegistrationId("google")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/google")
                .scope("email", "profile")
                .authorizationUri(authorizationUri)
                .tokenUri(tokenUri)
                .userInfoUri(userInfoUri)
                .userNameAttributeName("sub")
                .clientName("Google")
                .build();

        return new InMemoryClientRegistrationRepository(google);
    }
}
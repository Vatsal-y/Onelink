package com.snaplink.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration for JWT-based authentication via AWS Cognito.
 *
 * <p>Public endpoints: {@code GET /{code}} (redirect), {@code POST /auth/**} (login/register).
 * <p>Protected endpoints: {@code /api/**} (requires valid JWT Bearer token).
 *
 * <p>In the {@code local} profile, security can be relaxed by setting
 * {@code snaplink.security.enabled=false}.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${snaplink.security.enabled:true}")
    private boolean securityEnabled;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> {
                    if (!securityEnabled) {
                        // Local dev — allow everything
                        authz.anyRequest().permitAll();
                    } else {
                        authz
                                .requestMatchers("/auth/**").permitAll()
                                .requestMatchers("/actuator/**").permitAll()
                                .requestMatchers("/error").permitAll()
                                // Redirect endpoint — public
                                .requestMatchers("/{code:[a-zA-Z0-9\\-]{3,30}}").permitAll()
                                // API endpoints — require JWT
                                .requestMatchers("/api/**").authenticated()
                                .anyRequest().permitAll();
                    }
                });

        if (securityEnabled) {
            http.oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );
        }

        return http.build();
    }

    /**
     * Extracts the Cognito {@code sub} claim as the principal name.
     */
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        // Default extracts 'sub' claim — which is the Cognito user ID
        return converter;
    }
}

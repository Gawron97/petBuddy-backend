package com.example.petbuddybackend.config.security;

import com.example.petbuddybackend.middleware.filter.ExceptionHandlerFilter;
import com.example.petbuddybackend.utils.keycloak.KeycloakJwtAuthenticationConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.*;

@Profile("dev | prod")
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableWebSocketSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ExceptionHandlerFilter exceptionHandlerFilter;
    private final KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();

        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(tokenRepository)
                        .csrfTokenRequestHandler(requestHandler)
                        .ignoringRequestMatchers(
                                request -> request.getServletPath().startsWith("/api/caretaker")
                        )
                )
                .oauth2ResourceServer(auth ->
                        auth.jwt(token -> token.jwtAuthenticationConverter(keycloakJwtAuthenticationConverter))
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(exceptionHandlerFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthorizationManager<Message<?>> messageAuthorizationManager(MessageMatcherDelegatingAuthorizationManager.Builder messages) {
        messages
                .simpDestMatchers("/ws/**")
                    .permitAll()
                .nullDestMatcher()
                    .permitAll()
                .anyMessage()
                    .authenticated();

        return messages.build();
    }

    @Bean
    public BearerTokenResolver bearerTokenResolver() {
        return new BodyAndQueryTokenResolver();
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @Bean("csrfChannelInterceptor")
    public ChannelInterceptor noopCsrfInterceptor() {
        // Disables CSRF protection for WebSocket messages
        return new ChannelInterceptor() {
        };
    }
}

package com.example.petbuddybackend.testconfig;

import com.example.petbuddybackend.config.security.BodyAndQueryTokenResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collections;

@Profile("test-security-inject-user")
@Configuration
public class NoSecurityInjectUserConfig {

    public static String injectedUsername = "testuser";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .anyRequest()
                                .permitAll()
                )
                .addFilterBefore(new TestAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private final static class TestAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            SecurityContextHolder.getContext().setAuthentication(createTestAuthentication());
            chain.doFilter(request, response);
        }

        private Authentication createTestAuthentication() {
            return new UsernamePasswordAuthenticationToken(
                    new User(injectedUsername, "password", Collections.emptyList()),
                    "password",
                    Collections.emptyList()
            );
        }
    }

    @Bean
    public MessageMatcherDelegatingAuthorizationManager.Builder messageMatcherBuilder() {
        return new MessageMatcherDelegatingAuthorizationManager.Builder();
    }

    @Bean
    public AuthorizationManager<Message<?>> messageAuthorizationManager(MessageMatcherDelegatingAuthorizationManager.Builder messages) {
        return messages
                .anyMessage().permitAll()
                .build();
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

package com.example.petbuddybackend.testconfig;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collections;

@Profile("test-security-inject-user")
@Configuration
public class NoSecurityInjectUserConfig {

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

    final static class TestAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

        private final Authentication testAuthentication;

        public TestAuthenticationFilter() {
            this.testAuthentication = new UsernamePasswordAuthenticationToken(
                    new User("testuser", "password", Collections.emptyList()),
                    "password",
                    Collections.emptyList()
            );
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            SecurityContextHolder.getContext().setAuthentication(testAuthentication);
            chain.doFilter(request, response);
        }
    }
}

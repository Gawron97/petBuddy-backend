package com.example.petbuddybackend.filter;

import com.example.petbuddybackend.service.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Map;

import static org.mockito.Mockito.*;

public class UserRegistrationFilterTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserRegistrationFilter userRegistrationFilter;

    @Test
    void testDoFilterInternal() throws Exception {
        MockitoAnnotations.openMocks(this);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        JwtAuthenticationToken token = mock(JwtAuthenticationToken.class);
        when(request.getUserPrincipal()).thenReturn(token);
        when(token.getTokenAttributes()).thenReturn(Map.of("email", "test@example.com"));

        userRegistrationFilter.doFilterInternal(request, response, filterChain);

        ArgumentCaptor<JwtAuthenticationToken> tokenCaptor = ArgumentCaptor.forClass(JwtAuthenticationToken.class);
        verify(userService).createUserIfNotExist(tokenCaptor.capture());
        verify(filterChain).doFilter(request, response);
    }
}

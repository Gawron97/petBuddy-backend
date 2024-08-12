package com.example.petbuddybackend.filter;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import java.io.IOException;

import static org.mockito.Mockito.*;


public class ForceCsrfTokenGenerationFilterTest {

    private ForceCsrfTokenGenerationFilter filter = new ForceCsrfTokenGenerationFilter();

    @Test
    public void testDoFilterInternal() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        CsrfToken csrfToken = mock(CsrfToken.class);
        when(csrfToken.getToken()).thenReturn("mocked-token");

        request.setAttribute(CsrfToken.class.getName(), csrfToken);

        filter.doFilterInternal(request, response, filterChain);
        verify(csrfToken).getToken();
    }
}

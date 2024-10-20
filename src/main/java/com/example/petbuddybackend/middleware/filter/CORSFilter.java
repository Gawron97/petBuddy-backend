package com.example.petbuddybackend.middleware.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.IOException;

@Configuration
@EnableWebMvc
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CORSFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        if (servletResponse instanceof HttpServletResponse response) {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, PATCH, OPTIONS, HEAD");
            response.setHeader("Access-Control-Max-Age", "3600");
            response.setHeader("Access-Control-Allow-Headers", "accept-timezone,accept-role,authorization,x-xsrf-token,access-control-allow-credentials,access-control-allow-headers,access-control-allow-origin,content-type");
            if ("OPTIONS".equalsIgnoreCase((request.getMethod()))) {
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                chain.doFilter(request, response);
            }
            log.info("Added CORS headers to response using CORSFilter to request " + request.getRequestURI());
        }
    }
}

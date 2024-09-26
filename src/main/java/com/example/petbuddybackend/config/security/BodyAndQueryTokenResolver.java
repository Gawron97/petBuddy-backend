package com.example.petbuddybackend.config.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.BearerTokenErrors;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class BodyAndQueryTokenResolver implements org.springframework.security.oauth2.server.resource.web.BearerTokenResolver {

    private static final Pattern authorizationPattern = Pattern.compile(
            "^Bearer (?<token>[a-zA-Z0-9-._~+/]+=*)$",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public String resolve(HttpServletRequest request) {
        log.debug("Resolving token from request");
        Optional<String> parameterToken = resolveFromRequestParameters(request);
        Optional<String> authorizationHeaderToken = resolveFromAuthorizationHeader(request);

        if(authorizationHeaderToken.isEmpty() && parameterToken.isEmpty()) {
            return null;
        }

        if(authorizationHeaderToken.isPresent() && parameterToken.isPresent()) {
            BearerTokenError error = BearerTokenErrors.invalidRequest("Found multiple bearer tokens in the request");
            throw new OAuth2AuthenticationException(error);
        }

        return authorizationHeaderToken.orElseGet(parameterToken::get);
    }

    private Optional<String> resolveFromAuthorizationHeader(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (!StringUtils.startsWithIgnoreCase(authorization, "bearer")) {
            return Optional.empty();
        }

        Matcher matcher = authorizationPattern.matcher(authorization);

        if (!matcher.matches()) {
            BearerTokenError error = BearerTokenErrors.invalidToken("Bearer token is malformed");
            throw new OAuth2AuthenticationException(error);
        }

        return Optional.ofNullable(matcher.group("token"));
    }

    private Optional<String> resolveFromRequestParameters(HttpServletRequest request) {
        String[] values = request.getParameterValues("token");

        if (values == null || values.length == 0) {
            return Optional.empty();
        }

        if (values.length == 1) {
            return Optional.ofNullable(values[0]);
        }

        BearerTokenError error = BearerTokenErrors.invalidRequest("Found multiple bearer tokens in the request");
        throw new OAuth2AuthenticationException(error);
    }
}
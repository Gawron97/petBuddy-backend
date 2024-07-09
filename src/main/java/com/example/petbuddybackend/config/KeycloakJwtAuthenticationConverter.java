package com.example.petbuddybackend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@Slf4j
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String RESOURCE_ACCESS = "resource_access";

    @Value("${spring.keycloak.resource-roles-claim}")
    private String resourceRolesClaim;

    private static final String ROLES = "roles";

    @Value("${spring.keycloak.claim-name}")
    private String claimName;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {

        return new JwtAuthenticationToken(
                jwt,
                Stream.concat(
                     new JwtGrantedAuthoritiesConverter().convert(jwt).stream(),
                     extractResourceRoles(jwt).stream()
                ).collect(Collectors.toSet()),
                getPrincipleClaimName(jwt)
        );

    }

    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {

        Map<String, Object> resourceAccess;
        Map<String, Object> resource;
        List<String> roles;

        if(jwt.getClaim(RESOURCE_ACCESS) == null) {
            return Set.of();
        }
        resourceAccess = jwt.getClaim(RESOURCE_ACCESS);

        if(resourceAccess.get(resourceRolesClaim) == null) {
            return Set.of();
        }
        resource = (Map<String, Object>) resourceAccess.get(resourceRolesClaim);
        roles = (List<String>) resource.get(ROLES);

        log.info("Roles extracted from jwt");
        log.trace("Extracted roles: {}", roles);

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());

    }

    private String getPrincipleClaimName(Jwt jwt) {

        if (claimName == null) {
            claimName = JwtClaimNames.SUB;
        }
        return jwt.getClaim(claimName);

    }

}

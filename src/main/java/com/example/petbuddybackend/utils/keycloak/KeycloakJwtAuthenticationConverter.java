package com.example.petbuddybackend.utils.keycloak;

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

    public static final String RESOURCE_ACCESS = "resource_access";
    public static final String ROLES = "roles";

    @Value("${keycloak.resource-roles-claim}")
    private String RESOURCE_ROLES_CLAIM;

    @Value("${keycloak.claim-name:" + JwtClaimNames.SUB + "}")
    private String CLAIM_NAME;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {

        return new JwtAuthenticationToken(
                jwt,
                generateAuthorities(jwt),
                getPrincipleClaimName(jwt)
        );
    }

    private Set<GrantedAuthority> generateAuthorities(Jwt jwt) {

        Stream<GrantedAuthority> stream = Stream.concat(
                new JwtGrantedAuthoritiesConverter().convert(jwt).stream(),
                extractResourceRoles(jwt).stream()
        );

        return stream.collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {

        if(!(jwt.getClaim(RESOURCE_ACCESS) instanceof Map)) {
            return Collections.emptySet();
        }

        Map<String, Object> resourceAccess = jwt.getClaim(RESOURCE_ACCESS);

        if(!(resourceAccess.get(RESOURCE_ROLES_CLAIM) instanceof Map)) {
            return Collections.emptySet();
        }

        Map<String, Object> resource = (Map<String, Object>) resourceAccess.get(RESOURCE_ROLES_CLAIM);

        if(!(resource.get(ROLES) instanceof List)) {
            return Collections.emptySet();
        }

        List<String> roles = (List<String>) resource.get(ROLES);

        log.info("Roles extracted from jwt");
        log.trace("Extracted roles: {}", roles);

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());

    }

    private String getPrincipleClaimName(Jwt jwt) {
        return jwt.getClaim(CLAIM_NAME);
    }
}

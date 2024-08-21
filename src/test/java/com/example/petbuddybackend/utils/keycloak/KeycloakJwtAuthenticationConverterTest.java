package com.example.petbuddybackend.utils.keycloak;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.example.petbuddybackend.utils.keycloak.KeycloakJwtAuthenticationConverter.RESOURCE_ACCESS;
import static com.example.petbuddybackend.utils.keycloak.KeycloakJwtAuthenticationConverter.ROLES;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class KeycloakJwtAuthenticationConverterTest {

    private static String RESOURCE_ROLES_CLAIM = "pet-buddy-client";

    @Autowired
    private KeycloakJwtAuthenticationConverter converter;


    @ParameterizedTest
    @MethodSource("jwtProvider")
    void testConvert_fullConversion(Jwt jwt, int expectedRoleCount) {
        AbstractAuthenticationToken token = converter.convert(jwt);

        assertNotNull(token);
        assertEquals(expectedRoleCount, token.getAuthorities().size());
    }


    private static Stream<Arguments> jwtProvider() {
        return Stream.of(
                Arguments.of(
                        jwtWithTokenValue(
                                RESOURCE_ACCESS,
                                Map.of(RESOURCE_ROLES_CLAIM, Map.of(ROLES, List.of("role1", "role2")))
                        ), 2
                ),
                Arguments.of(
                        jwtWithTokenValue(
                                RESOURCE_ACCESS,
                                Map.of(RESOURCE_ROLES_CLAIM, Map.of(ROLES, Collections.emptyMap()))
                        ), 0
                ),
                Arguments.of(
                        jwtWithTokenValue(
                                RESOURCE_ACCESS,
                                "not a map"
                        ), 0
                ),
                Arguments.of(
                        jwtWithTokenValue(
                                RESOURCE_ACCESS,
                                Collections.emptyMap()
                        ), 0
                ),
                Arguments.of(

                        jwtWithTokenValue(
                                RESOURCE_ACCESS,
                                Map.of(RESOURCE_ROLES_CLAIM, "not a map")
                        ), 0
                )
        );
    }

    private static Jwt jwtWithTokenValue(String claimName, Object claimValue) {
        return Jwt.withTokenValue("token")
                .header("header", "value")
                .claim(claimName, claimValue)
                .build();
    }
}

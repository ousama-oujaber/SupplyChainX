package com.protocol.supplychainx.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Converts Keycloak JWT tokens to Spring Security Authentication tokens.
 * Extracts roles from both realm_access.roles and resource_access claims.
 */
@Component
public class KeycloakJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = Stream.concat(
                jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
                extractRoles(jwt).stream()
        ).collect(Collectors.toSet());

        return new JwtAuthenticationToken(jwt, authorities, getPrincipalName(jwt));
    }

    /**
     * Extract roles from Keycloak JWT token.
     * Looks in: realm_access.roles, resource_access.{client}.roles, and top-level roles claim
     */
    private Collection<GrantedAuthority> extractRoles(Jwt jwt) {
        Set<GrantedAuthority> roles = new HashSet<>();

        // Extract from realm_access.roles (standard Keycloak location)
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            @SuppressWarnings("unchecked")
            List<String> realmRoles = (List<String>) realmAccess.get("roles");
            realmRoles.forEach(role -> roles.add(new SimpleGrantedAuthority("ROLE_" + role)));
        }

        // Extract from resource_access.supplychainx-api.roles
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get("supplychainx-api");
            if (clientAccess != null && clientAccess.containsKey("roles")) {
                @SuppressWarnings("unchecked")
                List<String> clientRoles = (List<String>) clientAccess.get("roles");
                clientRoles.forEach(role -> roles.add(new SimpleGrantedAuthority("ROLE_" + role)));
            }
        }

        // Extract from top-level 'roles' claim (custom mapper)
        Object rolesObj = jwt.getClaim("roles");
        if (rolesObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> topLevelRoles = (List<String>) rolesObj;
            topLevelRoles.forEach(role -> roles.add(new SimpleGrantedAuthority("ROLE_" + role)));
        }

        return roles;
    }

    /**
     * Get the principal name from the JWT token.
     */
    private String getPrincipalName(Jwt jwt) {
        String principalName = jwt.getClaim("preferred_username");
        if (principalName == null) {
            principalName = jwt.getClaim("email");
        }
        if (principalName == null) {
            principalName = jwt.getSubject();
        }
        return principalName;
    }
}

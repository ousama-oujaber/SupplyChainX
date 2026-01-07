package com.protocol.supplychainx.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/keycloak")
@Tag(name = "Keycloak Authentication", description = "Keycloak authentication and user info endpoints")
public class KeycloakAuthController {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @GetMapping("/user-info")
    @Operation(summary = "Get current user info", description = "Get authenticated user information from JWT token")
    public Map<String, Object> getUserInfo(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", jwt.getClaim("preferred_username"));
        userInfo.put("email", jwt.getClaim("email"));
        userInfo.put("name", jwt.getClaim("name"));
        userInfo.put("sub", jwt.getSubject());
        userInfo.put("roles", jwt.getClaim("realm_access"));
        return userInfo;
    }

    @GetMapping("/auth-config")
    @Operation(summary = "Get Keycloak configuration", description = "Get Keycloak server URLs for frontend configuration")
    public Map<String, String> getAuthConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("issuerUri", issuerUri);
        config.put("tokenEndpoint", issuerUri + "/protocol/openid-connect/token");
        config.put("authEndpoint", issuerUri + "/protocol/openid-connect/auth");
        config.put("logoutEndpoint", issuerUri + "/protocol/openid-connect/logout");
        return config;
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Logout from Keycloak (requires frontend to clear token)")
    public Map<String, String> logout() {
        // In a stateless JWT setup, logout is handled client-side by removing the token
        // Optionally, you can call Keycloak's logout endpoint from the frontend
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout successful. Please clear your token on the client side.");
        response.put("logoutUrl", issuerUri + "/protocol/openid-connect/logout");
        return response;
    }
}

package com.protocol.supplychainx.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for Keycloak OAuth2 JWT authentication.
 * 
 * Role-based access control:
 * - ADMIN: Full access to all endpoints
 * - Module-specific roles: Access to their respective modules
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final KeycloakJwtConverter keycloakJwtConverter;

    // Role constants for clarity
    private static final String ADMIN = "ADMIN";
    
    // Procurement roles
    private static final String GESTIONNAIRE_APPROVISIONNEMENT = "GESTIONNAIRE_APPROVISIONNEMENT";
    private static final String RESPONSABLE_ACHATS = "RESPONSABLE_ACHATS";
    
    // Production roles
    private static final String CHEF_PRODUCTION = "CHEF_PRODUCTION";
    private static final String PLANIFICATEUR = "PLANIFICATEUR";
    private static final String SUPERVISEUR_PRODUCTION = "SUPERVISEUR_PRODUCTION";
    
    // Delivery roles
    private static final String GESTIONNAIRE_COMMERCIAL = "GESTIONNAIRE_COMMERCIAL";
    private static final String RESPONSABLE_LOGISTIQUE = "RESPONSABLE_LOGISTIQUE";
    private static final String SUPERVISEUR_LIVRAISONS = "SUPERVISEUR_LIVRAISONS";
    private static final String SUPERVISEUR_LOGISTIQUE = "SUPERVISEUR_LOGISTIQUE";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Dynamically read allowed origins from application.properties
        // Format: comma-separated list of origins
        // Example: http://localhost:4200,https://yourdomain.com
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for stateless API
                .csrf(AbstractHttpConfigurer::disable)
                
                // Enable CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // Stateless session management
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // ===== PUBLIC ENDPOINTS =====
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api-docs/**",
                                "/v3/api-docs/**",
                                "/actuator/health",
                                "/actuator/info",
                                "/error"
                        ).permitAll()
                        
                        // ===== USER MANAGEMENT - ADMIN ONLY =====
                        .requestMatchers("/api/users/**").hasRole(ADMIN)
                        
                        // ===== PROCUREMENT MODULE =====
                        // Write operations (POST, PUT, DELETE)
                        .requestMatchers(HttpMethod.POST, "/api/procurement/**")
                            .hasAnyRole(ADMIN, GESTIONNAIRE_APPROVISIONNEMENT, RESPONSABLE_ACHATS)
                        .requestMatchers(HttpMethod.PUT, "/api/procurement/**")
                            .hasAnyRole(ADMIN, GESTIONNAIRE_APPROVISIONNEMENT, RESPONSABLE_ACHATS)
                        .requestMatchers(HttpMethod.PATCH, "/api/procurement/**")
                            .hasAnyRole(ADMIN, GESTIONNAIRE_APPROVISIONNEMENT, RESPONSABLE_ACHATS)
                        .requestMatchers(HttpMethod.DELETE, "/api/procurement/**")
                            .hasAnyRole(ADMIN, RESPONSABLE_ACHATS)
                        // Read operations (GET)
                        .requestMatchers(HttpMethod.GET, "/api/procurement/**")
                            .hasAnyRole(ADMIN, GESTIONNAIRE_APPROVISIONNEMENT, RESPONSABLE_ACHATS, 
                                       SUPERVISEUR_LOGISTIQUE, CHEF_PRODUCTION, PLANIFICATEUR, 
                                       SUPERVISEUR_PRODUCTION, RESPONSABLE_LOGISTIQUE)
                        
                        // ===== PRODUCTION MODULE =====
                        // Write operations (POST, PUT, DELETE)
                        .requestMatchers(HttpMethod.POST, "/api/production/**")
                            .hasAnyRole(ADMIN, CHEF_PRODUCTION, PLANIFICATEUR, SUPERVISEUR_PRODUCTION)
                        .requestMatchers(HttpMethod.PUT, "/api/production/**")
                            .hasAnyRole(ADMIN, CHEF_PRODUCTION, PLANIFICATEUR, SUPERVISEUR_PRODUCTION)
                        .requestMatchers(HttpMethod.PATCH, "/api/production/**")
                            .hasAnyRole(ADMIN, CHEF_PRODUCTION, PLANIFICATEUR, SUPERVISEUR_PRODUCTION)
                        .requestMatchers(HttpMethod.DELETE, "/api/production/**")
                            .hasAnyRole(ADMIN, CHEF_PRODUCTION)
                        // Read operations (GET)
                        .requestMatchers(HttpMethod.GET, "/api/production/**")
                            .hasAnyRole(ADMIN, CHEF_PRODUCTION, PLANIFICATEUR, 
                                       SUPERVISEUR_PRODUCTION, GESTIONNAIRE_COMMERCIAL)
                        
                        // ===== DELIVERY MODULE =====
                        // Write operations (POST, PUT, DELETE)
                        .requestMatchers(HttpMethod.POST, "/api/delivery/**")
                            .hasAnyRole(ADMIN, GESTIONNAIRE_COMMERCIAL, RESPONSABLE_LOGISTIQUE, SUPERVISEUR_LIVRAISONS)
                        .requestMatchers(HttpMethod.PUT, "/api/delivery/**")
                            .hasAnyRole(ADMIN, GESTIONNAIRE_COMMERCIAL, RESPONSABLE_LOGISTIQUE, SUPERVISEUR_LIVRAISONS)
                        .requestMatchers(HttpMethod.PATCH, "/api/delivery/**")
                            .hasAnyRole(ADMIN, GESTIONNAIRE_COMMERCIAL, RESPONSABLE_LOGISTIQUE, SUPERVISEUR_LIVRAISONS)
                        .requestMatchers(HttpMethod.DELETE, "/api/delivery/**")
                            .hasAnyRole(ADMIN, RESPONSABLE_LOGISTIQUE)
                        // Read operations (GET)
                        .requestMatchers(HttpMethod.GET, "/api/delivery/**")
                            .hasAnyRole(ADMIN, SUPERVISEUR_LOGISTIQUE, GESTIONNAIRE_COMMERCIAL, 
                                       RESPONSABLE_LOGISTIQUE, SUPERVISEUR_LIVRAISONS)
                        
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                
                // Configure OAuth2 Resource Server with Keycloak JWT
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtConverter))
                );

        return http.build();
    }
}

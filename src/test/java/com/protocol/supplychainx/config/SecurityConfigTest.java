package com.protocol.supplychainx.config;

import com.protocol.supplychainx.common.enums.RoleUtilisateur;
import com.protocol.supplychainx.production.controller.ProductController;
import com.protocol.supplychainx.production.service.IProductService;
import com.protocol.supplychainx.user.entity.User;
import com.protocol.supplychainx.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import({SecurityConfig.class, CustomUserDetailsService.class})
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private IProductService productService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldDenyAccessToProtectedEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/production/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAccessWithValidCredentials() throws Exception {
        // Mock user
        User user = new User();
        user.setEmail("admin@test.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(RoleUtilisateur.ADMIN);

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/production/products")
                        .with(httpBasic("admin@test.com", "password")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyAccessWithInvalidCredentials() throws Exception {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/production/products")
                        .with(httpBasic("wrong@test.com", "wrong")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user", roles = "GESTIONNAIRE_APPROVISIONNEMENT")
    void shouldDenyAccessToAdminEndpointWithInsufficientRole() throws Exception {
        // Product creation requires ADMIN or CHEF_PRODUCTION
        mockMvc.perform(post("/api/production/products")
                        .contentType("application/json")
                        .content("{\"name\": \"Test Product\", \"productionTime\": 10, \"cost\": 100.0, \"stock\": 50}"))
                .andExpect(status().isForbidden());
    }
}

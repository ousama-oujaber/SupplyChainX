package com.protocol.supplychainx.production.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.protocol.supplychainx.config.aop.SecurityAspect;
import com.protocol.supplychainx.production.dto.ProductDTO;
import com.protocol.supplychainx.production.service.IProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = ProductController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = SecurityAspect.class
    )
)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "admin", roles = {"ADMIN"})
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IProductService productService;

    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        productDTO = ProductDTO.builder()
                .idProduct(1L)
                .name("Test Widget")
                .productionTime(5)
                .cost(100.0)
                .stock(50)
                .build();
    }

    @Test
    @DisplayName("Should create product successfully")
    void testCreateProduct() throws Exception {
        when(productService.createProduct(any(ProductDTO.class))).thenReturn(productDTO);

        mockMvc.perform(post("/api/production/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Widget"))
                .andExpect(jsonPath("$.cost").value(100.0));

        verify(productService, times(1)).createProduct(any(ProductDTO.class));
    }

    @Test
    @DisplayName("Should update product successfully")
    void testUpdateProduct() throws Exception {
        when(productService.updateProduct(eq(1L), any(ProductDTO.class))).thenReturn(productDTO);

        mockMvc.perform(put("/api/production/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Widget"));

        verify(productService, times(1)).updateProduct(eq(1L), any(ProductDTO.class));
    }

    @Test
    @DisplayName("Should get product by ID")
    void testGetProductById() throws Exception {
        when(productService.getProductById(1L)).thenReturn(productDTO);

        mockMvc.perform(get("/api/production/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Widget"))
                .andExpect(jsonPath("$.idProduct").value(1));

        verify(productService, times(1)).getProductById(1L);
    }

    @Test
    @DisplayName("Should get all products with pagination")
    void testGetAllProducts() throws Exception {
        Page<ProductDTO> page = new PageImpl<>(Arrays.asList(productDTO), PageRequest.of(0, 10), 1);
        when(productService.getAllProducts(any())).thenReturn(page);

        mockMvc.perform(get("/api/production/products")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Test Widget"));

        verify(productService, times(1)).getAllProducts(any());
    }

    @Test
    @DisplayName("Should search products by name")
    void testSearchProductsByName() throws Exception {
        Page<ProductDTO> page = new PageImpl<>(Arrays.asList(productDTO), PageRequest.of(0, 10), 1);
        when(productService.searchProductsByName(anyString(), any())).thenReturn(page);

        mockMvc.perform(get("/api/production/products/search")
                .param("name", "Widget")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Test Widget"));

        verify(productService, times(1)).searchProductsByName(anyString(), any());
    }

    @Test
    @DisplayName("Should delete product successfully")
    void testDeleteProduct() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/production/products/1"))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct(1L);
    }
}


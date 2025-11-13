package com.protocol.supplychainx.integration;

import com.protocol.supplychainx.common.exceptions.production.ProductNotFoundException;
import com.protocol.supplychainx.production.dto.ProductDTO;
import com.protocol.supplychainx.production.service.IProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductServiceIntegrationTest {

    @Autowired
    private IProductService productService;

    @MockBean
    private JavaMailSender javaMailSender;

    @Test
    @DisplayName("Integration: Should create and retrieve product successfully")
    void testCreateAndRetrieveProduct() {
        // Arrange
        ProductDTO productDTO = ProductDTO.builder()
                .name("Integration Product")
                .productionTime(10)
                .cost(250.0)
                .stock(100)
                .build();

        // Act
        ProductDTO createdProduct = productService.createProduct(productDTO);
        ProductDTO retrievedProduct = productService.getProductById(createdProduct.getIdProduct());

        // Assert
        assertNotNull(createdProduct.getIdProduct());
        assertEquals("Integration Product", retrievedProduct.getName());
        assertEquals(250.0, retrievedProduct.getCost());
    }

    @Test
    @DisplayName("Integration: Should update product successfully")
    void testUpdateProduct() {
        // Arrange
        ProductDTO productDTO = ProductDTO.builder()
                .name("Update Test Product")
                .productionTime(5)
                .cost(100.0)
                .stock(50)
                .build();

        ProductDTO createdProduct = productService.createProduct(productDTO);

        // Act
        createdProduct.setName("Updated Product Name");
        createdProduct.setCost(150.0);
        ProductDTO updatedProduct = productService.updateProduct(createdProduct.getIdProduct(), createdProduct);

        // Assert
        assertEquals("Updated Product Name", updatedProduct.getName());
        assertEquals(150.0, updatedProduct.getCost());
    }

    @Test
    @DisplayName("Integration: Should search products by name")
    void testSearchProductsByName() {
        // Arrange
        ProductDTO product1 = ProductDTO.builder()
                .name("SearchTest Widget 1")
                .productionTime(5)
                .cost(100.0)
                .stock(50)
                .build();

        ProductDTO product2 = ProductDTO.builder()
                .name("SearchTest Widget 2")
                .productionTime(7)
                .cost(150.0)
                .stock(30)
                .build();

        productService.createProduct(product1);
        productService.createProduct(product2);

        // Act
        Page<ProductDTO> results = productService.searchProductsByName("SearchTest", PageRequest.of(0, 10));

        // Assert
        assertNotNull(results);
        assertTrue(results.getContent().size() >= 2);
    }

    @Test
    @DisplayName("Integration: Should delete product successfully")
    void testDeleteProduct() {
        // Arrange
        ProductDTO productDTO = ProductDTO.builder()
                .name("Delete Test Product")
                .productionTime(5)
                .cost(100.0)
                .stock(50)
                .build();

        ProductDTO createdProduct = productService.createProduct(productDTO);

        // Act
        productService.deleteProduct(createdProduct.getIdProduct());

        // Assert
        assertThrows(ProductNotFoundException.class, () -> 
            productService.getProductById(createdProduct.getIdProduct())
        );
    }

    @Test
    @DisplayName("Integration: Should check if product exists by name")
    void testExistsByName() {
        // Arrange
        ProductDTO productDTO = ProductDTO.builder()
                .name("Unique Product Name")
                .productionTime(5)
                .cost(100.0)
                .stock(50)
                .build();

        productService.createProduct(productDTO);

        // Act
        boolean exists = productService.existsByName("Unique Product Name");
        boolean notExists = productService.existsByName("Non-existent Product");

        // Assert
        assertTrue(exists);
        assertFalse(notExists);
    }
}


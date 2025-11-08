package com.protocol.supplychainx.production.service;

import com.protocol.supplychainx.production.dto.ProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IProductService {
    
    ProductDTO createProduct(ProductDTO productDTO);
    
    ProductDTO updateProduct(Long id, ProductDTO productDTO);
    
    ProductDTO getProductById(Long id);
    
    Page<ProductDTO> getAllProducts(Pageable pageable);
    
    Page<ProductDTO> searchProductsByName(String name, Pageable pageable);
    
    void deleteProduct(Long id);
    
    boolean existsByName(String name);
}

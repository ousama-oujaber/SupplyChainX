package com.protocol.supplychainx.production.service.impl;

import com.protocol.supplychainx.common.enums.ProductionOrderStatus;
import com.protocol.supplychainx.common.exceptions.production.ProductHasActiveOrdersException;
import com.protocol.supplychainx.common.exceptions.production.ProductNotFoundException;
import com.protocol.supplychainx.production.dto.ProductDTO;
import com.protocol.supplychainx.production.entity.Product;
import com.protocol.supplychainx.production.mapper.ProductMapper;
import com.protocol.supplychainx.production.repository.ProductRepository;
import com.protocol.supplychainx.production.repository.ProductionOrderRepository;
import com.protocol.supplychainx.production.service.IProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService implements IProductService {

    private final ProductRepository productRepository;
    private final ProductionOrderRepository productionOrderRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.info("Creating new product with name: {}", productDTO.getName());

        Product product = productMapper.toEntity(productDTO);
        Product savedProduct = productRepository.save(product);

        log.info("Product created successfully with ID: {}", savedProduct.getIdProduct());
        return productMapper.toDTO(savedProduct);
    }

    @Override
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        log.info("Updating product with ID: {}", id);

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        existingProduct.setName(productDTO.getName());
        existingProduct.setProductionTime(productDTO.getProductionTime());
        existingProduct.setCost(productDTO.getCost());
        existingProduct.setStock(productDTO.getStock());

        Product updatedProduct = productRepository.save(existingProduct);

        log.info("Product updated successfully with ID: {}", id);
        return productMapper.toDTO(updatedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        log.info("Fetching product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        return productMapper.toDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        log.info("Fetching all products with pagination");

        Page<Product> products = productRepository.findAll(pageable);
        return products.map(productMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> searchProductsByName(String name, Pageable pageable) {
        log.info("Searching products by name: {}", name);

        Page<Product> products = productRepository.findByNameContainingIgnoreCase(name, pageable);
        return products.map(productMapper::toDTO);
    }

    @Override
    public void deleteProduct(Long id) {
        log.info("Attempting to delete product with ID: {}", id);

        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }

        long activeOrdersCount = productionOrderRepository.countByProductIdProductAndStatusIn(
                id,
                Arrays.asList(ProductionOrderStatus.EN_ATTENTE, ProductionOrderStatus.EN_PRODUCTION)
        );

        if (activeOrdersCount > 0) {
            log.error("Cannot delete product with ID: {}. Has {} active production order(s)", id, activeOrdersCount);
            throw new ProductHasActiveOrdersException(id, (int) activeOrdersCount);
        }

        productRepository.deleteById(id);
        log.info("Product deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return productRepository.existsByName(name);
    }
}

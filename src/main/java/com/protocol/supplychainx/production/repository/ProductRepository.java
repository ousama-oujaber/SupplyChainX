package com.protocol.supplychainx.production.repository;

import com.protocol.supplychainx.production.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByName(String name);
    boolean existsByName(String name);
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
}

package com.protocol.supplychainx.production.repository;

import com.protocol.supplychainx.production.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByName(String name);
    boolean existsByName(String name);

    @Query(
        value = "SELECT p FROM Product p WHERE UPPER(p.name) LIKE UPPER(CONCAT('%', :pattern, '%')) ESCAPE '|'",
        countQuery = "SELECT COUNT(p) FROM Product p WHERE UPPER(p.name) LIKE UPPER(CONCAT('%', :pattern, '%')) ESCAPE '|'"
    )
    Page<Product> findByNameContainingIgnoreCaseInternal(@Param("pattern") String pattern, Pageable pageable);

    default Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable) {
        return findByNameContainingIgnoreCaseInternal(escapeLikePattern(name), pageable);
    }

    private static String escapeLikePattern(String term) {
        if (term == null || term.isEmpty()) {
            return "";
        }
        return term
                .replace("|", "||")
                .replace("%", "|%")
                .replace("_", "|_");
    }
}

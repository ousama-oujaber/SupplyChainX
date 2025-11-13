package com.protocol.supplychainx.procurement.repository;

import com.protocol.supplychainx.common.enums.SupplyOrderStatus;
import com.protocol.supplychainx.procurement.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    
    Optional<Supplier> findByName(String name);
    
    boolean existsByName(String name);

    @Query(
        value = "SELECT s FROM Supplier s WHERE UPPER(s.name) LIKE UPPER(CONCAT('%', :pattern, '%')) ESCAPE '|'",
        countQuery = "SELECT COUNT(s) FROM Supplier s WHERE UPPER(s.name) LIKE UPPER(CONCAT('%', :pattern, '%')) ESCAPE '|'"
    )
    Page<Supplier> findByNameContainingIgnoreCaseInternal(@Param("pattern") String pattern, Pageable pageable);

    default Page<Supplier> findByNameContainingIgnoreCase(String name, Pageable pageable) {
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
    @Query("SELECT COUNT(o) FROM SupplyOrder o WHERE o.supplier.idSupplier = :supplierId AND o.status IN :statuses")
    long countActiveOrdersBySupplier(@Param("supplierId") Long supplierId, 
                                     @Param("statuses") List<SupplyOrderStatus> statuses);
}

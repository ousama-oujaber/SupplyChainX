package com.protocol.supplychainx.delivery.repository;

import com.protocol.supplychainx.common.enums.CustomerOrderStatus;
import com.protocol.supplychainx.delivery.entity.CustomerOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    Page<CustomerOrder> findByCustomerIdCustomer(Long customerId, Pageable pageable);
    Page<CustomerOrder> findByStatus(CustomerOrderStatus status, Pageable pageable);
    Page<CustomerOrder> findByProductIdProduct(Long productId, Pageable pageable);
    @Query("SELECT COUNT(o) FROM CustomerOrder o WHERE o.product.idProduct = :productId AND o.status IN :statuses")
    long countActiveOrdersByProduct(@Param("productId") Long productId, 
                                   @Param("statuses") List<CustomerOrderStatus> statuses);
}

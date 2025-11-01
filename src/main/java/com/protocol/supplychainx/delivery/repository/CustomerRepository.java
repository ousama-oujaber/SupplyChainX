package com.protocol.supplychainx.delivery.repository;

import com.protocol.supplychainx.common.enums.CustomerOrderStatus;
import com.protocol.supplychainx.delivery.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByName(String name);
    boolean existsByName(String name);
    Page<Customer> findByNameContainingIgnoreCase(String name, Pageable pageable);
    @Query("SELECT COUNT(o) FROM CustomerOrder o WHERE o.customer.idCustomer = :customerId AND o.status IN :statuses")
    long countActiveOrdersByCustomer(@Param("customerId") Long customerId, 
                                     @Param("statuses") List<CustomerOrderStatus> statuses);
}

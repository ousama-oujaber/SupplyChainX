package com.protocol.supplychainx.delivery.repository;

import com.protocol.supplychainx.common.enums.DeliveryStatus;
import com.protocol.supplychainx.delivery.entity.Delivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByOrderIdOrder(Long orderId);
    Page<Delivery> findByStatus(DeliveryStatus status, Pageable pageable);
    Page<Delivery> findByDeliveryDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    Page<Delivery> findByDriverContainingIgnoreCase(String driver, Pageable pageable);
}

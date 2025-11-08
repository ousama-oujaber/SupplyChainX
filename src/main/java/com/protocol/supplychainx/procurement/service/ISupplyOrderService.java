package com.protocol.supplychainx.procurement.service;

import com.protocol.supplychainx.common.enums.SupplyOrderStatus;
import com.protocol.supplychainx.procurement.dto.SupplyOrderDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ISupplyOrderService {
    SupplyOrderDTO createSupplyOrder(SupplyOrderDTO supplyOrderDTO);
    SupplyOrderDTO updateSupplyOrder(Long id, SupplyOrderDTO supplyOrderDTO);
    SupplyOrderDTO getSupplyOrder(Long id);
    Page<SupplyOrderDTO> getAllSupplyOrders(Pageable pageable);
    Page<SupplyOrderDTO> getSupplyOrdersByStatus(SupplyOrderStatus status, Pageable pageable);
    Page<SupplyOrderDTO> getSupplyOrdersBySupplier(Long supplierId, Pageable pageable);
    void deleteSupplyOrder(Long id);
    SupplyOrderDTO updateOrderStatus(Long id, SupplyOrderStatus status);
}

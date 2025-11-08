package com.protocol.supplychainx.procurement.controller;

import com.protocol.supplychainx.common.enums.SupplyOrderStatus;
import com.protocol.supplychainx.procurement.dto.SupplyOrderDTO;
import com.protocol.supplychainx.procurement.service.ISupplyOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/procurement/supply-orders")
@RequiredArgsConstructor
@Tag(name = "Supply Order Management", description = "APIs for managing supply orders in the procurement module")
public class SupplyOrderController {

    private final ISupplyOrderService supplyOrderService;

    @PostMapping
    @Operation(summary = "Create a new supply order", description = "Create a supply order for a supplier (US13)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Supply order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Supplier or material not found")
    })
    public ResponseEntity<SupplyOrderDTO> createSupplyOrder(@Valid @RequestBody SupplyOrderDTO supplyOrderDTO) {
        SupplyOrderDTO createdOrder = supplyOrderService.createSupplyOrder(supplyOrderDTO);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing supply order", description = "Modify supply order information (US14)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Supply order updated successfully"),
            @ApiResponse(responseCode = "404", description = "Supply order not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<SupplyOrderDTO> updateSupplyOrder(
            @Parameter(description = "Supply order ID") @PathVariable Long id,
            @Valid @RequestBody SupplyOrderDTO supplyOrderDTO) {
        SupplyOrderDTO updatedOrder = supplyOrderService.updateSupplyOrder(id, supplyOrderDTO);
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supply order by ID", description = "Retrieve supply order details by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Supply order found"),
            @ApiResponse(responseCode = "404", description = "Supply order not found")
    })
    public ResponseEntity<SupplyOrderDTO> getSupplyOrderById(
            @Parameter(description = "Supply order ID") @PathVariable Long id) {
        SupplyOrderDTO order = supplyOrderService.getSupplyOrder(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    @Operation(summary = "Get all supply orders", description = "Retrieve paginated list of all supply orders (US16)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Supply orders retrieved successfully")
    })
    public ResponseEntity<Page<SupplyOrderDTO>> getAllSupplyOrders(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "idOrder") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)") @RequestParam(defaultValue = "DESC") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<SupplyOrderDTO> orders = supplyOrderService.getAllSupplyOrders(pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/by-status")
    @Operation(summary = "Get supply orders by status", description = "Retrieve supply orders filtered by status (US17)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Supply orders retrieved successfully")
    })
    public ResponseEntity<Page<SupplyOrderDTO>> getSupplyOrdersByStatus(
            @Parameter(description = "Order status") @RequestParam SupplyOrderStatus status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderDate"));
        Page<SupplyOrderDTO> orders = supplyOrderService.getSupplyOrdersByStatus(status, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/by-supplier/{supplierId}")
    @Operation(summary = "Get supply orders by supplier", description = "Retrieve all supply orders for a specific supplier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Supply orders retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Supplier not found")
    })
    public ResponseEntity<Page<SupplyOrderDTO>> getSupplyOrdersBySupplier(
            @Parameter(description = "Supplier ID") @PathVariable Long supplierId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderDate"));
        Page<SupplyOrderDTO> orders = supplyOrderService.getSupplyOrdersBySupplier(supplierId, pageable);
        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update supply order status", description = "Update the status of a supply order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Supply order not found")
    })
    public ResponseEntity<SupplyOrderDTO> updateOrderStatus(
            @Parameter(description = "Supply order ID") @PathVariable Long id,
            @Parameter(description = "New status") @RequestParam SupplyOrderStatus status) {
        SupplyOrderDTO updatedOrder = supplyOrderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a supply order", description = "Delete a supply order if not yet delivered (US15)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Supply order deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Supply order not found"),
            @ApiResponse(responseCode = "409", description = "Supply order cannot be deleted (already delivered)")
    })
    public ResponseEntity<Void> deleteSupplyOrder(
            @Parameter(description = "Supply order ID") @PathVariable Long id) {
        supplyOrderService.deleteSupplyOrder(id);
        return ResponseEntity.noContent().build();
    }
}

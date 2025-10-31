package com.protocol.supplychainx.production.controller;

import com.protocol.supplychainx.common.enums.ProductionOrderStatus;
import com.protocol.supplychainx.production.dto.ProductionOrderDTO;
import com.protocol.supplychainx.production.service.IProductionOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/production/orders")
@RequiredArgsConstructor
@Tag(name = "Production Orders", description = "Production Order management APIs")
public class ProductionOrderController {

    private final IProductionOrderService productionOrderService;

    @PostMapping
    @Operation(summary = "Create a new production order", description = "Create a production order (checks material availability)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Production order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "409", description = "Insufficient materials")
    })
    public ResponseEntity<ProductionOrderDTO> createProductionOrder(@Valid @RequestBody ProductionOrderDTO productionOrderDTO) {
        ProductionOrderDTO createdOrder = productionOrderService.createProductionOrder(productionOrderDTO);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a production order", description = "Update an existing production order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Production order updated successfully"),
            @ApiResponse(responseCode = "404", description = "Production order not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<ProductionOrderDTO> updateProductionOrder(
            @PathVariable Long id,
            @Valid @RequestBody ProductionOrderDTO productionOrderDTO) {
        ProductionOrderDTO updatedOrder = productionOrderService.updateProductionOrder(id, productionOrderDTO);
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get production order by ID", description = "Retrieve a production order by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Production order found"),
            @ApiResponse(responseCode = "404", description = "Production order not found")
    })
    public ResponseEntity<ProductionOrderDTO> getProductionOrderById(@PathVariable Long id) {
        ProductionOrderDTO productionOrder = productionOrderService.getProductionOrderById(id);
        return ResponseEntity.ok(productionOrder);
    }

    @GetMapping
    @Operation(summary = "Get all production orders", description = "Retrieve all production orders with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Production orders retrieved successfully")
    })
    public ResponseEntity<Page<ProductionOrderDTO>> getAllProductionOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductionOrderDTO> orders = productionOrderService.getAllProductionOrders(pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/by-status")
    @Operation(summary = "Get production orders by status", description = "Retrieve production orders filtered by status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Production orders found")
    })
    public ResponseEntity<Page<ProductionOrderDTO>> getProductionOrdersByStatus(
            @RequestParam ProductionOrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductionOrderDTO> orders = productionOrderService.getProductionOrdersByStatus(status, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/by-product/{productId}")
    @Operation(summary = "Get production orders by product", description = "Retrieve production orders for a specific product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Production orders found"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Page<ProductionOrderDTO>> getProductionOrdersByProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductionOrderDTO> orders = productionOrderService.getProductionOrdersByProduct(productId, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/priority")
    @Operation(summary = "Get priority production orders", description = "Retrieve all priority production orders")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Priority orders found")
    })
    public ResponseEntity<Page<ProductionOrderDTO>> getPriorityProductionOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductionOrderDTO> orders = productionOrderService.getPriorityProductionOrders(pageable);
        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update production order status", description = "Update the status of a production order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Production order not found")
    })
    public ResponseEntity<ProductionOrderDTO> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam ProductionOrderStatus status) {
        ProductionOrderDTO updatedOrder = productionOrderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a production order", description = "Cancel a production order (only EN_ATTENTE status)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Production order cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Production order not found"),
            @ApiResponse(responseCode = "409", description = "Order cannot be cancelled")
    })
    public ResponseEntity<Void> cancelProductionOrder(@PathVariable Long id) {
        productionOrderService.cancelProductionOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/calculate-time")
    @Operation(summary = "Calculate estimated production time", description = "Calculate estimated production time for a product and quantity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Time calculated successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Integer> calculateEstimatedProductionTime(
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        Integer estimatedTime = productionOrderService.calculateEstimatedProductionTime(productId, quantity);
        return ResponseEntity.ok(estimatedTime);
    }
}

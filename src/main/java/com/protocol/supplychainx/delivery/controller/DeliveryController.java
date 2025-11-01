package com.protocol.supplychainx.delivery.controller;

import com.protocol.supplychainx.delivery.dto.DeliveryDTO;
import com.protocol.supplychainx.delivery.service.IDeliveryService;
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
@RequestMapping("/api/delivery/deliveries")
@RequiredArgsConstructor
@Tag(name = "Delivery Management", description = "APIs for managing deliveries in the delivery module")
public class DeliveryController {

    private final IDeliveryService deliveryService;

    @PostMapping
    @Operation(summary = "Create a new delivery", description = "Create a new delivery for a customer order and calculate cost (US40)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Delivery created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Customer order not found")
    })
    public ResponseEntity<DeliveryDTO> createDelivery(@Valid @RequestBody DeliveryDTO deliveryDTO) {
        DeliveryDTO createdDelivery = deliveryService.createDelivery(deliveryDTO);
        return new ResponseEntity<>(createdDelivery, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing delivery", description = "Modify delivery information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delivery updated successfully"),
            @ApiResponse(responseCode = "404", description = "Delivery not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<DeliveryDTO> updateDelivery(
            @Parameter(description = "Delivery ID") @PathVariable Long id,
            @Valid @RequestBody DeliveryDTO deliveryDTO) {
        DeliveryDTO updatedDelivery = deliveryService.updateDelivery(id, deliveryDTO);
        return ResponseEntity.ok(updatedDelivery);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get delivery by ID", description = "Retrieve delivery details by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delivery found"),
            @ApiResponse(responseCode = "404", description = "Delivery not found")
    })
    public ResponseEntity<DeliveryDTO> getDeliveryById(
            @Parameter(description = "Delivery ID") @PathVariable Long id) {
        DeliveryDTO delivery = deliveryService.getDelivery(id);
        return ResponseEntity.ok(delivery);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get delivery by order ID", description = "Retrieve delivery details by customer order ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delivery found"),
            @ApiResponse(responseCode = "404", description = "Delivery not found for the given order")
    })
    public ResponseEntity<DeliveryDTO> getDeliveryByOrderId(
            @Parameter(description = "Customer Order ID") @PathVariable Long orderId) {
        DeliveryDTO delivery = deliveryService.getDeliveryByOrderId(orderId);
        return ResponseEntity.ok(delivery);
    }

    @GetMapping
    @Operation(summary = "Get all deliveries", description = "Retrieve paginated list of all deliveries")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deliveries retrieved successfully")
    })
    public ResponseEntity<Page<DeliveryDTO>> getAllDeliveries(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "idDelivery") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)") @RequestParam(defaultValue = "DESC") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<DeliveryDTO> deliveries = deliveryService.getAllDeliveries(pageable);
        return ResponseEntity.ok(deliveries);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get deliveries by status", description = "Retrieve deliveries filtered by status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deliveries retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status value")
    })
    public ResponseEntity<Page<DeliveryDTO>> getDeliveriesByStatus(
            @Parameter(description = "Delivery status (PLANIFIEE, EN_COURS, LIVREE)") @PathVariable String status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<DeliveryDTO> deliveries = deliveryService.getDeliveriesByStatus(status, pageable);
        return ResponseEntity.ok(deliveries);
    }

    @PostMapping("/{id}/calculate-cost")
    @Operation(summary = "Calculate delivery cost", description = "Calculate and update the cost for a delivery (US40)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cost calculated successfully"),
            @ApiResponse(responseCode = "404", description = "Delivery not found")
    })
    public ResponseEntity<Double> calculateDeliveryCost(
            @Parameter(description = "Delivery ID") @PathVariable Long id) {
        Double cost = deliveryService.calculateDeliveryCost(id);
        return ResponseEntity.ok(cost);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a delivery", description = "Delete a delivery")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Delivery deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Delivery not found")
    })
    public ResponseEntity<Void> deleteDelivery(
            @Parameter(description = "Delivery ID") @PathVariable Long id) {
        deliveryService.deleteDelivery(id);
        return ResponseEntity.noContent().build();
    }
}

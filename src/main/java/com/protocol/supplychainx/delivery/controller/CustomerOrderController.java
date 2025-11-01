package com.protocol.supplychainx.delivery.controller;

import com.protocol.supplychainx.delivery.dto.CustomerOrderDTO;
import com.protocol.supplychainx.delivery.service.ICustomerOrderService;
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
@RequestMapping("/api/delivery/orders")
@RequiredArgsConstructor
@Tag(name = "Customer Order Management", description = "APIs for managing customer orders in the delivery module")
public class CustomerOrderController {

    private final ICustomerOrderService customerOrderService;

    @PostMapping
    @Operation(summary = "Create a new customer order", description = "Create a new customer order (US35)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Customer order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Customer or Product not found"),
            @ApiResponse(responseCode = "409", description = "Insufficient product stock")
    })
    public ResponseEntity<CustomerOrderDTO> createCustomerOrder(@Valid @RequestBody CustomerOrderDTO customerOrderDTO) {
        CustomerOrderDTO createdOrder = customerOrderService.createCustomerOrder(customerOrderDTO);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing customer order", description = "Modify customer order information (US36)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer order updated successfully"),
            @ApiResponse(responseCode = "404", description = "Customer order not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Insufficient product stock")
    })
    public ResponseEntity<CustomerOrderDTO> updateCustomerOrder(
            @Parameter(description = "Customer Order ID") @PathVariable Long id,
            @Valid @RequestBody CustomerOrderDTO customerOrderDTO) {
        CustomerOrderDTO updatedOrder = customerOrderService.updateCustomerOrder(id, customerOrderDTO);
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer order by ID", description = "Retrieve customer order details by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer order found"),
            @ApiResponse(responseCode = "404", description = "Customer order not found")
    })
    public ResponseEntity<CustomerOrderDTO> getCustomerOrderById(
            @Parameter(description = "Customer Order ID") @PathVariable Long id) {
        CustomerOrderDTO customerOrder = customerOrderService.getCustomerOrder(id);
        return ResponseEntity.ok(customerOrder);
    }

    @GetMapping
    @Operation(summary = "Get all customer orders", description = "Retrieve paginated list of all customer orders (US38)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer orders retrieved successfully")
    })
    public ResponseEntity<Page<CustomerOrderDTO>> getAllCustomerOrders(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "idOrder") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)") @RequestParam(defaultValue = "DESC") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<CustomerOrderDTO> orders = customerOrderService.getAllCustomerOrders(pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get customer orders by customer", description = "Retrieve orders for a specific customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer orders retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<Page<CustomerOrderDTO>> getCustomerOrdersByCustomer(
            @Parameter(description = "Customer ID") @PathVariable Long customerId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<CustomerOrderDTO> orders = customerOrderService.getCustomerOrdersByCustomer(customerId, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get customer orders by status", description = "Retrieve orders filtered by status (US39)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer orders retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status value")
    })
    public ResponseEntity<Page<CustomerOrderDTO>> getCustomerOrdersByStatus(
            @Parameter(description = "Order status (EN_PREPARATION, EN_ROUTE, LIVREE)") @PathVariable String status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<CustomerOrderDTO> orders = customerOrderService.getCustomerOrdersByStatus(status, pageable);
        return ResponseEntity.ok(orders);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a customer order", description = "Cancel a customer order if not shipped (US37)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Customer order cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Customer order not found"),
            @ApiResponse(responseCode = "409", description = "Customer order cannot be cancelled (already shipped or delivered)")
    })
    public ResponseEntity<Void> cancelCustomerOrder(
            @Parameter(description = "Customer Order ID") @PathVariable Long id) {
        customerOrderService.cancelCustomerOrder(id);
        return ResponseEntity.noContent().build();
    }
}

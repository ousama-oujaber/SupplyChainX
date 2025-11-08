package com.protocol.supplychainx.production.controller;

import com.protocol.supplychainx.production.dto.BillOfMaterialDTO;
import com.protocol.supplychainx.production.service.IBillOfMaterialService;
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

import java.util.List;

@RestController
@RequestMapping("/api/production/bom")
@RequiredArgsConstructor
@Tag(name = "Bill of Materials", description = "Bill of Materials management APIs")
public class BillOfMaterialController {

    private final IBillOfMaterialService billOfMaterialService;

    @PostMapping
    @Operation(summary = "Create a new BOM entry", description = "Add a new Bill of Material entry linking product and raw material")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "BOM entry created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Product or Material not found")
    })
    public ResponseEntity<BillOfMaterialDTO> createBillOfMaterial(@Valid @RequestBody BillOfMaterialDTO billOfMaterialDTO) {
        BillOfMaterialDTO createdBOM = billOfMaterialService.createBillOfMaterial(billOfMaterialDTO);
        return new ResponseEntity<>(createdBOM, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a BOM entry", description = "Update an existing Bill of Material entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "BOM entry updated successfully"),
            @ApiResponse(responseCode = "404", description = "BOM entry not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<BillOfMaterialDTO> updateBillOfMaterial(
            @PathVariable Long id,
            @Valid @RequestBody BillOfMaterialDTO billOfMaterialDTO) {
        BillOfMaterialDTO updatedBOM = billOfMaterialService.updateBillOfMaterial(id, billOfMaterialDTO);
        return ResponseEntity.ok(updatedBOM);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get BOM entry by ID", description = "Retrieve a Bill of Material entry by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "BOM entry found"),
            @ApiResponse(responseCode = "404", description = "BOM entry not found")
    })
    public ResponseEntity<BillOfMaterialDTO> getBillOfMaterialById(@PathVariable Long id) {
        BillOfMaterialDTO billOfMaterial = billOfMaterialService.getBillOfMaterialById(id);
        return ResponseEntity.ok(billOfMaterial);
    }

    @GetMapping
    @Operation(summary = "Get all BOM entries", description = "Retrieve all Bill of Material entries with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "BOM entries retrieved successfully")
    })
    public ResponseEntity<Page<BillOfMaterialDTO>> getAllBillOfMaterials(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BillOfMaterialDTO> billOfMaterials = billOfMaterialService.getAllBillOfMaterials(pageable);
        return ResponseEntity.ok(billOfMaterials);
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get BOM entries by product", description = "Retrieve all Bill of Material entries for a specific product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "BOM entries found"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<List<BillOfMaterialDTO>> getBillOfMaterialsByProduct(@PathVariable Long productId) {
        List<BillOfMaterialDTO> billOfMaterials = billOfMaterialService.getBillOfMaterialsByProduct(productId);
        return ResponseEntity.ok(billOfMaterials);
    }

    @GetMapping("/product/{productId}/paginated")
    @Operation(summary = "Get BOM entries by product (paginated)", description = "Retrieve Bill of Material entries for a product with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "BOM entries found"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Page<BillOfMaterialDTO>> getBillOfMaterialsByProductPaginated(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BillOfMaterialDTO> billOfMaterials = billOfMaterialService.getBillOfMaterialsByProductPaginated(productId, pageable);
        return ResponseEntity.ok(billOfMaterials);
    }

    @GetMapping("/product/{productId}/check-availability")
    @Operation(summary = "Check materials availability", description = "Check if all required materials are available for production")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Availability check completed")
    })
    public ResponseEntity<Boolean> checkMaterialsAvailability(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        boolean available = billOfMaterialService.checkMaterialsAvailability(productId, quantity);
        return ResponseEntity.ok(available);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a BOM entry", description = "Delete a Bill of Material entry by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "BOM entry deleted successfully"),
            @ApiResponse(responseCode = "404", description = "BOM entry not found")
    })
    public ResponseEntity<Void> deleteBillOfMaterial(@PathVariable Long id) {
        billOfMaterialService.deleteBillOfMaterial(id);
        return ResponseEntity.noContent().build();
    }
}

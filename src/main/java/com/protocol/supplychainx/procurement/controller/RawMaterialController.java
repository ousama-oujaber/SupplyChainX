package com.protocol.supplychainx.procurement.controller;

import com.protocol.supplychainx.procurement.dto.RawMaterialDTO;
import com.protocol.supplychainx.procurement.service.IRawMaterialService;
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

import java.util.List;

@RestController
@RequestMapping("/api/procurement/raw-materials")
@RequiredArgsConstructor
@Tag(name = "Raw Material Management", description = "APIs for managing raw materials in the procurement module")
public class RawMaterialController {

    private final IRawMaterialService rawMaterialService;

    @PostMapping
    @Operation(summary = "Create a new raw material", description = "Add a new raw material to the system (US8)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Raw material created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<RawMaterialDTO> createRawMaterial(@Valid @RequestBody RawMaterialDTO rawMaterialDTO) {
        RawMaterialDTO createdMaterial = rawMaterialService.createRawMaterial(rawMaterialDTO);
        return new ResponseEntity<>(createdMaterial, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing raw material", description = "Modify raw material information (US9)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Raw material updated successfully"),
            @ApiResponse(responseCode = "404", description = "Raw material not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<RawMaterialDTO> updateRawMaterial(
            @Parameter(description = "Raw material ID") @PathVariable Long id,
            @Valid @RequestBody RawMaterialDTO rawMaterialDTO) {
        RawMaterialDTO updatedMaterial = rawMaterialService.updateRawMaterial(id, rawMaterialDTO);
        return ResponseEntity.ok(updatedMaterial);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get raw material by ID", description = "Retrieve raw material details by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Raw material found"),
            @ApiResponse(responseCode = "404", description = "Raw material not found")
    })
    public ResponseEntity<RawMaterialDTO> getRawMaterialById(
            @Parameter(description = "Raw material ID") @PathVariable Long id) {
        RawMaterialDTO material = rawMaterialService.getRawMaterial(id);
        return ResponseEntity.ok(material);
    }

    @GetMapping
    @Operation(summary = "Get all raw materials", description = "Retrieve paginated list of all raw materials (US11)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Raw materials retrieved successfully")
    })
    public ResponseEntity<Page<RawMaterialDTO>> getAllRawMaterials(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "idMaterial") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)") @RequestParam(defaultValue = "ASC") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<RawMaterialDTO> materials = rawMaterialService.getAllRawMaterials(pageable);
        return ResponseEntity.ok(materials);
    }

    @GetMapping("/search")
    @Operation(summary = "Search raw materials by name", description = "Search raw materials by name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    public ResponseEntity<Page<RawMaterialDTO>> searchRawMaterialsByName(
            @Parameter(description = "Name to search") @RequestParam String name,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<RawMaterialDTO> materials = rawMaterialService.searchRawMaterialsByName(name, pageable);
        return ResponseEntity.ok(materials);
    }

    @GetMapping("/below-minimum")
    @Operation(summary = "Get materials below minimum stock", description = "Retrieve materials with stock below minimum threshold (US12)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Materials retrieved successfully")
    })
    public ResponseEntity<Page<RawMaterialDTO>> getMaterialsBelowMinimumStock(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<RawMaterialDTO> materials = rawMaterialService.getRawMaterialsBelowMinimumStock(pageable);
        return ResponseEntity.ok(materials);
    }

    @GetMapping("/below-minimum/all")
    @Operation(summary = "Get all materials below minimum stock", description = "Retrieve all materials with stock below minimum (for alerts)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Materials retrieved successfully")
    })
    public ResponseEntity<List<RawMaterialDTO>> getAllMaterialsBelowMinimumStock() {
        List<RawMaterialDTO> materials = rawMaterialService.getAllRawMaterialsBelowMinimumStock();
        return ResponseEntity.ok(materials);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a raw material", description = "Delete a raw material if not used (US10)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Raw material deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Raw material not found"),
            @ApiResponse(responseCode = "409", description = "Raw material is in use")
    })
    public ResponseEntity<Void> deleteRawMaterial(
            @Parameter(description = "Raw material ID") @PathVariable Long id) {
        rawMaterialService.deleteRawMaterial(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{materialId}/suppliers/{supplierId}")
    @Operation(summary = "Add supplier to material", description = "Associate a supplier with a raw material")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Supplier added successfully"),
            @ApiResponse(responseCode = "404", description = "Material or supplier not found")
    })
    public ResponseEntity<RawMaterialDTO> addSupplierToMaterial(
            @Parameter(description = "Material ID") @PathVariable Long materialId,
            @Parameter(description = "Supplier ID") @PathVariable Long supplierId) {
        RawMaterialDTO material = rawMaterialService.addSupplierToMaterial(materialId, supplierId);
        return ResponseEntity.ok(material);
    }

    @DeleteMapping("/{materialId}/suppliers/{supplierId}")
    @Operation(summary = "Remove supplier from material", description = "Remove a supplier association from a raw material")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Supplier removed successfully"),
            @ApiResponse(responseCode = "404", description = "Material or supplier not found")
    })
    public ResponseEntity<RawMaterialDTO> removeSupplierFromMaterial(
            @Parameter(description = "Material ID") @PathVariable Long materialId,
            @Parameter(description = "Supplier ID") @PathVariable Long supplierId) {
        RawMaterialDTO material = rawMaterialService.removeSupplierFromMaterial(materialId, supplierId);
        return ResponseEntity.ok(material);
    }
}

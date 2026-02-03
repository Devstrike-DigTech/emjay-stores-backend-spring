package com.emjay.backend.ims.presentation.controller.supplier

import com.emjay.backend.common.application.dto.auth.MessageResponse
import com.emjay.backend.ims.application.dto.product.ProductPageResponse
import com.emjay.backend.ims.application.dto.supplier.*
import com.emjay.backend.ims.application.service.ProductService
import com.emjay.backend.ims.application.service.SupplierService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/suppliers")
@Tag(name = "Supplier Management", description = "Endpoints for managing suppliers")
@SecurityRequirement(name = "bearerAuth")
class SupplierController(
    private val supplierService: SupplierService,
    private val productService: ProductService
) {

    @PostMapping
    @Operation(summary = "Create a new supplier (Admin/Manager only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun createSupplier(@Valid @RequestBody request: CreateSupplierRequest): ResponseEntity<SupplierResponse> {
        val response = supplierService.createSupplier(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    @Operation(summary = "Get all suppliers")
    fun getAllSuppliers(): ResponseEntity<SupplierListResponse> {
        val response = supplierService.getAllSuppliers()
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{supplierId}")
    @Operation(summary = "Get supplier by ID")
    fun getSupplierById(@PathVariable supplierId: UUID): ResponseEntity<SupplierResponse> {
        val response = supplierService.getSupplierById(supplierId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/active")
    @Operation(summary = "Get active suppliers only")
    fun getActiveSuppliers(): ResponseEntity<SupplierListResponse> {
        val response = supplierService.getActiveSuppliers()
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{supplierId}/products")
    @Operation(summary = "Get products from a supplier")
    fun getSupplierProducts(
        @PathVariable supplierId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ProductPageResponse> {
        val response = productService.getProductsBySupplier(supplierId, page, size)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{supplierId}")
    @Operation(summary = "Update supplier (Admin/Manager only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun updateSupplier(
        @PathVariable supplierId: UUID,
        @Valid @RequestBody request: UpdateSupplierRequest
    ): ResponseEntity<SupplierResponse> {
        val response = supplierService.updateSupplier(supplierId, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{supplierId}")
    @Operation(summary = "Delete supplier (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteSupplier(@PathVariable supplierId: UUID): ResponseEntity<MessageResponse> {
        supplierService.deleteSupplier(supplierId)
        return ResponseEntity.ok(MessageResponse("Supplier deleted successfully"))
    }
}
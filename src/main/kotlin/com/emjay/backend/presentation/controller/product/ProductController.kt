package com.emjay.backend.presentation.controller.product

import com.emjay.backend.application.dto.auth.MessageResponse
import com.emjay.backend.application.dto.product.CreateProductRequest
import com.emjay.backend.application.dto.product.ProductPageResponse
import com.emjay.backend.application.dto.product.ProductResponse
import com.emjay.backend.application.dto.product.UpdateProductRequest
import com.emjay.backend.application.service.ProductService
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
@RequestMapping("/api/v1/products")
@Tag(name = "Product Management", description = "Endpoints for managing products")
@SecurityRequirement(name = "bearerAuth")
class ProductController(
    private val productService: ProductService
) {

    @PostMapping
    @Operation(summary = "Create a new product (Admin/Manager only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun createProduct(@Valid @RequestBody request: CreateProductRequest): ResponseEntity<ProductResponse> {
        val response = productService.createProduct(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    @Operation(summary = "Get all products (paginated)")
    fun getAllProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "name") sortBy: String
    ): ResponseEntity<ProductPageResponse> {
        val response = productService.getAllProducts(page, size, sortBy)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get product by ID")
    fun getProductById(@PathVariable productId: UUID): ResponseEntity<ProductResponse> {
        val response = productService.getProductById(productId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get product by SKU")
    fun getProductBySku(@PathVariable sku: String): ResponseEntity<ProductResponse> {
        val response = productService.getProductBySku(sku)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category")
    fun getProductsByCategory(
        @PathVariable categoryId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ProductPageResponse> {
        val response = productService.getProductsByCategory(categoryId, page, size)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/supplier/{supplierId}")
    @Operation(summary = "Get products by supplier")
    fun getProductsBySupplier(
        @PathVariable supplierId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ProductPageResponse> {
        val response = productService.getProductsBySupplier(supplierId, page, size)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock products")
    fun getLowStockProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ProductPageResponse> {
        val response = productService.getLowStockProducts(page, size)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by name or SKU")
    fun searchProducts(
        @RequestParam searchTerm: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ProductPageResponse> {
        val response = productService.searchProducts(searchTerm, page, size)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{productId}")
    @Operation(summary = "Update product (Admin/Manager only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun updateProduct(
        @PathVariable productId: UUID,
        @Valid @RequestBody request: UpdateProductRequest
    ): ResponseEntity<ProductResponse> {
        val response = productService.updateProduct(productId, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Delete product (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteProduct(@PathVariable productId: UUID): ResponseEntity<MessageResponse> {
        productService.deleteProduct(productId)
        return ResponseEntity.ok(MessageResponse("Product deleted successfully"))
    }
}
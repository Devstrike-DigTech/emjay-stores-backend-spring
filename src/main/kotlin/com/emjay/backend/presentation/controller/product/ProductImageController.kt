package com.emjay.backend.presentation.controller.product

import com.emjay.backend.application.dto.auth.MessageResponse
import com.emjay.backend.application.service.ProductImageResponse
import com.emjay.backend.application.service.ProductImageService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@RestController
@RequestMapping("/api/v1/products/{productId}/images")
@Tag(name = "Product Images", description = "Endpoints for managing product images")
@SecurityRequirement(name = "bearerAuth")
class ProductImageController(
    private val productImageService: ProductImageService
) {

    @PostMapping
    @Operation(summary = "Upload product image (Admin/Manager only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun uploadImage(
        @PathVariable productId: UUID,
        @RequestParam("file") file: MultipartFile,
        @RequestParam(required = false, defaultValue = "false") isPrimary: Boolean,
        @RequestParam(required = false, defaultValue = "0") displayOrder: Int
    ): ResponseEntity<ProductImageResponse> {
        val response = productImageService.uploadProductImage(productId, file, isPrimary, displayOrder)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    @Operation(summary = "Get all images for a product")
    fun getProductImages(@PathVariable productId: UUID): ResponseEntity<List<ProductImageResponse>> {
        val images = productImageService.getProductImages(productId)
        return ResponseEntity.ok(images)
    }

    @GetMapping("/primary")
    @Operation(summary = "Get primary image for a product")
    fun getPrimaryImage(@PathVariable productId: UUID): ResponseEntity<ProductImageResponse> {
        val image = productImageService.getPrimaryImage(productId)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(image)
    }

    @PatchMapping("/{imageId}/set-primary")
    @Operation(summary = "Set image as primary (Admin/Manager only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun setPrimaryImage(
        @PathVariable productId: UUID,
        @PathVariable imageId: UUID
    ): ResponseEntity<ProductImageResponse> {
        val response = productImageService.setPrimaryImage(productId, imageId)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{imageId}")
    @Operation(summary = "Delete product image (Admin/Manager only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun deleteImage(
        @PathVariable productId: UUID,
        @PathVariable imageId: UUID
    ): ResponseEntity<MessageResponse> {
        productImageService.deleteProductImage(productId, imageId)
        return ResponseEntity.ok(MessageResponse("Image deleted successfully"))
    }
}
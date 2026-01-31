package com.emjay.backend.application.dto.product

import com.emjay.backend.domain.entity.product.ProductStatus
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.util.*

// Request DTOs
data class CreateProductRequest(
    @field:NotBlank(message = "SKU is required")
    @field:Size(max = 100, message = "SKU must not exceed 100 characters")
    val sku: String,

    @field:NotBlank(message = "Product name is required")
    @field:Size(max = 300, message = "Product name must not exceed 300 characters")
    val name: String,

    val description: String? = null,

    @field:NotNull(message = "Category ID is required")
    val categoryId: UUID,

    val supplierId: UUID? = null,

    @field:NotNull(message = "Retail price is required")
    @field:DecimalMin(value = "0.0", inclusive = false, message = "Retail price must be greater than 0")
    val retailPrice: BigDecimal,

    @field:DecimalMin(value = "0.0", inclusive = false, message = "Wholesale price must be greater than 0")
    val wholesalePrice: BigDecimal? = null,

    @field:NotNull(message = "Cost price is required")
    @field:DecimalMin(value = "0.0", inclusive = false, message = "Cost price must be greater than 0")
    val costPrice: BigDecimal,

    @field:Min(value = 0, message = "Stock quantity cannot be negative")
    val stockQuantity: Int = 0,

    @field:Min(value = 0, message = "Minimum stock threshold cannot be negative")
    val minStockThreshold: Int = 10,

    @field:Size(max = 200, message = "Brand must not exceed 200 characters")
    val brand: String? = null,

    @field:NotNull(message = "Status is required")
    val status: ProductStatus = ProductStatus.ACTIVE
)

data class UpdateProductRequest(
    val name: String? = null,
    val description: String? = null,
    val categoryId: UUID? = null,
    val supplierId: UUID? = null,
    val retailPrice: BigDecimal? = null,
    val wholesalePrice: BigDecimal? = null,
    val costPrice: BigDecimal? = null,
    val stockQuantity: Int? = null,
    val minStockThreshold: Int? = null,
    val brand: String? = null,
    val status: ProductStatus? = null
)

// Response DTOs
data class ProductResponse(
    val id: String,
    val sku: String,
    val name: String,
    val description: String?,
    val categoryId: String,
    val supplierId: String?,
    val retailPrice: BigDecimal,
    val wholesalePrice: BigDecimal?,
    val costPrice: BigDecimal,
    val stockQuantity: Int,
    val minStockThreshold: Int,
    val brand: String?,
    val status: ProductStatus,
    val isLowStock: Boolean,
    val isOutOfStock: Boolean,
    val profitMargin: BigDecimal,
    val totalValue: BigDecimal,
    val images: List<ProductImageInfo> = emptyList()
)

data class ProductImageInfo(
    val id: String,
    val imageUrl: String,
    val isPrimary: Boolean,
    val displayOrder: Int
)

data class ProductPageResponse(
    val content: List<ProductResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int
)
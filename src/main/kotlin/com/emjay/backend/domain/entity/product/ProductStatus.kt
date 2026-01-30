package com.emjay.backend.domain.entity.product

/**
 * Product status in the inventory system
 */
enum class ProductStatus {
    ACTIVE,         // Available for sale
    DISCONTINUED,   // No longer available
    OUT_OF_STOCK   // Temporarily unavailable
}

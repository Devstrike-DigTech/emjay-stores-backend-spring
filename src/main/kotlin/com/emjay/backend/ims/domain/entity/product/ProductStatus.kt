package com.emjay.backend.ims.domain.entity.product

/**
 * Product status in the inventory system
 */
enum class ProductStatus {
    ACTIVE,         // Available for sale
    DISCONTINUED,   // No longer available
    OUT_OF_STOCK   // Temporarily unavailable
}

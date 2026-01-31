package com.emjay.backend.domain.repository.product

import com.emjay.backend.domain.entity.product.ProductImage
import java.util.*

interface ProductImageRepository {
    fun save(productImage: ProductImage): ProductImage
    
    fun findById(id: UUID): ProductImage?
    
    fun findByProductId(productId: UUID): List<ProductImage>
    
    fun findPrimaryByProductId(productId: UUID): ProductImage?
    
    fun deleteById(id: UUID)
    
    fun deleteAllByProductId(productId: UUID)
}

package com.emjay.backend.ims.infrastructure.persistence.repository

import com.emjay.backend.ims.infrastructure.persistence.entity.ProductImageEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface JpaProductImageRepository : JpaRepository<ProductImageEntity, UUID> {
    
    fun findAllByProductIdOrderByDisplayOrderAsc(productId: UUID): List<ProductImageEntity>
    
    fun findByProductIdAndIsPrimaryTrue(productId: UUID): ProductImageEntity?
    
    fun deleteAllByProductId(productId: UUID)
}

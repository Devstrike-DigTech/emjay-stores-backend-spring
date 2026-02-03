package com.emjay.backend.ims.infrastructure.persistence.repository

import com.emjay.backend.ims.domain.entity.product.ProductStatus
import com.emjay.backend.ims.infrastructure.persistence.entity.ProductEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface JpaProductRepository : JpaRepository<ProductEntity, UUID> {

    fun findBySku(sku: String): ProductEntity?

    fun existsBySku(sku: String): Boolean

    fun findAllByCategoryId(categoryId: UUID, pageable: Pageable): Page<ProductEntity>

    fun findAllBySupplierId(supplierId: UUID, pageable: Pageable): Page<ProductEntity>

    fun findAllByStatus(status: ProductStatus, pageable: Pageable): Page<ProductEntity>

    @Query("SELECT p FROM ProductEntity p WHERE p.stockQuantity <= p.minStockThreshold")
    fun findLowStockProducts(pageable: Pageable): Page<ProductEntity>

    @Query("SELECT p FROM ProductEntity p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    fun searchByNameOrSku(@Param("searchTerm") searchTerm: String, pageable: Pageable): Page<ProductEntity>
}
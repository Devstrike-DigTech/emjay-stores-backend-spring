package com.emjay.backend.promotions.infrastructure.persistence.repository

import com.emjay.backend.promotions.domain.entity.BundleStatus
import com.emjay.backend.promotions.domain.entity.PromotionStatus
import com.emjay.backend.promotions.infrastructure.persistence.entity.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

// ========== PRODUCT BUNDLE ==========

@Repository
interface JpaProductBundleRepository : JpaRepository<ProductBundleEntity, UUID> {
    fun findBySlug(slug: String): ProductBundleEntity?
    fun findByStatus(status: BundleStatus, pageable: Pageable): Page<ProductBundleEntity>
    fun findByIsFeaturedTrue(): List<ProductBundleEntity>

    @Query("SELECT b FROM ProductBundleEntity b WHERE b.status = 'ACTIVE'")
    fun findActiveOnly(): List<ProductBundleEntity>

    @Query("SELECT b FROM ProductBundleEntity b WHERE LOWER(b.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun searchByName(@Param("query") query: String, pageable: Pageable): Page<ProductBundleEntity>
}

// ========== BUNDLE PRODUCT ==========

@Repository
interface JpaBundleProductRepository : JpaRepository<BundleProductEntity, UUID> {
    fun findByBundleId(bundleId: UUID): List<BundleProductEntity>
    fun findByProductId(productId: UUID): List<BundleProductEntity>
    fun deleteByBundleId(bundleId: UUID)
}

// ========== BUNDLE IMAGE ==========

@Repository
interface JpaBundleImageRepository : JpaRepository<BundleImageEntity, UUID> {
    fun findByBundleId(bundleId: UUID): List<BundleImageEntity>

    @Query("SELECT i FROM BundleImageEntity i WHERE i.bundleId = :bundleId AND i.isPrimary = true")
    fun findPrimaryByBundleId(@Param("bundleId") bundleId: UUID): BundleImageEntity?
}

// ========== PROMOTION ==========

@Repository
interface JpaPromotionRepository : JpaRepository<PromotionEntity, UUID> {
    fun findByCode(code: String): PromotionEntity?
    fun findByStatus(status: PromotionStatus, pageable: Pageable): Page<PromotionEntity>

    @Query("SELECT p FROM PromotionEntity p WHERE p.status = 'ACTIVE'")
    fun findActiveOnly(): List<PromotionEntity>

    @Query("""
        SELECT p FROM PromotionEntity p 
        WHERE p.status = 'ACTIVE' 
        AND p.startDate <= :date 
        AND p.endDate >= :date
    """)
    fun findActiveByDate(@Param("date") date: LocalDateTime): List<PromotionEntity>

    @Query("SELECT p FROM PromotionEntity p WHERE p.code IS NULL AND p.status = 'ACTIVE'")
    fun findAutoApplied(): List<PromotionEntity>
}

// ========== PROMOTION PRODUCT ==========

@Repository
interface JpaPromotionProductRepository : JpaRepository<PromotionProductEntity, UUID> {
    fun findByPromotionId(promotionId: UUID): List<PromotionProductEntity>
    fun findByProductId(productId: UUID): List<PromotionProductEntity>
    fun deleteByPromotionId(promotionId: UUID)
}

// ========== PROMOTION CATEGORY ==========

@Repository
interface JpaPromotionCategoryRepository : JpaRepository<PromotionCategoryEntity, UUID> {
    fun findByPromotionId(promotionId: UUID): List<PromotionCategoryEntity>
    fun findByCategoryId(categoryId: UUID): List<PromotionCategoryEntity>
    fun deleteByPromotionId(promotionId: UUID)
}

// ========== PROMOTION SERVICE ==========

@Repository
interface JpaPromotionServiceRepository : JpaRepository<PromotionServiceEntity, UUID> {
    fun findByPromotionId(promotionId: UUID): List<PromotionServiceEntity>
    fun findByServiceId(serviceId: UUID): List<PromotionServiceEntity>
    fun deleteByPromotionId(promotionId: UUID)
}

// ========== PROMOTION USAGE ==========

@Repository
interface JpaPromotionUsageRepository : JpaRepository<PromotionUsageEntity, UUID> {
    fun findByPromotionId(promotionId: UUID): List<PromotionUsageEntity>
    fun findByCustomerId(customerId: UUID): List<PromotionUsageEntity>
    fun countByPromotionId(promotionId: UUID): Long

    @Query("SELECT COUNT(u) FROM PromotionUsageEntity u WHERE u.promotionId = :promotionId AND u.customerId = :customerId")
    fun countByPromotionIdAndCustomerId(
        @Param("promotionId") promotionId: UUID,
        @Param("customerId") customerId: UUID
    ): Long
}
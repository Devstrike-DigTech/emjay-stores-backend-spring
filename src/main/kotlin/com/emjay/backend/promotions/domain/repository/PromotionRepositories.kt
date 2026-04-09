package com.emjay.backend.promotions.domain.repository

import com.emjay.backend.promotions.domain.entity.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime
import java.util.*

// ========== PRODUCT BUNDLE REPOSITORY ==========

interface ProductBundleRepository {
    fun save(bundle: ProductBundle): ProductBundle
    fun findById(id: UUID): ProductBundle?
    fun findBySlug(slug: String): ProductBundle?
    fun findAll(pageable: Pageable): Page<ProductBundle>
    fun findByStatus(status: BundleStatus, pageable: Pageable): Page<ProductBundle>
    fun findFeatured(): List<ProductBundle>
    fun findActive(): List<ProductBundle>
    fun searchByName(query: String, pageable: Pageable): Page<ProductBundle>
    fun delete(bundle: ProductBundle)
}

// ========== BUNDLE PRODUCT REPOSITORY ==========

interface BundleProductRepository {
    fun save(bundleProduct: BundleProduct): BundleProduct
    fun saveAll(bundleProducts: List<BundleProduct>): List<BundleProduct>
    fun findByBundleId(bundleId: UUID): List<BundleProduct>
    fun findByProductId(productId: UUID): List<BundleProduct>
    fun deleteByBundleId(bundleId: UUID)
}

// ========== BUNDLE IMAGE REPOSITORY ==========

interface BundleImageRepository {
    fun save(image: BundleImage): BundleImage
    fun saveAll(images: List<BundleImage>): List<BundleImage>
    fun findByBundleId(bundleId: UUID): List<BundleImage>
    fun findPrimaryImage(bundleId: UUID): BundleImage?
    fun delete(image: BundleImage)
}

// ========== PROMOTION REPOSITORY ==========

interface PromotionRepository {
    fun save(promotion: Promotion): Promotion
    fun findById(id: UUID): Promotion?
    fun findByCode(code: String): Promotion?
    fun findAll(pageable: Pageable): Page<Promotion>
    fun findByStatus(status: PromotionStatus, pageable: Pageable): Page<Promotion>
    fun findActive(): List<Promotion>
    fun findActiveByDate(date: LocalDateTime): List<Promotion>
    fun findAutoApplied(): List<Promotion>
    fun delete(promotion: Promotion)
}

// ========== PROMOTION PRODUCT REPOSITORY ==========

interface PromotionProductRepository {
    fun save(promotionProduct: PromotionProduct): PromotionProduct
    fun saveAll(promotionProducts: List<PromotionProduct>): List<PromotionProduct>
    fun findByPromotionId(promotionId: UUID): List<PromotionProduct>
    fun findByProductId(productId: UUID): List<PromotionProduct>
    fun deleteByPromotionId(promotionId: UUID)
}

// ========== PROMOTION CATEGORY REPOSITORY ==========

interface PromotionCategoryRepository {
    fun save(promotionCategory: PromotionCategory): PromotionCategory
    fun saveAll(promotionCategories: List<PromotionCategory>): List<PromotionCategory>
    fun findByPromotionId(promotionId: UUID): List<PromotionCategory>
    fun findByCategoryId(categoryId: UUID): List<PromotionCategory>
    fun deleteByPromotionId(promotionId: UUID)
}

// ========== PROMOTION SERVICE REPOSITORY ==========

interface PromotionServiceRepository {
    fun save(promotionService: PromotionService): PromotionService
    fun saveAll(promotionServices: List<PromotionService>): List<PromotionService>
    fun findByPromotionId(promotionId: UUID): List<PromotionService>
    fun findByServiceId(serviceId: UUID): List<PromotionService>
    fun deleteByPromotionId(promotionId: UUID)
}

// ========== PROMOTION USAGE REPOSITORY ==========

interface PromotionUsageRepository {
    fun save(usage: PromotionUsage): PromotionUsage
    fun findByPromotionId(promotionId: UUID): List<PromotionUsage>
    fun findByCustomerId(customerId: UUID): List<PromotionUsage>
    fun countByPromotionId(promotionId: UUID): Long
    fun countByPromotionIdAndCustomerId(promotionId: UUID, customerId: UUID): Long
}
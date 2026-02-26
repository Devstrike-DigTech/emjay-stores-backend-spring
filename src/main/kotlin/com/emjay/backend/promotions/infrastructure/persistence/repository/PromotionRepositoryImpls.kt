package com.emjay.backend.promotions.infrastructure.persistence.repository

import com.emjay.backend.promotions.domain.entity.*
import com.emjay.backend.promotions.domain.repository.*
import com.emjay.backend.promotions.infrastructure.persistence.entity.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

// ========== PRODUCT BUNDLE REPOSITORY IMPL ==========

@Repository
class ProductBundleRepositoryImpl(
    private val jpaRepository: JpaProductBundleRepository
) : ProductBundleRepository {

    override fun save(bundle: ProductBundle): ProductBundle {
        val entity = toEntity(bundle)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findById(id: UUID): ProductBundle? =
        jpaRepository.findById(id).map { toDomain(it) }.orElse(null)

    override fun findBySlug(slug: String): ProductBundle? =
        jpaRepository.findBySlug(slug)?.let { toDomain(it) }

    override fun findAll(pageable: Pageable): Page<ProductBundle> =
        jpaRepository.findAll(pageable).map { toDomain(it) }

    override fun findByStatus(status: BundleStatus, pageable: Pageable): Page<ProductBundle> =
        jpaRepository.findByStatus(status, pageable).map { toDomain(it) }

    override fun findFeatured(): List<ProductBundle> =
        jpaRepository.findByIsFeaturedTrue().map { toDomain(it) }

    override fun findActive(): List<ProductBundle> =
        jpaRepository.findActiveOnly().map { toDomain(it) }

    override fun searchByName(query: String, pageable: Pageable): Page<ProductBundle> =
        jpaRepository.searchByName(query, pageable).map { toDomain(it) }

    override fun delete(bundle: ProductBundle) {
        bundle.id?.let { jpaRepository.deleteById(it) }
    }

    private fun toDomain(entity: ProductBundleEntity) = ProductBundle(
        id = entity.id,
        name = entity.name,
        slug = entity.slug,
        description = entity.description,
        shortDescription = entity.shortDescription,
        originalTotalPrice = entity.originalTotalPrice,
        bundlePrice = entity.bundlePrice,
        savingsAmount = entity.savingsAmount,
        savingsPercentage = entity.savingsPercentage,
        minQuantity = entity.minQuantity,
        maxQuantity = entity.maxQuantity,
        availableStock = entity.availableStock,
        status = entity.status,
        isFeatured = entity.isFeatured,
        startDate = entity.startDate,
        endDate = entity.endDate,
        primaryImageUrl = entity.primaryImageUrl,
        metaTitle = entity.metaTitle,
        metaDescription = entity.metaDescription,
        metaKeywords = entity.metaKeywords,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
        createdBy = entity.createdBy
    )

    private fun toEntity(domain: ProductBundle) = ProductBundleEntity(
        id = domain.id,
        name = domain.name,
        slug = domain.slug,
        description = domain.description,
        shortDescription = domain.shortDescription,
        originalTotalPrice = domain.originalTotalPrice,
        bundlePrice = domain.bundlePrice,
        savingsAmount = domain.savingsAmount,
        savingsPercentage = domain.savingsPercentage,
        minQuantity = domain.minQuantity,
        maxQuantity = domain.maxQuantity,
        availableStock = domain.availableStock,
        status = domain.status,
        isFeatured = domain.isFeatured,
        startDate = domain.startDate,
        endDate = domain.endDate,
        primaryImageUrl = domain.primaryImageUrl,
        metaTitle = domain.metaTitle,
        metaDescription = domain.metaDescription,
        metaKeywords = domain.metaKeywords,
        createdAt = domain.createdAt ?: LocalDateTime.now(),
        updatedAt = domain.updatedAt ?: LocalDateTime.now(),
        createdBy = domain.createdBy
    )
}

// ========== BUNDLE PRODUCT REPOSITORY IMPL ==========

@Repository
class BundleProductRepositoryImpl(
    private val jpaRepository: JpaBundleProductRepository
) : BundleProductRepository {

    override fun save(bundleProduct: BundleProduct): BundleProduct {
        val entity = toEntity(bundleProduct)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun saveAll(bundleProducts: List<BundleProduct>): List<BundleProduct> {
        val entities = bundleProducts.map { toEntity(it) }
        val saved = jpaRepository.saveAll(entities)
        return saved.map { toDomain(it) }
    }

    override fun findByBundleId(bundleId: UUID): List<BundleProduct> =
        jpaRepository.findByBundleId(bundleId).map { toDomain(it) }

    override fun findByProductId(productId: UUID): List<BundleProduct> =
        jpaRepository.findByProductId(productId).map { toDomain(it) }

    override fun deleteByBundleId(bundleId: UUID) {
        jpaRepository.deleteByBundleId(bundleId)
    }

    private fun toDomain(entity: BundleProductEntity) = BundleProduct(
        id = entity.id,
        bundleId = entity.bundleId,
        productId = entity.productId,
        quantity = entity.quantity,
        displayOrder = entity.displayOrder,
        createdAt = entity.createdAt
    )

    private fun toEntity(domain: BundleProduct) = BundleProductEntity(
        id = domain.id,
        bundleId = domain.bundleId,
        productId = domain.productId,
        quantity = domain.quantity,
        displayOrder = domain.displayOrder,
        createdAt = domain.createdAt ?: LocalDateTime.now()
    )
}

// ========== BUNDLE IMAGE REPOSITORY IMPL ==========

@Repository
class BundleImageRepositoryImpl(
    private val jpaRepository: JpaBundleImageRepository
) : BundleImageRepository {

    override fun save(image: BundleImage): BundleImage {
        val entity = toEntity(image)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun saveAll(images: List<BundleImage>): List<BundleImage> {
        val entities = images.map { toEntity(it) }
        val saved = jpaRepository.saveAll(entities)
        return saved.map { toDomain(it) }
    }

    override fun findByBundleId(bundleId: UUID): List<BundleImage> =
        jpaRepository.findByBundleId(bundleId).map { toDomain(it) }

    override fun findPrimaryImage(bundleId: UUID): BundleImage? =
        jpaRepository.findPrimaryByBundleId(bundleId)?.let { toDomain(it) }

    override fun delete(image: BundleImage) {
        image.id?.let { jpaRepository.deleteById(it) }
    }

    private fun toDomain(entity: BundleImageEntity) = BundleImage(
        id = entity.id,
        bundleId = entity.bundleId,
        imageUrl = entity.imageUrl,
        altText = entity.altText,
        displayOrder = entity.displayOrder,
        isPrimary = entity.isPrimary,
        createdAt = entity.createdAt
    )

    private fun toEntity(domain: BundleImage) = BundleImageEntity(
        id = domain.id,
        bundleId = domain.bundleId,
        imageUrl = domain.imageUrl,
        altText = domain.altText,
        displayOrder = domain.displayOrder,
        isPrimary = domain.isPrimary,
        createdAt = domain.createdAt ?: LocalDateTime.now()
    )
}

// ========== PROMOTION REPOSITORY IMPL ==========

@Repository
class PromotionRepositoryImpl(
    private val jpaRepository: JpaPromotionRepository
) : PromotionRepository {

    override fun save(promotion: Promotion): Promotion {
        val entity = toEntity(promotion)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findById(id: UUID): Promotion? =
        jpaRepository.findById(id).map { toDomain(it) }.orElse(null)

    override fun findByCode(code: String): Promotion? =
        jpaRepository.findByCode(code)?.let { toDomain(it) }

    override fun findAll(pageable: Pageable): Page<Promotion> =
        jpaRepository.findAll(pageable).map { toDomain(it) }

    override fun findByStatus(status: PromotionStatus, pageable: Pageable): Page<Promotion> =
        jpaRepository.findByStatus(status, pageable).map { toDomain(it) }

    override fun findActive(): List<Promotion> =
        jpaRepository.findActiveOnly().map { toDomain(it) }

    override fun findActiveByDate(date: LocalDateTime): List<Promotion> =
        jpaRepository.findActiveByDate(date).map { toDomain(it) }

    override fun findAutoApplied(): List<Promotion> =
        jpaRepository.findAutoApplied().map { toDomain(it) }

    override fun delete(promotion: Promotion) {
        promotion.id?.let { jpaRepository.deleteById(it) }
    }

    private fun toDomain(entity: PromotionEntity) = Promotion(
        id = entity.id,
        name = entity.name,
        code = entity.code,
        description = entity.description,
        promotionType = entity.promotionType,
        discountValue = entity.discountValue,
        minPurchaseAmount = entity.minPurchaseAmount,
        maxDiscountAmount = entity.maxDiscountAmount,
        usageLimit = entity.usageLimit,
        usagePerCustomer = entity.usagePerCustomer,
        appliesTo = entity.appliesTo,
        status = entity.status,
        startDate = entity.startDate,
        endDate = entity.endDate,
        totalUsageCount = entity.totalUsageCount,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
        createdBy = entity.createdBy
    )

    private fun toEntity(domain: Promotion) = PromotionEntity(
        id = domain.id,
        name = domain.name,
        code = domain.code,
        description = domain.description,
        promotionType = domain.promotionType,
        discountValue = domain.discountValue,
        minPurchaseAmount = domain.minPurchaseAmount,
        maxDiscountAmount = domain.maxDiscountAmount,
        usageLimit = domain.usageLimit,
        usagePerCustomer = domain.usagePerCustomer,
        appliesTo = domain.appliesTo,
        status = domain.status,
        startDate = domain.startDate,
        endDate = domain.endDate,
        totalUsageCount = domain.totalUsageCount,
        createdAt = domain.createdAt ?: LocalDateTime.now(),
        updatedAt = domain.updatedAt ?: LocalDateTime.now(),
        createdBy = domain.createdBy
    )
}

// ========== PROMOTION PRODUCT REPOSITORY IMPL ==========

@Repository
class PromotionProductRepositoryImpl(
    private val jpaRepository: JpaPromotionProductRepository
) : PromotionProductRepository {

    override fun save(promotionProduct: PromotionProduct) =
        toDomain(jpaRepository.save(toEntity(promotionProduct)))

    override fun saveAll(promotionProducts: List<PromotionProduct>) =
        jpaRepository.saveAll(promotionProducts.map { toEntity(it) }).map { toDomain(it) }

    override fun findByPromotionId(promotionId: UUID) =
        jpaRepository.findByPromotionId(promotionId).map { toDomain(it) }

    override fun findByProductId(productId: UUID) =
        jpaRepository.findByProductId(productId).map { toDomain(it) }

    override fun deleteByPromotionId(promotionId: UUID) =
        jpaRepository.deleteByPromotionId(promotionId)

    private fun toDomain(entity: PromotionProductEntity) = PromotionProduct(
        id = entity.id,
        promotionId = entity.promotionId,
        productId = entity.productId,
        createdAt = entity.createdAt
    )

    private fun toEntity(domain: PromotionProduct) = PromotionProductEntity(
        id = domain.id,
        promotionId = domain.promotionId,
        productId = domain.productId,
        createdAt = domain.createdAt ?: LocalDateTime.now()
    )
}

// ========== PROMOTION CATEGORY/SERVICE/USAGE (Same Pattern) ==========

@Repository
class PromotionCategoryRepositoryImpl(
    private val jpaRepository: JpaPromotionCategoryRepository
) : PromotionCategoryRepository {
    override fun save(promotionCategory: PromotionCategory) =
        toDomain(jpaRepository.save(toEntity(promotionCategory)))
    override fun saveAll(promotionCategories: List<PromotionCategory>) =
        jpaRepository.saveAll(promotionCategories.map { toEntity(it) }).map { toDomain(it) }
    override fun findByPromotionId(promotionId: UUID) =
        jpaRepository.findByPromotionId(promotionId).map { toDomain(it) }
    override fun findByCategoryId(categoryId: UUID) =
        jpaRepository.findByCategoryId(categoryId).map { toDomain(it) }
    override fun deleteByPromotionId(promotionId: UUID) =
        jpaRepository.deleteByPromotionId(promotionId)

    private fun toDomain(entity: PromotionCategoryEntity) = PromotionCategory(
        id = entity.id, promotionId = entity.promotionId,
        categoryId = entity.categoryId, createdAt = entity.createdAt
    )
    private fun toEntity(domain: PromotionCategory) = PromotionCategoryEntity(
        id = domain.id, promotionId = domain.promotionId,
        categoryId = domain.categoryId, createdAt = domain.createdAt ?: LocalDateTime.now()
    )
}

@Repository
class PromotionServiceRepositoryImpl(
    private val jpaRepository: JpaPromotionServiceRepository
) : PromotionServiceRepository {
    override fun save(promotionService: PromotionService) =
        toDomain(jpaRepository.save(toEntity(promotionService)))
    override fun saveAll(promotionServices: List<PromotionService>) =
        jpaRepository.saveAll(promotionServices.map { toEntity(it) }).map { toDomain(it) }
    override fun findByPromotionId(promotionId: UUID) =
        jpaRepository.findByPromotionId(promotionId).map { toDomain(it) }
    override fun findByServiceId(serviceId: UUID) =
        jpaRepository.findByServiceId(serviceId).map { toDomain(it) }
    override fun deleteByPromotionId(promotionId: UUID) =
        jpaRepository.deleteByPromotionId(promotionId)

    private fun toDomain(entity: PromotionServiceEntity) = PromotionService(
        id = entity.id, promotionId = entity.promotionId,
        serviceId = entity.serviceId, createdAt = entity.createdAt
    )
    private fun toEntity(domain: PromotionService) = PromotionServiceEntity(
        id = domain.id, promotionId = domain.promotionId,
        serviceId = domain.serviceId, createdAt = domain.createdAt ?: LocalDateTime.now()
    )
}

@Repository
class PromotionUsageRepositoryImpl(
    private val jpaRepository: JpaPromotionUsageRepository
) : PromotionUsageRepository {
    override fun save(usage: PromotionUsage) =
        toDomain(jpaRepository.save(toEntity(usage)))
    override fun findByPromotionId(promotionId: UUID) =
        jpaRepository.findByPromotionId(promotionId).map { toDomain(it) }
    override fun findByCustomerId(customerId: UUID) =
        jpaRepository.findByCustomerId(customerId).map { toDomain(it) }
    override fun countByPromotionId(promotionId: UUID) =
        jpaRepository.countByPromotionId(promotionId)
    override fun countByPromotionIdAndCustomerId(promotionId: UUID, customerId: UUID) =
        jpaRepository.countByPromotionIdAndCustomerId(promotionId, customerId)

    private fun toDomain(entity: PromotionUsageEntity) = PromotionUsage(
        id = entity.id, promotionId = entity.promotionId, customerId = entity.customerId,
        orderId = entity.orderId, discountAmount = entity.discountAmount, usedAt = entity.usedAt
    )
    private fun toEntity(domain: PromotionUsage) = PromotionUsageEntity(
        id = domain.id, promotionId = domain.promotionId, customerId = domain.customerId,
        orderId = domain.orderId, discountAmount = domain.discountAmount, usedAt = domain.usedAt
    )
}
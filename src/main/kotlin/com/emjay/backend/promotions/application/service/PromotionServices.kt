package com.emjay.backend.promotions.application.service

import com.emjay.backend.domain.exception.ResourceNotFoundException
import com.emjay.backend.ims.domain.repository.product.ProductRepository
import com.emjay.backend.promotions.application.dto.*
import com.emjay.backend.promotions.domain.entity.*
import com.emjay.backend.promotions.domain.repository.*
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.util.*

// ========== BUNDLE SERVICE ==========

@Service
class BundleService(
    private val bundleRepository: ProductBundleRepository,
    private val bundleProductRepository: BundleProductRepository,
    private val bundleImageRepository: BundleImageRepository,
    private val productRepository: ProductRepository
) {

    @Transactional
    fun createBundle(request: CreateBundleRequest, createdBy: UUID): BundleResponse {
        // Calculate original total price
        var originalTotal = BigDecimal.ZERO
        val productDetails = mutableListOf<Pair<UUID, Int>>()

        for (bundleProduct in request.products) {
            val product = productRepository.findById(bundleProduct.productId)
                ?: throw ResourceNotFoundException("Product ${bundleProduct.productId} not found")

            val subtotal = product.retailPrice * BigDecimal(bundleProduct.quantity)
            originalTotal += subtotal
            productDetails.add(bundleProduct.productId to bundleProduct.quantity)
        }

        // Calculate savings
        val savingsAmount = originalTotal - request.bundlePrice
        val savingsPercentage = if (originalTotal > BigDecimal.ZERO) {
            (savingsAmount / originalTotal * BigDecimal(100))
                .setScale(2, RoundingMode.HALF_UP)
        } else BigDecimal.ZERO

        // Generate slug
        val slug = generateSlug(request.name)

        // Create bundle
        val bundle = ProductBundle(
            name = request.name,
            slug = slug,
            description = request.description,
            shortDescription = request.shortDescription,
            originalTotalPrice = originalTotal,
            bundlePrice = request.bundlePrice,
            savingsAmount = savingsAmount,
            savingsPercentage = savingsPercentage,
            minQuantity = request.minQuantity,
            maxQuantity = request.maxQuantity,
            availableStock = request.availableStock,
            isFeatured = request.isFeatured,
            startDate = request.startDate,
            endDate = request.endDate,
            primaryImageUrl = request.primaryImageUrl,
            metaTitle = request.metaTitle,
            metaDescription = request.metaDescription,
            createdBy = createdBy
        )

        val savedBundle = bundleRepository.save(bundle)

        // Save bundle products
        val bundleProducts = request.products.map { req ->
            BundleProduct(
                bundleId = savedBundle.id!!,
                productId = req.productId,
                quantity = req.quantity,
                displayOrder = req.displayOrder
            )
        }
        bundleProductRepository.saveAll(bundleProducts)

        return toBundleResponse(savedBundle)
    }

    fun getBundle(id: UUID): BundleResponse {
        val bundle = bundleRepository.findById(id)
            ?: throw ResourceNotFoundException("Bundle not found")
        return toBundleResponse(bundle)
    }

    fun getBundles(page: Int = 0, size: Int = 20): List<BundleSummaryResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        return bundleRepository.findAll(pageable)
            .content
            .map { toBundleSummary(it) }
    }

    fun getActiveBundles(): List<BundleSummaryResponse> {
        return bundleRepository.findActive()
            .map { toBundleSummary(it) }
    }

    @Transactional
    fun updateBundle(id: UUID, request: UpdateBundleRequest): BundleResponse {
        val bundle = bundleRepository.findById(id)
            ?: throw ResourceNotFoundException("Bundle not found")

        val updated = bundle.copy(
            name = request.name ?: bundle.name,
            description = request.description ?: bundle.description,
            shortDescription = request.shortDescription ?: bundle.shortDescription,
            bundlePrice = request.bundlePrice ?: bundle.bundlePrice,
            status = request.status ?: bundle.status,
            isFeatured = request.isFeatured ?: bundle.isFeatured,
            availableStock = request.availableStock ?: bundle.availableStock
        )

        val saved = bundleRepository.save(updated)
        return toBundleResponse(saved)
    }

    @Transactional
    fun addBundleImage(bundleId: UUID, request: AddBundleImageRequest): BundleImageResponse {
        val bundle = bundleRepository.findById(bundleId)
            ?: throw ResourceNotFoundException("Bundle not found")

        val image = BundleImage(
            bundleId = bundleId,
            imageUrl = request.imageUrl,
            altText = request.altText,
            displayOrder = request.displayOrder,
            isPrimary = request.isPrimary
        )

        val saved = bundleImageRepository.save(image)
        return BundleImageResponse(
            id = saved.id.toString(),
            imageUrl = saved.imageUrl,
            altText = saved.altText,
            displayOrder = saved.displayOrder,
            isPrimary = saved.isPrimary
        )
    }

    /**
     * Calculate bundle pricing automatically
     */
    fun calculateBundlePrice(request: CalculateBundlePriceRequest): CalculateBundlePriceResponse {
        var originalTotal = BigDecimal.ZERO
        val productInfos = mutableListOf<ProductPriceInfo>()

        for (bundleProduct in request.products) {
            val product = productRepository.findById(bundleProduct.productId)
                ?: throw ResourceNotFoundException("Product ${bundleProduct.productId} not found")

            val subtotal = product.retailPrice * BigDecimal(bundleProduct.quantity)
            originalTotal += subtotal

            productInfos.add(ProductPriceInfo(
                productId = product.id.toString(),
                productName = product.name,
                quantity = bundleProduct.quantity,
                unitPrice = product.retailPrice,
                subtotal = subtotal
            ))
        }

        // Suggest 10-20% discount
        val defaultDiscount = BigDecimal("15")
        val discountPercentage = request.desiredDiscountPercentage ?: defaultDiscount
        val suggestedPrice = originalTotal * (BigDecimal(100) - discountPercentage) / BigDecimal(100)
        val minimumPrice = originalTotal * BigDecimal("0.80") // Max 20% discount

        return CalculateBundlePriceResponse(
            originalTotalPrice = originalTotal,
            suggestedBundlePrice = suggestedPrice.setScale(2, RoundingMode.HALF_UP),
            minimumBundlePrice = minimumPrice.setScale(2, RoundingMode.HALF_UP),
            savingsAmount = originalTotal - suggestedPrice,
            savingsPercentage = discountPercentage,
            products = productInfos
        )
    }

    private fun generateSlug(name: String): String {
        return name.lowercase()
            .replace(Regex("[^a-z0-9\\s-]"), "")
            .replace(Regex("\\s+"), "-")
            .trim('-')
    }

    private fun toBundleResponse(bundle: ProductBundle): BundleResponse {
        val products = bundleProductRepository.findByBundleId(bundle.id!!)
        val images = bundleImageRepository.findByBundleId(bundle.id)

        return BundleResponse(
            id = bundle.id.toString(),
            name = bundle.name,
            slug = bundle.slug,
            description = bundle.description,
            shortDescription = bundle.shortDescription,
            originalTotalPrice = bundle.originalTotalPrice,
            bundlePrice = bundle.bundlePrice,
            savingsAmount = bundle.savingsAmount,
            savingsPercentage = bundle.savingsPercentage ?: bundle.calculateSavingsPercentage(),
            minQuantity = bundle.minQuantity,
            maxQuantity = bundle.maxQuantity,
            availableStock = bundle.availableStock,
            status = bundle.status,
            isFeatured = bundle.isFeatured,
            isActive = bundle.isActive(),
            isInStock = bundle.isInStock(),
            startDate = bundle.startDate,
            endDate = bundle.endDate,
            primaryImageUrl = bundle.primaryImageUrl,
            images = images.map { BundleImageResponse(
                id = it.id.toString(),
                imageUrl = it.imageUrl,
                altText = it.altText,
                displayOrder = it.displayOrder,
                isPrimary = it.isPrimary
            )},
            products = emptyList(), // TODO: Fetch product details
            createdAt = bundle.createdAt!!
        )
    }

    private fun toBundleSummary(bundle: ProductBundle): BundleSummaryResponse {
        val productCount = bundleProductRepository.findByBundleId(bundle.id!!).size

        return BundleSummaryResponse(
            id = bundle.id.toString(),
            name = bundle.name,
            slug = bundle.slug,
            shortDescription = bundle.shortDescription,
            originalTotalPrice = bundle.originalTotalPrice,
            bundlePrice = bundle.bundlePrice,
            savingsAmount = bundle.savingsAmount,
            savingsPercentage = bundle.savingsPercentage ?: bundle.calculateSavingsPercentage(),
            primaryImageUrl = bundle.primaryImageUrl,
            productCount = productCount,
            isFeatured = bundle.isFeatured,
            isActive = bundle.isActive()
        )
    }
}

// ========== PROMOTION SERVICE ==========

@Service
class PromotionService(
    private val promotionRepository: PromotionRepository,
    private val promotionProductRepository: PromotionProductRepository,
    private val promotionCategoryRepository: PromotionCategoryRepository,
    private val promotionServiceRepository: PromotionServiceRepository,
    private val promotionUsageRepository: PromotionUsageRepository
) {

    @Transactional
    fun createPromotion(request: CreatePromotionRequest, createdBy: UUID): PromotionResponse {
        // Validate code uniqueness
        if (request.code != null) {
            promotionRepository.findByCode(request.code)?.let {
                throw IllegalArgumentException("Promo code already exists")
            }
        }

        val promotion = Promotion(
            name = request.name,
            code = request.code,
            description = request.description,
            promotionType = request.promotionType,
            discountValue = request.discountValue,
            minPurchaseAmount = request.minPurchaseAmount,
            maxDiscountAmount = request.maxDiscountAmount,
            usageLimit = request.usageLimit,
            usagePerCustomer = request.usagePerCustomer,
            appliesTo = request.appliesTo,
            startDate = request.startDate,
            endDate = request.endDate,
            createdBy = createdBy
        )

        val saved = promotionRepository.save(promotion)

        // Save applicable products/categories/services
        if (request.productIds.isNotEmpty()) {
            val promoProducts = request.productIds.map {
                PromotionProduct(promotionId = saved.id!!, productId = it)
            }
            promotionProductRepository.saveAll(promoProducts)
        }

        if (request.categoryIds.isNotEmpty()) {
            val promoCategories = request.categoryIds.map {
                PromotionCategory(promotionId = saved.id!!, categoryId = it)
            }
            promotionCategoryRepository.saveAll(promoCategories)
        }

        if (request.serviceIds.isNotEmpty()) {
            val promoServices = request.serviceIds.map {
                PromotionService(promotionId = saved.id!!, serviceId = it)
            }
            promotionServiceRepository.saveAll(promoServices)
        }

        return toPromotionResponse(saved)
    }

    fun validatePromoCode(request: ValidatePromoCodeRequest, customerId: UUID): ValidatePromoCodeResponse {
        val promotion = promotionRepository.findByCode(request.code)
            ?: return ValidatePromoCodeResponse(
                isValid = false,
                promotion = null,
                discountAmount = BigDecimal.ZERO,
                message = "Invalid promo code"
            )

        if (!promotion.canBeUsed()) {
            return ValidatePromoCodeResponse(
                isValid = false,
                promotion = null,
                discountAmount = BigDecimal.ZERO,
                message = "Promo code is expired or usage limit reached"
            )
        }

        // Check customer usage
        val customerUsage = promotionUsageRepository.countByPromotionIdAndCustomerId(promotion.id!!, customerId)
        if (customerUsage >= promotion.usagePerCustomer) {
            return ValidatePromoCodeResponse(
                isValid = false,
                promotion = null,
                discountAmount = BigDecimal.ZERO,
                message = "You have already used this promo code"
            )
        }

        // Calculate discount
        val discount = promotion.calculateDiscount(request.orderAmount)

        return ValidatePromoCodeResponse(
            isValid = discount > BigDecimal.ZERO,
            promotion = toPromotionResponse(promotion),
            discountAmount = discount,
            message = if (discount > BigDecimal.ZERO) "Promo code applied successfully" else "Minimum purchase requirement not met"
        )
    }

    fun getActivePromotions(): List<PromotionSummaryResponse> {
        return promotionRepository.findActive()
            .map { toPromotionSummary(it) }
    }

    @Transactional
    fun recordUsage(promotionId: UUID, customerId: UUID, orderId: UUID, discountAmount: BigDecimal) {
        val usage = PromotionUsage(
            promotionId = promotionId,
            customerId = customerId,
            orderId = orderId,
            discountAmount = discountAmount
        )
        promotionUsageRepository.save(usage)

        // Update promotion usage count
        val promotion = promotionRepository.findById(promotionId)
            ?: throw ResourceNotFoundException("Promotion not found")

        val updated = promotion.copy(totalUsageCount = promotion.totalUsageCount + 1)
        promotionRepository.save(updated)
    }

    private fun toPromotionResponse(promotion: Promotion): PromotionResponse {
        val products = promotionProductRepository.findByPromotionId(promotion.id!!).map { it.productId }
        val categories = promotionCategoryRepository.findByPromotionId(promotion.id).map { it.categoryId }
        val services = promotionServiceRepository.findByPromotionId(promotion.id).map { it.serviceId }

        val remainingUses = if (promotion.usageLimit != null) {
            promotion.usageLimit - promotion.totalUsageCount
        } else null

        return PromotionResponse(
            id = promotion.id.toString(),
            name = promotion.name,
            code = promotion.code,
            description = promotion.description,
            promotionType = promotion.promotionType,
            discountValue = promotion.discountValue,
            minPurchaseAmount = promotion.minPurchaseAmount,
            maxDiscountAmount = promotion.maxDiscountAmount,
            usageLimit = promotion.usageLimit,
            usagePerCustomer = promotion.usagePerCustomer,
            totalUsageCount = promotion.totalUsageCount,
            remainingUses = remainingUses,
            appliesTo = promotion.appliesTo,
            status = promotion.status,
            isActive = promotion.isActive(),
            hasCode = promotion.hasCode(),
            isAutoApplied = promotion.isAutoApplied(),
            startDate = promotion.startDate,
            endDate = promotion.endDate,
            applicableProducts = products,
            applicableCategories = categories,
            applicableServices = services,
            createdAt = promotion.createdAt!!
        )
    }

    private fun toPromotionSummary(promotion: Promotion) = PromotionSummaryResponse(
        id = promotion.id.toString(),
        name = promotion.name,
        code = promotion.code,
        promotionType = promotion.promotionType,
        discountValue = promotion.discountValue,
        status = promotion.status,
        isActive = promotion.isActive(),
        startDate = promotion.startDate,
        endDate = promotion.endDate
    )
}
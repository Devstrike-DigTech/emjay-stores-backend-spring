package com.emjay.backend.ecommerce.application.service

import com.emjay.backend.domain.exception.ResourceNotFoundException
import com.emjay.backend.ecommerce.application.dto.customer.*
import com.emjay.backend.ecommerce.domain.entity.customer.CustomerAnalytics
import com.emjay.backend.ecommerce.domain.entity.customer.WishlistItem
import com.emjay.backend.ecommerce.domain.repository.customer.CustomerAnalyticsRepository
import com.emjay.backend.ecommerce.domain.repository.customer.CustomerRepository
import com.emjay.backend.ecommerce.domain.repository.customer.WishlistItemRepository
import com.emjay.backend.ims.domain.repository.product.ProductImageRepository
import com.emjay.backend.ims.domain.repository.product.ProductRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class WishlistService(
    private val wishlistRepository: WishlistItemRepository,
    private val customerRepository: CustomerRepository,
    private val productRepository: ProductRepository,
    private val productImageRepository: ProductImageRepository
) {

    @Transactional
    fun addToWishlist(customerId: UUID, request: AddToWishlistRequest): WishlistItemResponse {
        // Verify customer exists
        customerRepository.findById(customerId)
            ?: throw ResourceNotFoundException("Customer not found")

        // Verify product exists
        val product = productRepository.findById(request.productId)
            ?: throw ResourceNotFoundException("Product not found")

        // Check if already in wishlist
        if (wishlistRepository.existsByCustomerAndProduct(customerId, request.productId)) {
            throw IllegalArgumentException("Product already in wishlist")
        }

        val wishlistItem = WishlistItem(
            customerId = customerId,
            productId = request.productId,
            priority = request.priority,
            notes = request.notes,
            priceWhenAdded = product.retailPrice,
            notifyOnPriceDrop = request.notifyOnPriceDrop,
            targetPrice = request.targetPrice
        )

        val saved = wishlistRepository.save(wishlistItem)
        return toWishlistItemResponse(saved)
    }

    fun getCustomerWishlist(customerId: UUID, page: Int = 0, size: Int = 20): WishlistListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "priority", "addedAt"))
        val wishlistPage = wishlistRepository.findByCustomerId(customerId, pageable)

        val responses = wishlistPage.content.map { toWishlistItemResponse(it) }

        return WishlistListResponse(
            content = responses,
            totalElements = wishlistPage.totalElements,
            totalPages = wishlistPage.totalPages,
            currentPage = wishlistPage.number,
            pageSize = wishlistPage.size
        )
    }

    fun getWishlistItemCount(customerId: UUID): Long {
        return wishlistRepository.countByCustomerId(customerId)
    }

    @Transactional
    fun updateWishlistItem(itemId: UUID, request: UpdateWishlistItemRequest): WishlistItemResponse {
        val existing = wishlistRepository.findById(itemId)
            ?: throw ResourceNotFoundException("Wishlist item not found")

        val updated = existing.copy(
            priority = request.priority ?: existing.priority,
            notes = request.notes ?: existing.notes,
            notifyOnPriceDrop = request.notifyOnPriceDrop ?: existing.notifyOnPriceDrop,
            targetPrice = request.targetPrice ?: existing.targetPrice
        )

        val saved = wishlistRepository.save(updated)
        return toWishlistItemResponse(saved)
    }

    @Transactional
    fun removeFromWishlist(itemId: UUID) {
        val item = wishlistRepository.findById(itemId)
            ?: throw ResourceNotFoundException("Wishlist item not found")
        wishlistRepository.delete(item)
    }

    @Transactional
    fun removeProductFromWishlist(customerId: UUID, productId: UUID) {
        wishlistRepository.deleteByCustomerAndProduct(customerId, productId)
    }

    private fun toWishlistItemResponse(item: WishlistItem): WishlistItemResponse {
        val product = productRepository.findById(item.productId)

        // Get primary product image
        val productImageUrl = product?.id?.let { productId ->
            productImageRepository.findPrimaryByProductId(productId)?.imageUrl
        }

        val currentPrice = product?.retailPrice
        val priceDropPercentage = currentPrice?.let { item.priceDropPercentage(it) }
        val isPriceAtTarget = currentPrice?.let { item.isPriceAtOrBelowTarget(it) } ?: false

        return WishlistItemResponse(
            id = item.id.toString(),
            customerId = item.customerId.toString(),
            productId = item.productId.toString(),
            productName = product?.name,
            productImageUrl = productImageUrl,
            currentPrice = currentPrice,
            priority = item.priority,
            notes = item.notes,
            priceWhenAdded = item.priceWhenAdded,
            notifyOnPriceDrop = item.notifyOnPriceDrop,
            targetPrice = item.targetPrice,
            priceDropPercentage = priceDropPercentage,
            isPriceAtTarget = isPriceAtTarget,
            addedAt = item.addedAt!!
        )
    }
}

@Service
class CustomerAnalyticsService(
    private val analyticsRepository: CustomerAnalyticsRepository,
    private val customerRepository: CustomerRepository
) {

    fun getCustomerAnalytics(customerId: UUID): CustomerAnalyticsResponse {
        // Verify customer exists
        customerRepository.findById(customerId)
            ?: throw ResourceNotFoundException("Customer not found")

        val analytics = analyticsRepository.findByCustomerId(customerId)
            ?: throw ResourceNotFoundException("Customer analytics not found")

        return toCustomerAnalyticsResponse(analytics)
    }

    @Transactional
    fun setBudgetCap(customerId: UUID, request: SetBudgetCapRequest): CustomerAnalyticsResponse {
        val analytics = analyticsRepository.findByCustomerId(customerId)
            ?: throw ResourceNotFoundException("Customer analytics not found")

        val updated = analytics.copy(
            monthlyBudgetCap = request.monthlyBudgetCap,
            budgetAlertThreshold = request.budgetAlertThreshold
        )

        val saved = analyticsRepository.save(updated)
        return toCustomerAnalyticsResponse(saved)
    }

    @Transactional
    fun removeBudgetCap(customerId: UUID): CustomerAnalyticsResponse {
        val analytics = analyticsRepository.findByCustomerId(customerId)
            ?: throw ResourceNotFoundException("Customer analytics not found")

        val updated = analytics.copy(
            monthlyBudgetCap = null
        )

        val saved = analyticsRepository.save(updated)
        return toCustomerAnalyticsResponse(saved)
    }

    fun getDashboard(customerId: UUID): CustomerDashboardResponse {
        val customer = customerRepository.findById(customerId)
            ?: throw ResourceNotFoundException("Customer not found")

        val analytics = analyticsRepository.findByCustomerId(customerId)
            ?: throw ResourceNotFoundException("Customer analytics not found")

        // TODO: Fetch recent orders from order service
        val recentOrders = emptyList<OrderSummary>()

        // TODO: Get counts from respective services
        val wishlistCount = 0
        val addressCount = 0

        return CustomerDashboardResponse(
            profile = toCustomerProfileResponse(customer),
            analytics = toCustomerAnalyticsResponse(analytics),
            recentOrders = recentOrders,
            wishlistCount = wishlistCount,
            addressCount = addressCount
        )
    }

    private fun toCustomerAnalyticsResponse(analytics: CustomerAnalytics): CustomerAnalyticsResponse {
        return CustomerAnalyticsResponse(
            id = analytics.id.toString(),
            customerId = analytics.customerId.toString(),
            totalOrders = analytics.totalOrders,
            totalSpent = analytics.totalSpent,
            averageOrderValue = analytics.averageOrderValue,
            monthlyBudgetCap = analytics.monthlyBudgetCap,
            currentMonthSpent = analytics.currentMonthSpent,
            budgetAlertThreshold = analytics.budgetAlertThreshold,
            budgetUtilizationPercentage = analytics.budgetUtilizationPercentage(),
            remainingBudget = analytics.remainingBudget(),
            isOverBudget = analytics.isOverBudget(),
            isNearBudgetLimit = analytics.isNearBudgetLimit(),
            lifetimeValue = analytics.lifetimeValue,
            lastPurchaseAt = analytics.lastPurchaseAt,
            firstPurchaseAt = analytics.firstPurchaseAt,
            daysSinceLastPurchase = analytics.daysSinceLastPurchase,
            favoriteCategoryId = analytics.favoriteCategoryId?.toString(),
            favoriteBrand = analytics.favoriteBrand,
            isActiveCustomer = analytics.isActiveCustomer(),
            isNewCustomer = analytics.isNewCustomer(),
            isVIPCustomer = analytics.isVIPCustomer(),
            updatedAt = analytics.updatedAt!!
        )
    }

    private fun toCustomerProfileResponse(customer: com.emjay.backend.ecommerce.domain.entity.customer.Customer): CustomerProfileResponse {
        return CustomerProfileResponse(
            id = customer.id.toString(),
            customerType = customer.customerType,
            email = customer.email,
            phone = customer.phone,
            firstName = customer.firstName,
            lastName = customer.lastName,
            fullName = customer.fullName(),
            dateOfBirth = customer.dateOfBirth,
            gender = customer.gender,
            profileImageUrl = customer.profileImageUrl,
            authProvider = customer.authProvider,
            status = customer.status,
            emailVerified = customer.emailVerified,
            phoneVerified = customer.phoneVerified,
            newsletterSubscribed = customer.newsletterSubscribed,
            smsNotifications = customer.smsNotifications,
            isGuest = customer.isGuest(),
            isActive = customer.isActive(),
            lastLoginAt = customer.lastLoginAt,
            createdAt = customer.createdAt!!,
            updatedAt = customer.updatedAt!!
        )
    }
}
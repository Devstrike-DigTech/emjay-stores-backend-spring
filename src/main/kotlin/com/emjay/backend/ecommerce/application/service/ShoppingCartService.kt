package com.emjay.backend.ecommerce.application.service

import com.emjay.backend.domain.exception.ResourceNotFoundException
import com.emjay.backend.ecommerce.application.dto.cart.*
import com.emjay.backend.ecommerce.domain.entity.cart.CartItem
import com.emjay.backend.ecommerce.domain.entity.cart.CartStatus
import com.emjay.backend.ecommerce.domain.entity.cart.ShoppingCart
import com.emjay.backend.ecommerce.domain.repository.cart.CartItemRepository
import com.emjay.backend.ecommerce.domain.repository.cart.ShoppingCartRepository
import com.emjay.backend.ecommerce.domain.repository.customer.CustomerRepository
import com.emjay.backend.ims.domain.repository.product.ProductImageRepository
import com.emjay.backend.ims.domain.repository.product.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Service
class ShoppingCartService(
    private val cartRepository: ShoppingCartRepository,
    private val cartItemRepository: CartItemRepository,
    private val productRepository: ProductRepository,
    private val productImageRepository: ProductImageRepository,
    private val customerRepository: CustomerRepository
) {

    companion object {
        private const val TAX_RATE = 0.075 // 7.5% VAT in Nigeria
        private const val GUEST_CART_EXPIRY_HOURS = 24L
    }

    // ========== Create/Get Cart ==========

    @Transactional
    fun getOrCreateCart(customerId: UUID?, guestSessionId: String?): ShoppingCartResponse {
        require(customerId != null || guestSessionId != null) {
            "Either customerId or guestSessionId must be provided"
        }

        val cart = if (customerId != null) {
            getOrCreateCustomerCart(customerId)
        } else {
            getOrCreateGuestCart(guestSessionId!!)
        }

        return toShoppingCartResponse(cart)
    }

    private fun getOrCreateCustomerCart(customerId: UUID): ShoppingCart {
        // Verify customer exists
        customerRepository.findById(customerId)
            ?: throw ResourceNotFoundException("Customer not found")

        return cartRepository.findActiveByCustomerId(customerId)
            ?: createCustomerCart(customerId)
    }

    private fun createCustomerCart(customerId: UUID): ShoppingCart {
        val cart = ShoppingCart(
            customerId = customerId,
            status = CartStatus.ACTIVE
        )
        return cartRepository.save(cart)
    }

    private fun getOrCreateGuestCart(guestSessionId: String): ShoppingCart {
        return cartRepository.findActiveByGuestSessionId(guestSessionId)
            ?: createGuestCart(guestSessionId)
    }

    private fun createGuestCart(guestSessionId: String): ShoppingCart {
        val cart = ShoppingCart(
            guestSessionId = guestSessionId,
            status = CartStatus.ACTIVE,
            expiresAt = LocalDateTime.now().plusHours(GUEST_CART_EXPIRY_HOURS)
        )
        return cartRepository.save(cart)
    }

    @Transactional
    fun createGuestCartExplicit(request: CreateGuestCartRequest): CreateGuestCartResponse {
        val cart = createGuestCart(request.guestSessionId)

        return CreateGuestCartResponse(
            cartId = cart.id.toString(),
            guestSessionId = request.guestSessionId,
            expiresAt = cart.expiresAt!!,
            message = "Guest cart created successfully"
        )
    }

    // ========== Add to Cart ==========

    @Transactional
    fun addToCart(
        customerId: UUID?,
        guestSessionId: String?,
        request: AddToCartRequest
    ): AddToCartResponse {
        // Get or create cart
        val cart = if (customerId != null) {
            getOrCreateCustomerCart(customerId)
        } else {
            getOrCreateGuestCart(guestSessionId!!)
        }

        // Validate product
        val product = productRepository.findById(request.productId)
            ?: throw ResourceNotFoundException("Product not found")

        if (!product.canBeSold()) {
            throw IllegalArgumentException("Product is not available for purchase")
        }

        // Check stock
        if (product.stockQuantity < request.quantity) {
            throw IllegalArgumentException(
                "Insufficient stock. Available: ${product.stockQuantity}, Requested: ${request.quantity}"
            )
        }

        // Check if product already in cart
        val existingItem = cartItemRepository.findByCartAndProduct(cart.id!!, request.productId)

        val cartItem = if (existingItem != null) {
            // Update quantity
            val newQuantity = existingItem.quantity + request.quantity

            // Validate total quantity against stock
            if (product.stockQuantity < newQuantity) {
                throw IllegalArgumentException(
                    "Insufficient stock. Available: ${product.stockQuantity}, Requested total: $newQuantity"
                )
            }

            val updated = existingItem.updateQuantity(newQuantity)
            cartItemRepository.save(updated)
        } else {
            // Add new item
            // Get product image
            val productImageUrl = productImageRepository.findPrimaryByProductId(request.productId)?.imageUrl

            val newItem = CartItem(
                cartId = cart.id,
                productId = request.productId,
                quantity = request.quantity,
                unitPrice = product.retailPrice,
                subtotal = product.retailPrice * BigDecimal(request.quantity),
                productName = product.name,
                productSku = product.sku,
                productImageUrl = productImageUrl
            )
            cartItemRepository.save(newItem)
        }

        // Recalculate cart totals
        val updatedCart = recalculateCart(cart.id)

        return AddToCartResponse(
            cartId = cart.id.toString(),
            item = toCartItemResponse(cartItem),
            cart = getCartSummary(updatedCart.id!!)
        )
    }

    // ========== Update Cart Item ==========

    @Transactional
    fun updateCartItem(itemId: UUID, request: UpdateCartItemRequest): CartSummaryResponse {
        val item = cartItemRepository.findById(itemId)
            ?: throw ResourceNotFoundException("Cart item not found")

        // Validate product stock
        val product = productRepository.findById(item.productId)
            ?: throw ResourceNotFoundException("Product not found")

        if (product.stockQuantity < request.quantity) {
            throw IllegalArgumentException(
                "Insufficient stock. Available: ${product.stockQuantity}, Requested: ${request.quantity}"
            )
        }

        // Update quantity
        val updated = item.updateQuantity(request.quantity)
        cartItemRepository.save(updated)

        // Recalculate cart
        val cart = recalculateCart(item.cartId)

        return getCartSummary(cart.id!!)
    }

    // ========== Remove from Cart ==========

    @Transactional
    fun removeCartItem(itemId: UUID): CartSummaryResponse {
        val item = cartItemRepository.findById(itemId)
            ?: throw ResourceNotFoundException("Cart item not found")

        val cartId = item.cartId

        cartItemRepository.delete(item)

        // Recalculate cart
        val cart = recalculateCart(cartId)

        return getCartSummary(cart.id!!)
    }

    @Transactional
    fun clearCart(customerId: UUID?, guestSessionId: String?): CartOperationResponse {
        val cart = if (customerId != null) {
            cartRepository.findActiveByCustomerId(customerId)
        } else {
            cartRepository.findActiveByGuestSessionId(guestSessionId!!)
        } ?: throw ResourceNotFoundException("Cart not found")

        // Delete all items
        cartItemRepository.deleteByCartId(cart.id!!)

        // Reset cart totals
        val clearedCart = cart.copy(
            subtotal = BigDecimal.ZERO,
            discountAmount = BigDecimal.ZERO,
            taxAmount = BigDecimal.ZERO,
            totalAmount = BigDecimal.ZERO,
            couponCode = null
        )
        cartRepository.save(clearedCart)

        return CartOperationResponse(
            success = true,
            message = "Cart cleared successfully",
            cart = getCartSummary(cart.id)
        )
    }

    // ========== Get Cart ==========

    fun getCart(customerId: UUID?, guestSessionId: String?): CartSummaryResponse {
        val cart = if (customerId != null) {
            cartRepository.findActiveByCustomerId(customerId)
        } else {
            cartRepository.findActiveByGuestSessionId(guestSessionId!!)
        } ?: throw ResourceNotFoundException("Cart not found")

        return getCartSummary(cart.id!!)
    }

    fun getCartSummary(cartId: UUID): CartSummaryResponse {
        val cart = cartRepository.findById(cartId)
            ?: throw ResourceNotFoundException("Cart not found")

        val items = cartItemRepository.findByCartId(cartId)
        val itemResponses = items.map { toCartItemResponse(it) }

        val itemCount = items.size
        val totalQuantity = items.sumOf { it.quantity }

        return CartSummaryResponse(
            cartId = cart.id.toString(),
            itemCount = itemCount,
            totalQuantity = totalQuantity,
            subtotal = cart.subtotal,
            discountAmount = cart.discountAmount,
            taxAmount = cart.taxAmount,
            totalAmount = cart.totalAmount,
            couponCode = cart.couponCode,
            discountPercentage = cart.discountPercentage(),
            isGuest = cart.isGuestCart(),
            expiresAt = cart.expiresAt,
            items = itemResponses
        )
    }

    // ========== Validate Cart ==========

    fun validateCart(cartId: UUID): CartValidationResponse {
        val cart = cartRepository.findById(cartId)
            ?: throw ResourceNotFoundException("Cart not found")

        val items = cartItemRepository.findByCartId(cartId)
        val issues = mutableListOf<CartValidationIssue>()

        items.forEach { item ->
            val product = productRepository.findById(item.productId)

            if (product == null) {
                issues.add(CartValidationIssue(
                    itemId = item.id.toString(),
                    productId = item.productId.toString(),
                    productName = item.productName ?: "Unknown Product",
                    issueType = ValidationIssueType.PRODUCT_DELETED,
                    message = "Product no longer available",
                    requestedQuantity = item.quantity,
                    availableQuantity = null
                ))
            } else {
                if (product.isDiscontinued()) {
                    issues.add(CartValidationIssue(
                        itemId = item.id.toString(),
                        productId = item.productId.toString(),
                        productName = product.name,
                        issueType = ValidationIssueType.PRODUCT_DISCONTINUED,
                        message = "Product has been discontinued",
                        requestedQuantity = item.quantity,
                        availableQuantity = product.stockQuantity
                    ))
                } else if (product.isOutOfStock()) {
                    issues.add(CartValidationIssue(
                        itemId = item.id.toString(),
                        productId = item.productId.toString(),
                        productName = product.name,
                        issueType = ValidationIssueType.OUT_OF_STOCK,
                        message = "Product is out of stock",
                        requestedQuantity = item.quantity,
                        availableQuantity = 0
                    ))
                } else if (product.stockQuantity < item.quantity) {
                    issues.add(CartValidationIssue(
                        itemId = item.id.toString(),
                        productId = item.productId.toString(),
                        productName = product.name,
                        issueType = ValidationIssueType.INSUFFICIENT_STOCK,
                        message = "Only ${product.stockQuantity} items available",
                        requestedQuantity = item.quantity,
                        availableQuantity = product.stockQuantity
                    ))
                } else if (product.retailPrice != item.unitPrice) {
                    issues.add(CartValidationIssue(
                        itemId = item.id.toString(),
                        productId = item.productId.toString(),
                        productName = product.name,
                        issueType = ValidationIssueType.PRICE_CHANGED,
                        message = "Price has changed from ${item.unitPrice} to ${product.retailPrice}",
                        requestedQuantity = item.quantity,
                        availableQuantity = product.stockQuantity
                    ))
                }
            }
        }

        val isValid = issues.isEmpty()
        val cartSummary = if (isValid) getCartSummary(cartId) else null

        return CartValidationResponse(
            isValid = isValid,
            issues = issues,
            cart = cartSummary
        )
    }

    // ========== Merge Carts (Guest → Registered) ==========

    @Transactional
    fun mergeCarts(customerId: UUID, request: MergeCartsRequest): MergeCartsResponse {
        // Get customer cart (or create)
        val customerCart = getOrCreateCustomerCart(customerId)

        // Get guest cart
        val guestCart = cartRepository.findActiveByGuestSessionId(request.guestSessionId)
            ?: throw ResourceNotFoundException("Guest cart not found")

        if (guestCart.isEmpty()) {
            return MergeCartsResponse(
                success = true,
                message = "Guest cart is empty, nothing to merge",
                mergedItemCount = 0,
                cart = getCartSummary(customerCart.id!!)
            )
        }

        // Get items from guest cart
        val guestItems = cartItemRepository.findByCartId(guestCart.id!!)
        var mergedCount = 0

        // Merge items
        guestItems.forEach { guestItem ->
            val existingItem = cartItemRepository.findByCartAndProduct(
                customerCart.id!!,
                guestItem.productId
            )

            if (existingItem != null) {
                // Update quantity
                val newQuantity = existingItem.quantity + guestItem.quantity
                val updated = existingItem.updateQuantity(newQuantity)
                cartItemRepository.save(updated)
            } else {
                // Move item to customer cart
                val movedItem = guestItem.copy(
                    id = null,
                    cartId = customerCart.id
                )
                cartItemRepository.save(movedItem)
            }
            mergedCount++
        }

        // Mark guest cart as merged
        val mergedGuestCart = guestCart.copy(status = CartStatus.MERGED)
        cartRepository.save(mergedGuestCart)

        // Recalculate customer cart
        val updatedCart = recalculateCart(customerCart.id!!)

        return MergeCartsResponse(
            success = true,
            message = "Successfully merged $mergedCount items from guest cart",
            mergedItemCount = mergedCount,
            cart = getCartSummary(updatedCart.id!!)
        )
    }

    // ========== Helper Methods ==========

    private fun recalculateCart(cartId: UUID): ShoppingCart {
        val cart = cartRepository.findById(cartId)
            ?: throw ResourceNotFoundException("Cart not found")

        val items = cartItemRepository.findByCartId(cartId)

        val subtotal = items.sumOf { it.subtotal }
        val taxAmount = subtotal * BigDecimal(TAX_RATE)
        val totalAmount = subtotal - cart.discountAmount + taxAmount

        val updated = cart.copy(
            subtotal = subtotal,
            taxAmount = taxAmount,
            totalAmount = totalAmount
        )

        return cartRepository.save(updated)
    }

    private fun toCartItemResponse(item: CartItem): CartItemResponse {
        val product = productRepository.findById(item.productId)

        return CartItemResponse(
            id = item.id.toString(),
            cartId = item.cartId.toString(),
            productId = item.productId.toString(),
            productName = item.productName ?: "Unknown Product",
            productSku = item.productSku ?: "",
            productImageUrl = item.productImageUrl,
            quantity = item.quantity,
            unitPrice = item.unitPrice,
            subtotal = item.subtotal,
            isInStock = product?.let { !it.isOutOfStock() } ?: false,
            availableQuantity = product?.stockQuantity,
            addedAt = item.addedAt!!,
            updatedAt = item.updatedAt!!
        )
    }

    private fun toShoppingCartResponse(cart: ShoppingCart): ShoppingCartResponse {
        val items = cartItemRepository.findByCartId(cart.id!!)

        return ShoppingCartResponse(
            id = cart.id.toString(),
            customerId = cart.customerId?.toString(),
            guestSessionId = cart.guestSessionId,
            status = cart.status,
            subtotal = cart.subtotal,
            discountAmount = cart.discountAmount,
            taxAmount = cart.taxAmount,
            totalAmount = cart.totalAmount,
            couponCode = cart.couponCode,
            itemCount = items.size,
            totalQuantity = items.sumOf { it.quantity },
            isGuest = cart.isGuestCart(),
            isActive = cart.isActive(),
            isExpired = cart.isExpired(),
            expiresAt = cart.expiresAt,
            createdAt = cart.createdAt!!,
            updatedAt = cart.updatedAt!!
        )
    }
}
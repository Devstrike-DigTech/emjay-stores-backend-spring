package com.emjay.backend.ecommerce.domain.repository.customer

import com.emjay.backend.ecommerce.domain.entity.customer.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.*

/**
 * Repository interface for Customer domain entity
 */
interface CustomerRepository {

    fun save(customer: Customer): Customer

    fun findById(id: UUID): Customer?

    fun findByEmail(email: String): Customer?

    fun findByGoogleId(googleId: String): Customer?

    fun findByGuestSessionId(sessionId: String): Customer?

    fun findAll(pageable: Pageable): Page<Customer>

    fun findByCustomerType(customerType: CustomerType, pageable: Pageable): Page<Customer>

    fun findByStatus(status: CustomerStatus, pageable: Pageable): Page<Customer>

    fun existsByEmail(email: String): Boolean

    fun delete(customer: Customer)
}

/**
 * Repository interface for CustomerAddress domain entity
 */
interface CustomerAddressRepository {

    fun save(address: CustomerAddress): CustomerAddress

    fun findById(id: UUID): CustomerAddress?

    fun findByCustomerId(customerId: UUID, pageable: Pageable): Page<CustomerAddress>

    fun findDefaultAddress(customerId: UUID): CustomerAddress?

    fun findShippingAddresses(customerId: UUID, pageable: Pageable): Page<CustomerAddress>

    fun findBillingAddresses(customerId: UUID, pageable: Pageable): Page<CustomerAddress>

    fun countByCustomerId(customerId: UUID): Long

    fun delete(address: CustomerAddress)
}

/**
 * Repository interface for WishlistItem domain entity
 */
interface WishlistItemRepository {

    fun save(item: WishlistItem): WishlistItem

    fun findById(id: UUID): WishlistItem?

    fun findByCustomerId(customerId: UUID, pageable: Pageable): Page<WishlistItem>

    fun findByCustomerAndProduct(customerId: UUID, productId: UUID): WishlistItem?

    fun existsByCustomerAndProduct(customerId: UUID, productId: UUID): Boolean

    fun countByCustomerId(customerId: UUID): Long

    fun delete(item: WishlistItem)

    fun deleteByCustomerAndProduct(customerId: UUID, productId: UUID)
}

/**
 * Repository interface for CustomerAnalytics domain entity
 */
interface CustomerAnalyticsRepository {

    fun save(analytics: CustomerAnalytics): CustomerAnalytics

    fun findById(id: UUID): CustomerAnalytics?

    fun findByCustomerId(customerId: UUID): CustomerAnalytics?

    fun findAll(pageable: Pageable): Page<CustomerAnalytics>

    fun delete(analytics: CustomerAnalytics)
}
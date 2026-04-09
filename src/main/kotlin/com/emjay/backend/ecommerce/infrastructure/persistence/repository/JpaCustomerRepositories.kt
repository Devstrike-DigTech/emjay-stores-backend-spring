package com.emjay.backend.ecommerce.infrastructure.persistence.repository

import com.emjay.backend.ecommerce.domain.entity.customer.CustomerStatus
import com.emjay.backend.ecommerce.domain.entity.customer.CustomerType
import com.emjay.backend.ecommerce.infrastructure.persistence.entity.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface JpaCustomerRepository : JpaRepository<CustomerEntity, UUID> {

    fun findByEmail(email: String): CustomerEntity?

    fun findByGoogleId(googleId: String): CustomerEntity?

    fun findByGuestSessionId(sessionId: String): CustomerEntity?

    fun findByCustomerType(customerType: CustomerType, pageable: Pageable): Page<CustomerEntity>

    fun findByStatus(status: CustomerStatus, pageable: Pageable): Page<CustomerEntity>

    fun existsByEmail(email: String): Boolean
}

@Repository
interface JpaCustomerAddressRepository : JpaRepository<CustomerAddressEntity, UUID> {

    fun findByCustomerId(customerId: UUID, pageable: Pageable): Page<CustomerAddressEntity>

    @Query("SELECT a FROM CustomerAddressEntity a WHERE a.customerId = :customerId AND a.isDefault = true")
    fun findDefaultAddress(@Param("customerId") customerId: UUID): CustomerAddressEntity?

    @Query("SELECT a FROM CustomerAddressEntity a WHERE a.customerId = :customerId AND a.isShippingAddress = true")
    fun findShippingAddresses(@Param("customerId") customerId: UUID, pageable: Pageable): Page<CustomerAddressEntity>

    @Query("SELECT a FROM CustomerAddressEntity a WHERE a.customerId = :customerId AND a.isBillingAddress = true")
    fun findBillingAddresses(@Param("customerId") customerId: UUID, pageable: Pageable): Page<CustomerAddressEntity>

    fun countByCustomerId(customerId: UUID): Long
}

@Repository
interface JpaWishlistItemRepository : JpaRepository<WishlistItemEntity, UUID> {

    fun findByCustomerId(customerId: UUID, pageable: Pageable): Page<WishlistItemEntity>

    fun findByCustomerIdAndProductId(customerId: UUID, productId: UUID): WishlistItemEntity?

    fun existsByCustomerIdAndProductId(customerId: UUID, productId: UUID): Boolean

    fun countByCustomerId(customerId: UUID): Long

    fun deleteByCustomerIdAndProductId(customerId: UUID, productId: UUID)
}

@Repository
interface JpaCustomerAnalyticsRepository : JpaRepository<CustomerAnalyticsEntity, UUID> {

    fun findByCustomerId(customerId: UUID): CustomerAnalyticsEntity?
}
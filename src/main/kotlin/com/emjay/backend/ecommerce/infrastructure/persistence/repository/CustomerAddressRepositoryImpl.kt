package com.emjay.backend.ecommerce.infrastructure.persistence.repository

import com.emjay.backend.ecommerce.domain.entity.customer.CustomerAddress
import com.emjay.backend.ecommerce.domain.repository.customer.CustomerAddressRepository
import com.emjay.backend.ecommerce.infrastructure.persistence.entity.CustomerAddressEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
class CustomerAddressRepositoryImpl(
    private val jpaRepository: JpaCustomerAddressRepository
) : CustomerAddressRepository {

    override fun save(address: CustomerAddress): CustomerAddress {
        val entity = toEntity(address)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findById(id: UUID): CustomerAddress? {
        return jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    }

    override fun findByCustomerId(customerId: UUID, pageable: Pageable): Page<CustomerAddress> {
        return jpaRepository.findByCustomerId(customerId, pageable).map { toDomain(it) }
    }

    override fun findDefaultAddress(customerId: UUID): CustomerAddress? {
        return jpaRepository.findDefaultAddress(customerId)?.let { toDomain(it) }
    }

    override fun findShippingAddresses(customerId: UUID, pageable: Pageable): Page<CustomerAddress> {
        return jpaRepository.findShippingAddresses(customerId, pageable).map { toDomain(it) }
    }

    override fun findBillingAddresses(customerId: UUID, pageable: Pageable): Page<CustomerAddress> {
        return jpaRepository.findBillingAddresses(customerId, pageable).map { toDomain(it) }
    }

    override fun countByCustomerId(customerId: UUID): Long {
        return jpaRepository.countByCustomerId(customerId)
    }

    override fun delete(address: CustomerAddress) {
        address.id?.let { jpaRepository.deleteById(it) }
    }

    private fun toDomain(entity: CustomerAddressEntity): CustomerAddress {
        return CustomerAddress(
            id = entity.id,
            customerId = entity.customerId,
            addressLabel = entity.addressLabel,
            recipientName = entity.recipientName,
            phone = entity.phone,
            addressLine1 = entity.addressLine1,
            addressLine2 = entity.addressLine2,
            city = entity.city,
            stateProvince = entity.stateProvince,
            postalCode = entity.postalCode,
            country = entity.country,
            latitude = entity.latitude,
            longitude = entity.longitude,
            isDefault = entity.isDefault,
            isBillingAddress = entity.isBillingAddress,
            isShippingAddress = entity.isShippingAddress,
            deliveryInstructions = entity.deliveryInstructions,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    private fun toEntity(domain: CustomerAddress): CustomerAddressEntity {
        return CustomerAddressEntity(
            id = domain.id,
            customerId = domain.customerId,
            addressLabel = domain.addressLabel,
            recipientName = domain.recipientName,
            phone = domain.phone,
            addressLine1 = domain.addressLine1,
            addressLine2 = domain.addressLine2,
            city = domain.city,
            stateProvince = domain.stateProvince,
            postalCode = domain.postalCode,
            country = domain.country,
            latitude = domain.latitude,
            longitude = domain.longitude,
            isDefault = domain.isDefault,
            isBillingAddress = domain.isBillingAddress,
            isShippingAddress = domain.isShippingAddress,
            deliveryInstructions = domain.deliveryInstructions,
            createdAt = domain.createdAt ?: LocalDateTime.now(),
            updatedAt = domain.updatedAt ?: LocalDateTime.now()
        )
    }
}
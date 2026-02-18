package com.emjay.backend.ecommerce.infrastructure.persistence.repository

import com.emjay.backend.ecommerce.domain.entity.customer.Customer
import com.emjay.backend.ecommerce.domain.entity.customer.CustomerStatus
import com.emjay.backend.ecommerce.domain.entity.customer.CustomerType
import com.emjay.backend.ecommerce.domain.repository.customer.CustomerRepository
import com.emjay.backend.ecommerce.infrastructure.persistence.entity.CustomerEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
class CustomerRepositoryImpl(
    private val jpaRepository: JpaCustomerRepository
) : CustomerRepository {

    override fun save(customer: Customer): Customer {
        val entity = toEntity(customer)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findById(id: UUID): Customer? {
        return jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    }

    override fun findByEmail(email: String): Customer? {
        return jpaRepository.findByEmail(email)?.let { toDomain(it) }
    }

    override fun findByGoogleId(googleId: String): Customer? {
        return jpaRepository.findByGoogleId(googleId)?.let { toDomain(it) }
    }

    override fun findByGuestSessionId(sessionId: String): Customer? {
        return jpaRepository.findByGuestSessionId(sessionId)?.let { toDomain(it) }
    }

    override fun findAll(pageable: Pageable): Page<Customer> {
        return jpaRepository.findAll(pageable).map { toDomain(it) }
    }

    override fun findByCustomerType(customerType: CustomerType, pageable: Pageable): Page<Customer> {
        return jpaRepository.findByCustomerType(customerType, pageable).map { toDomain(it) }
    }

    override fun findByStatus(status: CustomerStatus, pageable: Pageable): Page<Customer> {
        return jpaRepository.findByStatus(status, pageable).map { toDomain(it) }
    }

    override fun existsByEmail(email: String): Boolean {
        return jpaRepository.existsByEmail(email)
    }

    override fun delete(customer: Customer) {
        customer.id?.let { jpaRepository.deleteById(it) }
    }

    private fun toDomain(entity: CustomerEntity): Customer {
        return Customer(
            id = entity.id,
            customerType = entity.customerType,
            email = entity.email,
            phone = entity.phone,
            firstName = entity.firstName,
            lastName = entity.lastName,
            passwordHash = entity.passwordHash,
            authProvider = entity.authProvider,
            googleId = entity.googleId,
            facebookId = entity.facebookId,
            appleId = entity.appleId,
            dateOfBirth = entity.dateOfBirth,
            gender = entity.gender,
            profileImageUrl = entity.profileImageUrl,
            status = entity.status,
            emailVerified = entity.emailVerified,
            phoneVerified = entity.phoneVerified,
            newsletterSubscribed = entity.newsletterSubscribed,
            smsNotifications = entity.smsNotifications,
            guestSessionId = entity.guestSessionId,
            guestSessionExpiresAt = entity.guestSessionExpiresAt,
            lastLoginAt = entity.lastLoginAt,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    private fun toEntity(domain: Customer): CustomerEntity {
        return CustomerEntity(
            id = domain.id,
            customerType = domain.customerType,
            email = domain.email,
            phone = domain.phone,
            firstName = domain.firstName,
            lastName = domain.lastName,
            passwordHash = domain.passwordHash,
            authProvider = domain.authProvider,
            googleId = domain.googleId,
            facebookId = domain.facebookId,
            appleId = domain.appleId,
            dateOfBirth = domain.dateOfBirth,
            gender = domain.gender,
            profileImageUrl = domain.profileImageUrl,
            status = domain.status,
            emailVerified = domain.emailVerified,
            phoneVerified = domain.phoneVerified,
            newsletterSubscribed = domain.newsletterSubscribed,
            smsNotifications = domain.smsNotifications,
            guestSessionId = domain.guestSessionId,
            guestSessionExpiresAt = domain.guestSessionExpiresAt,
            lastLoginAt = domain.lastLoginAt,
            createdAt = domain.createdAt ?: LocalDateTime.now(),
            updatedAt = domain.updatedAt ?: LocalDateTime.now()
        )
    }
}
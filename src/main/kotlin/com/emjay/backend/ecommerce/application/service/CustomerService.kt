package com.emjay.backend.ecommerce.application.service

import com.emjay.backend.domain.exception.ResourceNotFoundException
import com.emjay.backend.ecommerce.application.dto.customer.*
import com.emjay.backend.ecommerce.domain.entity.customer.*
import com.emjay.backend.ecommerce.domain.repository.customer.CustomerAnalyticsRepository
import com.emjay.backend.ecommerce.domain.repository.customer.CustomerAddressRepository
import com.emjay.backend.ecommerce.domain.repository.customer.CustomerRepository
import com.emjay.backend.ecommerce.domain.repository.customer.WishlistItemRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Service
class CustomerService(
    private val customerRepository: CustomerRepository,
    private val customerAddressRepository: CustomerAddressRepository,
    private val wishlistRepository: WishlistItemRepository,
    private val analyticsRepository: CustomerAnalyticsRepository,
    private val passwordEncoder: PasswordEncoder
) {

    // ========== Customer Registration & Authentication ==========

    @Transactional
    fun registerCustomer(request: RegisterCustomerRequest): CustomerProfileResponse {
        // Check if email already exists
        if (customerRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already registered")
        }

        // Create customer
        val customer = Customer(
            customerType = CustomerType.REGISTERED,
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            phone = request.phone,
            passwordHash = passwordEncoder.encode(request.password),
            authProvider = AuthProvider.LOCAL,
            newsletterSubscribed = request.newsletterSubscribed,
            status = CustomerStatus.ACTIVE
        )

        val saved = customerRepository.save(customer)

        // Create analytics record
        createCustomerAnalytics(saved.id!!)

        // TODO: Send verification email

        return toCustomerProfileResponse(saved)
    }

    @Transactional
    fun registerWithGoogle(request: GoogleAuthRequest): GoogleAuthResponse {
        // TODO: Verify Google ID token with Google API
        // For now, this is a placeholder structure

        // Extract info from token (mock)
        val googleId = "google-${UUID.randomUUID()}" // From verified token
        val email = "user@example.com" // From verified token
        val firstName = "John" // From verified token
        val lastName = "Doe" // From verified token
        val profileImageUrl: String? = null // From verified token

        // Check if customer exists
        var customer = customerRepository.findByGoogleId(googleId)
        val isNewCustomer = customer == null

        if (customer == null) {
            // Check if email exists with different auth
            customer = customerRepository.findByEmail(email)

            if (customer != null && customer.authProvider != AuthProvider.GOOGLE) {
                throw IllegalArgumentException("Email already registered with different provider")
            }

            // Create new customer
            customer = Customer(
                customerType = CustomerType.REGISTERED,
                email = email,
                firstName = firstName,
                lastName = lastName,
                authProvider = AuthProvider.GOOGLE,
                googleId = googleId,
                profileImageUrl = profileImageUrl,
                emailVerified = true, // Google emails are pre-verified
                status = CustomerStatus.ACTIVE
            )

            customer = customerRepository.save(customer)
            createCustomerAnalytics(customer.id!!)
        } else {
            // Update last login
            val updated = customer.copy(lastLoginAt = LocalDateTime.now())
            customer = customerRepository.save(updated)
        }

        // TODO: Generate JWT token
        val accessToken = "jwt-token-${customer.id}"

        return GoogleAuthResponse(
            customerId = customer.id.toString(),
            email = customer.email!!,
            firstName = customer.firstName,
            lastName = customer.lastName,
            profileImageUrl = customer.profileImageUrl,
            isNewCustomer = isNewCustomer,
            accessToken = accessToken
        )
    }

    fun login(request: LoginRequest): LoginResponse {
        val customer = customerRepository.findByEmail(request.email)
            ?: throw IllegalArgumentException("Invalid email or password")

        if (!customer.isActive()) {
            throw IllegalArgumentException("Account is not active")
        }

        if (customer.authProvider != AuthProvider.LOCAL) {
            throw IllegalArgumentException("Please login with ${customer.authProvider}")
        }

        // Verify password
        if (!passwordEncoder.matches(request.password, customer.passwordHash)) {
            throw IllegalArgumentException("Invalid email or password")
        }

        // Update last login
        val updated = customer.copy(lastLoginAt = LocalDateTime.now())
        customerRepository.save(updated)

        // TODO: Generate JWT tokens
        val accessToken = "jwt-access-${customer.id}"
        val refreshToken = "jwt-refresh-${customer.id}"

        return LoginResponse(
            customerId = customer.id.toString(),
            email = customer.email!!,
            fullName = customer.fullName(),
            customerType = customer.customerType,
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    @Transactional
    fun createGuestSession(request: GuestSessionRequest): GuestSessionResponse {
        val sessionId = UUID.randomUUID().toString()
        val expiresAt = LocalDateTime.now().plusHours(24) // 24 hour session

        val guestCustomer = Customer(
            customerType = CustomerType.GUEST,
            email = request.email,
            phone = request.phone,
            authProvider = null, // Guest customers don't have auth provider
            guestSessionId = sessionId,
            guestSessionExpiresAt = expiresAt,
            status = CustomerStatus.ACTIVE
        )

        val saved = customerRepository.save(guestCustomer)

        return GuestSessionResponse(
            sessionId = sessionId,
            customerId = saved.id.toString(),
            expiresAt = expiresAt
        )
    }

    fun getGuestBySession(sessionId: String): CustomerProfileResponse {
        val customer = customerRepository.findByGuestSessionId(sessionId)
            ?: throw ResourceNotFoundException("Guest session not found or expired")

        if (!customer.guestSessionValid()) {
            throw IllegalStateException("Guest session expired")
        }

        return toCustomerProfileResponse(customer)
    }

    // ========== Customer Profile Management ==========

    fun getCustomerProfile(customerId: UUID): CustomerProfileResponse {
        val customer = customerRepository.findById(customerId)
            ?: throw ResourceNotFoundException("Customer not found")
        return toCustomerProfileResponse(customer)
    }

    @Transactional
    fun updateCustomerProfile(customerId: UUID, request: UpdateCustomerProfileRequest): CustomerProfileResponse {
        val existing = customerRepository.findById(customerId)
            ?: throw ResourceNotFoundException("Customer not found")

        val updated = existing.copy(
            firstName = request.firstName ?: existing.firstName,
            lastName = request.lastName ?: existing.lastName,
            phone = request.phone ?: existing.phone,
            dateOfBirth = request.dateOfBirth ?: existing.dateOfBirth,
            gender = request.gender ?: existing.gender,
            newsletterSubscribed = request.newsletterSubscribed ?: existing.newsletterSubscribed,
            smsNotifications = request.smsNotifications ?: existing.smsNotifications
        )

        val saved = customerRepository.save(updated)
        return toCustomerProfileResponse(saved)
    }

    fun getAllCustomers(page: Int = 0, size: Int = 20): CustomerListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val customersPage = customerRepository.findAll(pageable)

        val responses = customersPage.content.map { toCustomerProfileResponse(it) }

        return CustomerListResponse(
            content = responses,
            totalElements = customersPage.totalElements,
            totalPages = customersPage.totalPages,
            currentPage = customersPage.number,
            pageSize = customersPage.size
        )
    }

    @Transactional
    fun changePassword(customerId: UUID, request: ChangePasswordRequest) {
        val customer = customerRepository.findById(customerId)
            ?: throw ResourceNotFoundException("Customer not found")

        if (customer.authProvider != AuthProvider.LOCAL) {
            throw IllegalArgumentException("Cannot change password for ${customer.authProvider} accounts")
        }

        // Verify current password
        if (!passwordEncoder.matches(request.currentPassword, customer.passwordHash)) {
            throw IllegalArgumentException("Current password is incorrect")
        }

        // Update password
        val updated = customer.copy(
            passwordHash = passwordEncoder.encode(request.newPassword)
        )
        customerRepository.save(updated)
    }

    // ========== Customer Address Management ==========

    @Transactional
    fun addCustomerAddress(customerId: UUID, request: CreateCustomerAddressRequest): CustomerAddressResponse {
        // Verify customer exists
        customerRepository.findById(customerId)
            ?: throw ResourceNotFoundException("Customer not found")

        // If this is set as default, unset other default addresses
        if (request.isDefault) {
            val existingDefault = customerAddressRepository.findDefaultAddress(customerId)
            existingDefault?.let {
                val updated = it.copy(isDefault = false)
                customerAddressRepository.save(updated)
            }
        }

        val address = CustomerAddress(
            customerId = customerId,
            addressLabel = request.addressLabel,
            recipientName = request.recipientName,
            phone = request.phone,
            addressLine1 = request.addressLine1,
            addressLine2 = request.addressLine2,
            city = request.city,
            stateProvince = request.stateProvince,
            postalCode = request.postalCode,
            country = request.country,
            latitude = request.latitude,
            longitude = request.longitude,
            isDefault = request.isDefault,
            isBillingAddress = request.isBillingAddress,
            isShippingAddress = request.isShippingAddress,
            deliveryInstructions = request.deliveryInstructions
        )

        val saved = customerAddressRepository.save(address)
        return toCustomerAddressResponse(saved)
    }

    fun getCustomerAddresses(customerId: UUID, page: Int = 0, size: Int = 20): CustomerAddressListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "isDefault"))
        val addressesPage = customerAddressRepository.findByCustomerId(customerId, pageable)

        val responses = addressesPage.content.map { toCustomerAddressResponse(it) }

        return CustomerAddressListResponse(
            content = responses,
            totalElements = addressesPage.totalElements,
            totalPages = addressesPage.totalPages,
            currentPage = addressesPage.number,
            pageSize = addressesPage.size
        )
    }

    fun getDefaultAddress(customerId: UUID): CustomerAddressResponse? {
        return customerAddressRepository.findDefaultAddress(customerId)
            ?.let { toCustomerAddressResponse(it) }
    }

    @Transactional
    fun updateCustomerAddress(addressId: UUID, request: UpdateCustomerAddressRequest): CustomerAddressResponse {
        val existing = customerAddressRepository.findById(addressId)
            ?: throw ResourceNotFoundException("Address not found")

        // If setting as default, unset other defaults
        if (request.isDefault == true) {
            val existingDefault = customerAddressRepository.findDefaultAddress(existing.customerId)
            if (existingDefault != null && existingDefault.id != addressId) {
                val updated = existingDefault.copy(isDefault = false)
                customerAddressRepository.save(updated)
            }
        }

        val updated = existing.copy(
            addressLabel = request.addressLabel ?: existing.addressLabel,
            recipientName = request.recipientName ?: existing.recipientName,
            phone = request.phone ?: existing.phone,
            addressLine1 = request.addressLine1 ?: existing.addressLine1,
            addressLine2 = request.addressLine2 ?: existing.addressLine2,
            city = request.city ?: existing.city,
            stateProvince = request.stateProvince ?: existing.stateProvince,
            postalCode = request.postalCode ?: existing.postalCode,
            country = request.country ?: existing.country,
            latitude = request.latitude ?: existing.latitude,
            longitude = request.longitude ?: existing.longitude,
            isDefault = request.isDefault ?: existing.isDefault,
            isBillingAddress = request.isBillingAddress ?: existing.isBillingAddress,
            isShippingAddress = request.isShippingAddress ?: existing.isShippingAddress,
            deliveryInstructions = request.deliveryInstructions ?: existing.deliveryInstructions
        )

        val saved = customerAddressRepository.save(updated)
        return toCustomerAddressResponse(saved)
    }

    @Transactional
    fun deleteCustomerAddress(addressId: UUID) {
        val address = customerAddressRepository.findById(addressId)
            ?: throw ResourceNotFoundException("Address not found")
        customerAddressRepository.delete(address)
    }

    // ========== Helper Methods ==========

    private fun createCustomerAnalytics(customerId: UUID) {
        val analytics = CustomerAnalytics(
            customerId = customerId
        )
        analyticsRepository.save(analytics)
    }

    private fun toCustomerProfileResponse(customer: Customer): CustomerProfileResponse {
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

    private fun toCustomerAddressResponse(address: CustomerAddress): CustomerAddressResponse {
        return CustomerAddressResponse(
            id = address.id.toString(),
            customerId = address.customerId.toString(),
            addressLabel = address.addressLabel,
            recipientName = address.recipientName,
            phone = address.phone,
            addressLine1 = address.addressLine1,
            addressLine2 = address.addressLine2,
            city = address.city,
            stateProvince = address.stateProvince,
            postalCode = address.postalCode,
            country = address.country,
            latitude = address.latitude,
            longitude = address.longitude,
            isDefault = address.isDefault,
            isBillingAddress = address.isBillingAddress,
            isShippingAddress = address.isShippingAddress,
            deliveryInstructions = address.deliveryInstructions,
            fullAddress = address.fullAddress(),
            shortAddress = address.shortAddress(),
            displayLabel = address.displayLabel(),
            hasCoordinates = address.hasCoordinates(),
            createdAt = address.createdAt!!,
            updatedAt = address.updatedAt!!
        )
    }
}
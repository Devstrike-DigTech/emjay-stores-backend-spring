package com.emjay.backend.ecommerce.domain.entity.customer

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

/**
 * Customer Address domain entity
 * Supports shipping and billing addresses
 */
data class CustomerAddress(
    val id: UUID? = null,
    val customerId: UUID,
    val addressLabel: String? = null,
    val recipientName: String? = null,
    val phone: String? = null,
    val addressLine1: String,
    val addressLine2: String? = null,
    val city: String,
    val stateProvince: String? = null,
    val postalCode: String? = null,
    val country: String = "Nigeria",
    val latitude: BigDecimal? = null,
    val longitude: BigDecimal? = null,
    val isDefault: Boolean = false,
    val isBillingAddress: Boolean = false,
    val isShippingAddress: Boolean = true,
    val deliveryInstructions: String? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun fullAddress(): String {
        val parts = mutableListOf<String>()
        parts.add(addressLine1)
        addressLine2?.let { parts.add(it) }
        parts.add(city)
        stateProvince?.let { parts.add(it) }
        postalCode?.let { parts.add(it) }
        parts.add(country)
        return parts.joinToString(", ")
    }

    fun shortAddress(): String {
        return "$addressLine1, $city"
    }

    fun hasCoordinates(): Boolean {
        return latitude != null && longitude != null
    }

    fun displayLabel(): String {
        return addressLabel ?: if (isDefault) "Default Address" else "Address"
    }
}
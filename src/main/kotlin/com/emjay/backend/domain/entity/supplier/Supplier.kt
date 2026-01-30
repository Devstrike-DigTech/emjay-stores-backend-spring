package com.emjay.backend.domain.entity.supplier

import java.time.LocalDateTime
import java.util.UUID

/**
 * Supplier domain entity representing a product supplier
 */
data class Supplier(
    val id: UUID? = null,
    val name: String,
    val contactPerson: String?,
    val email: String?,
    val phone: String?,
    val address: String?,
    val paymentTerms: String?,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun hasContactInfo(): Boolean = !email.isNullOrBlank() || !phone.isNullOrBlank()
    
    fun activate(): Supplier = copy(isActive = true)
    
    fun deactivate(): Supplier = copy(isActive = false)
}

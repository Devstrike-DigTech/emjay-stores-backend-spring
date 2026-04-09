package com.emjay.backend.ims.application.dto.supplier

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

// Request DTOs
data class CreateSupplierRequest(
    @field:NotBlank(message = "Supplier name is required")
    @field:Size(max = 200, message = "Supplier name must not exceed 200 characters")
    val name: String,

    @field:Size(max = 200, message = "Contact person name must not exceed 200 characters")
    val contactPerson: String? = null,

    @field:Email(message = "Email must be valid")
    @field:Size(max = 255, message = "Email must not exceed 255 characters")
    val email: String? = null,

    @field:Size(max = 20, message = "Phone must not exceed 20 characters")
    val phone: String? = null,

    val address: String? = null,

    @field:Size(max = 500, message = "Payment terms must not exceed 500 characters")
    val paymentTerms: String? = null
)

data class UpdateSupplierRequest(
    val name: String? = null,
    val contactPerson: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val paymentTerms: String? = null,
    val isActive: Boolean? = null
)

// Response DTOs
data class SupplierResponse(
    val id: String,
    val name: String,
    val contactPerson: String?,
    val email: String?,
    val phone: String?,
    val address: String?,
    val paymentTerms: String?,
    val isActive: Boolean,
    val productCount: Int = 0
)

data class SupplierListResponse(
    val suppliers: List<SupplierResponse>,
    val totalCount: Int
)
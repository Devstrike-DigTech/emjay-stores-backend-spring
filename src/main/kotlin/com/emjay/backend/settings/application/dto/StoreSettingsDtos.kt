package com.emjay.backend.settings.application.dto

import java.time.LocalDate

data class StoreAccountResponse(
    val storeName: String,
    val storeDescription: String?,
    val logoUrl: String?
)

data class UpdateStoreAccountRequest(
    val storeName: String?,
    val storeDescription: String?,
    val logoUrl: String?
)

data class ContactInfoResponse(
    val contactEmail: String?,
    val contactPhone: String?,
    val address: String?
)

data class UpdateContactInfoRequest(
    val contactEmail: String?,
    val contactPhone: String?,
    val address: String?
)

data class WebsiteInfoResponse(
    val dateStarted: LocalDate?,
    val lastMaintenance: LocalDate?,
    val developerCompany: String?
)

data class UpdateWebsiteInfoRequest(
    val dateStarted: LocalDate?,
    val lastMaintenance: LocalDate?,
    val developerCompany: String?
)

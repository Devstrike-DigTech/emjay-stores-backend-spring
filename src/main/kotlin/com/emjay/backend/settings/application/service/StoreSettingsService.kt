package com.emjay.backend.settings.application.service

import com.emjay.backend.settings.application.dto.*
import com.emjay.backend.settings.domain.entity.StoreSettings
import com.emjay.backend.settings.domain.repository.StoreSettingsRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StoreSettingsService(
    private val storeSettingsRepository: StoreSettingsRepository
) {

    private fun getOrCreate(): StoreSettings =
        storeSettingsRepository.findFirst() ?: storeSettingsRepository.save(
            StoreSettings(storeName = "Emjay Stores")
        )

    fun getStoreAccount(): StoreAccountResponse {
        val s = getOrCreate()
        return StoreAccountResponse(s.storeName, s.storeDescription, s.logoUrl)
    }

    fun getContactInfo(): ContactInfoResponse {
        val s = getOrCreate()
        return ContactInfoResponse(s.contactEmail, s.contactPhone, s.address)
    }

    fun getWebsiteInfo(): WebsiteInfoResponse {
        val s = getOrCreate()
        return WebsiteInfoResponse(s.dateStarted, s.lastMaintenance, s.developerCompany)
    }

    @Transactional
    fun updateStoreAccount(request: UpdateStoreAccountRequest): StoreAccountResponse {
        val s = getOrCreate().copy(
            storeName = request.storeName ?: getOrCreate().storeName,
            storeDescription = request.storeDescription ?: getOrCreate().storeDescription,
            logoUrl = request.logoUrl ?: getOrCreate().logoUrl
        )
        val saved = storeSettingsRepository.save(s)
        return StoreAccountResponse(saved.storeName, saved.storeDescription, saved.logoUrl)
    }

    @Transactional
    fun updateContactInfo(request: UpdateContactInfoRequest): ContactInfoResponse {
        val s = getOrCreate().copy(
            contactEmail = request.contactEmail,
            contactPhone = request.contactPhone,
            address = request.address
        )
        val saved = storeSettingsRepository.save(s)
        return ContactInfoResponse(saved.contactEmail, saved.contactPhone, saved.address)
    }

    @Transactional
    fun updateWebsiteInfo(request: UpdateWebsiteInfoRequest): WebsiteInfoResponse {
        val s = getOrCreate().copy(
            dateStarted = request.dateStarted,
            lastMaintenance = request.lastMaintenance,
            developerCompany = request.developerCompany
        )
        val saved = storeSettingsRepository.save(s)
        return WebsiteInfoResponse(saved.dateStarted, saved.lastMaintenance, saved.developerCompany)
    }
}

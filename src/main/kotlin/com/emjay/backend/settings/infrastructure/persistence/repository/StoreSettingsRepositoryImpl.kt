package com.emjay.backend.settings.infrastructure.persistence.repository

import com.emjay.backend.settings.domain.entity.StoreSettings
import com.emjay.backend.settings.domain.repository.StoreSettingsRepository
import com.emjay.backend.settings.infrastructure.persistence.entity.StoreSettingsEntity
import org.springframework.stereotype.Repository

@Repository
class StoreSettingsRepositoryImpl(
    private val jpaRepository: JpaStoreSettingsRepository
) : StoreSettingsRepository {

    override fun findFirst(): StoreSettings? =
        jpaRepository.findFirstByOrderByCreatedAtAsc()?.let { toDomain(it) }

    override fun save(settings: StoreSettings): StoreSettings =
        toDomain(jpaRepository.save(toEntity(settings)))

    private fun toDomain(e: StoreSettingsEntity) = StoreSettings(
        id = e.id,
        storeName = e.storeName,
        storeDescription = e.storeDescription,
        logoUrl = e.logoUrl,
        contactEmail = e.contactEmail,
        contactPhone = e.contactPhone,
        address = e.address,
        dateStarted = e.dateStarted,
        lastMaintenance = e.lastMaintenance,
        developerCompany = e.developerCompany,
        createdAt = e.createdAt,
        updatedAt = e.updatedAt
    )

    private fun toEntity(d: StoreSettings) = StoreSettingsEntity(
        id = d.id,
        storeName = d.storeName,
        storeDescription = d.storeDescription,
        logoUrl = d.logoUrl,
        contactEmail = d.contactEmail,
        contactPhone = d.contactPhone,
        address = d.address,
        dateStarted = d.dateStarted,
        lastMaintenance = d.lastMaintenance,
        developerCompany = d.developerCompany,
        createdAt = d.createdAt ?: java.time.LocalDateTime.now(),
        updatedAt = d.updatedAt ?: java.time.LocalDateTime.now()
    )
}

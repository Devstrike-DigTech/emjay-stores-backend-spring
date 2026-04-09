package com.emjay.backend.ads.infrastructure.persistence.repository

import com.emjay.backend.ads.domain.entity.Ad
import com.emjay.backend.ads.domain.entity.AdStatus
import com.emjay.backend.ads.domain.entity.AdTarget
import com.emjay.backend.ads.domain.repository.AdRepository
import com.emjay.backend.ads.domain.repository.AdTargetRepository
import com.emjay.backend.ads.infrastructure.persistence.entity.AdEntity
import com.emjay.backend.ads.infrastructure.persistence.entity.AdTargetEntity
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
class AdRepositoryImpl(
    private val jpaAdRepository: JpaAdRepository
) : AdRepository {

    override fun save(ad: Ad): Ad = toDomain(jpaAdRepository.save(toEntity(ad)))

    override fun findById(id: UUID): Ad? =
        jpaAdRepository.findById(id).map { toDomain(it) }.orElse(null)

    override fun findAll(): List<Ad> = jpaAdRepository.findAll().map { toDomain(it) }

    override fun findByStatus(status: AdStatus): List<Ad> =
        jpaAdRepository.findByStatus(status).map { toDomain(it) }

    override fun findActive(): List<Ad> =
        jpaAdRepository.findCurrentlyActive().map { toDomain(it) }

    override fun delete(id: UUID) = jpaAdRepository.deleteById(id)

    private fun toDomain(e: AdEntity) = Ad(
        id = e.id,
        headline = e.headline,
        description = e.description,
        imageUrl = e.imageUrl,
        startDate = e.startDate,
        endDate = e.endDate,
        appliesTo = e.appliesTo,
        targetIds = emptyList(), // loaded separately
        status = e.status,
        createdBy = e.createdBy,
        createdAt = e.createdAt,
        updatedAt = e.updatedAt
    )

    private fun toEntity(d: Ad) = AdEntity(
        id = d.id,
        headline = d.headline,
        description = d.description,
        imageUrl = d.imageUrl,
        startDate = d.startDate,
        endDate = d.endDate,
        appliesTo = d.appliesTo,
        status = d.status,
        createdBy = d.createdBy,
        createdAt = d.createdAt ?: LocalDateTime.now(),
        updatedAt = d.updatedAt ?: LocalDateTime.now()
    )
}

@Repository
class AdTargetRepositoryImpl(
    private val jpaAdTargetRepository: JpaAdTargetRepository
) : AdTargetRepository {

    override fun saveAll(adId: UUID, targetIds: List<UUID>) {
        val entities = targetIds.map { AdTargetEntity(adId = adId, targetId = it) }
        jpaAdTargetRepository.saveAll(entities)
    }

    override fun findByAdId(adId: UUID): List<UUID> =
        jpaAdTargetRepository.findByAdId(adId).map { it.targetId }

    override fun deleteByAdId(adId: UUID) =
        jpaAdTargetRepository.deleteByAdId(adId)
}

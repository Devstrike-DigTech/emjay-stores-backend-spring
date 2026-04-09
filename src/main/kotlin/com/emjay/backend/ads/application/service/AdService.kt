package com.emjay.backend.ads.application.service

import com.emjay.backend.ads.application.dto.AdResponse
import com.emjay.backend.ads.application.dto.CreateAdRequest
import com.emjay.backend.ads.application.dto.UpdateAdRequest
import com.emjay.backend.ads.domain.entity.Ad
import com.emjay.backend.ads.domain.entity.AdStatus
import com.emjay.backend.ads.domain.repository.AdRepository
import com.emjay.backend.ads.domain.repository.AdTargetRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class AdService(
    private val adRepository: AdRepository,
    private val adTargetRepository: AdTargetRepository
) {

    @Transactional
    fun createAd(request: CreateAdRequest, createdBy: UUID): AdResponse {
        val status = when {
            LocalDateTime.now().isBefore(request.startDate) -> AdStatus.SCHEDULED
            else -> AdStatus.ACTIVE
        }
        val ad = Ad(
            headline = request.headline,
            description = request.description,
            imageUrl = request.imageUrl,
            startDate = request.startDate,
            endDate = request.endDate,
            appliesTo = request.appliesTo,
            status = status,
            createdBy = createdBy
        )
        val saved = adRepository.save(ad)
        if (request.targetIds.isNotEmpty()) {
            adTargetRepository.saveAll(saved.id!!, request.targetIds)
        }
        return toResponse(saved)
    }

    fun getAllAds(): List<AdResponse> = adRepository.findAll().map { toResponse(it) }

    fun getActiveAds(): List<AdResponse> = adRepository.findActive().map { toResponse(it) }

    fun getAdById(id: UUID): AdResponse {
        val ad = adRepository.findById(id) ?: throw NoSuchElementException("Ad not found: $id")
        return toResponse(ad)
    }

    @Transactional
    fun updateAd(id: UUID, request: UpdateAdRequest): AdResponse {
        val existing = adRepository.findById(id) ?: throw NoSuchElementException("Ad not found: $id")
        val updated = existing.copy(
            headline = request.headline ?: existing.headline,
            description = request.description ?: existing.description,
            imageUrl = request.imageUrl ?: existing.imageUrl,
            startDate = request.startDate ?: existing.startDate,
            endDate = request.endDate ?: existing.endDate,
            appliesTo = request.appliesTo ?: existing.appliesTo,
            status = request.status ?: existing.status,
            updatedAt = LocalDateTime.now()
        )
        val saved = adRepository.save(updated)
        if (request.targetIds != null) {
            adTargetRepository.deleteByAdId(id)
            if (request.targetIds.isNotEmpty()) {
                adTargetRepository.saveAll(id, request.targetIds)
            }
        }
        return toResponse(saved)
    }

    @Transactional
    fun deleteAd(id: UUID) {
        adTargetRepository.deleteByAdId(id)
        adRepository.delete(id)
    }

    @Transactional
    fun uploadImage(id: UUID, imageUrl: String): AdResponse {
        val existing = adRepository.findById(id) ?: throw NoSuchElementException("Ad not found: $id")
        val updated = existing.copy(imageUrl = imageUrl, updatedAt = LocalDateTime.now())
        return toResponse(adRepository.save(updated))
    }

    private fun toResponse(ad: Ad): AdResponse {
        val targetIds = if (ad.id != null) adTargetRepository.findByAdId(ad.id) else emptyList()
        return AdResponse(
            id = ad.id.toString(),
            headline = ad.headline,
            description = ad.description,
            imageUrl = ad.imageUrl,
            startDate = ad.startDate,
            endDate = ad.endDate,
            appliesTo = ad.appliesTo,
            targetIds = targetIds.map { it.toString() },
            status = ad.status,
            isActive = ad.isCurrentlyActive(),
            createdBy = ad.createdBy.toString(),
            createdAt = ad.createdAt,
            updatedAt = ad.updatedAt
        )
    }
}

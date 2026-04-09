package com.emjay.backend.ads.domain.repository

import com.emjay.backend.ads.domain.entity.Ad
import com.emjay.backend.ads.domain.entity.AdStatus
import java.util.UUID

interface AdRepository {
    fun save(ad: Ad): Ad
    fun findById(id: UUID): Ad?
    fun findAll(): List<Ad>
    fun findByStatus(status: AdStatus): List<Ad>
    fun findActive(): List<Ad>
    fun delete(id: UUID)
}

interface AdTargetRepository {
    fun saveAll(adId: UUID, targetIds: List<UUID>)
    fun findByAdId(adId: UUID): List<UUID>
    fun deleteByAdId(adId: UUID)
}

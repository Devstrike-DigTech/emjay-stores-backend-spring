package com.emjay.backend.ads.infrastructure.persistence.repository

import com.emjay.backend.ads.domain.entity.AdStatus
import com.emjay.backend.ads.infrastructure.persistence.entity.AdEntity
import com.emjay.backend.ads.infrastructure.persistence.entity.AdTargetEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface JpaAdRepository : JpaRepository<AdEntity, UUID> {
    fun findByStatus(status: AdStatus): List<AdEntity>

    @Query("SELECT a FROM AdEntity a WHERE a.status = 'ACTIVE' AND a.startDate <= :now AND a.endDate >= :now")
    fun findCurrentlyActive(now: LocalDateTime = LocalDateTime.now()): List<AdEntity>
}

@Repository
interface JpaAdTargetRepository : JpaRepository<AdTargetEntity, UUID> {
    fun findByAdId(adId: UUID): List<AdTargetEntity>
    fun deleteByAdId(adId: UUID)
}

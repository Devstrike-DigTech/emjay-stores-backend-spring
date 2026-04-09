package com.emjay.backend.settings.infrastructure.persistence.repository

import com.emjay.backend.settings.infrastructure.persistence.entity.StoreSettingsEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface JpaStoreSettingsRepository : JpaRepository<StoreSettingsEntity, UUID> {
    fun findFirstByOrderByCreatedAtAsc(): StoreSettingsEntity?
}

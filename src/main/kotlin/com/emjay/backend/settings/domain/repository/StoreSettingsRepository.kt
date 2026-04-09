package com.emjay.backend.settings.domain.repository

import com.emjay.backend.settings.domain.entity.StoreSettings

interface StoreSettingsRepository {
    fun findFirst(): StoreSettings?
    fun save(settings: StoreSettings): StoreSettings
}

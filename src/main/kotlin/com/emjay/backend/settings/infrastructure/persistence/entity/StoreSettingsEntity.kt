package com.emjay.backend.settings.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "store_settings")
data class StoreSettingsEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "store_name", nullable = false, length = 200)
    val storeName: String,

    @Column(name = "store_description", columnDefinition = "TEXT")
    val storeDescription: String? = null,

    @Column(name = "logo_url", length = 500)
    val logoUrl: String? = null,

    @Column(name = "contact_email", length = 255)
    val contactEmail: String? = null,

    @Column(name = "contact_phone", length = 50)
    val contactPhone: String? = null,

    @Column(columnDefinition = "TEXT")
    val address: String? = null,

    @Column(name = "date_started")
    val dateStarted: LocalDate? = null,

    @Column(name = "last_maintenance")
    val lastMaintenance: LocalDate? = null,

    @Column(name = "developer_company", length = 200)
    val developerCompany: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        val now = LocalDateTime.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}

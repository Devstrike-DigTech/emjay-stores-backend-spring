package com.emjay.backend.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "suppliers")
data class SupplierEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(nullable = false, unique = true, length = 200)
    val name: String,

    @Column(name = "contact_person", length = 200)
    val contactPerson: String? = null,

    @Column(length = 255)
    val email: String? = null,

    @Column(length = 20)
    val phone: String? = null,

    @Column(columnDefinition = "TEXT")
    val address: String? = null,

    @Column(name = "payment_terms", length = 500)
    val paymentTerms: String? = null,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

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
package com.emjay.backend.services.infrastructure.persistence.entity

import com.emjay.backend.services.domain.entity.*
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

// ========== SERVICE CATEGORY ENTITY ==========

@Entity
@Table(name = "service_categories")
data class ServiceCategoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(nullable = false, unique = true, length = 200)
    val name: String,

    @Column(nullable = false, unique = true, length = 200)
    val slug: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "image_url", length = 500)
    val imageUrl: String? = null,

    @Column(name = "display_order")
    val displayOrder: Int = 0,

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

// ========== SERVICE SUBCATEGORY ENTITY ==========

@Entity
@Table(
    name = "service_subcategories",
    uniqueConstraints = [UniqueConstraint(columnNames = ["category_id", "slug"])]
)
data class ServiceSubcategoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "category_id", nullable = false)
    val categoryId: UUID,

    @Column(nullable = false, length = 200)
    val name: String,

    @Column(nullable = false, length = 200)
    val slug: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "display_order")
    val displayOrder: Int = 0,

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

// ========== SERVICE ENTITY ==========

@Entity
@Table(name = "services")
data class ServiceEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "category_id", nullable = false)
    val categoryId: UUID,

    @Column(name = "subcategory_id")
    val subcategoryId: UUID? = null,

    @Column(nullable = false, length = 300)
    val name: String,

    @Column(nullable = false, unique = true, length = 300)
    val slug: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "short_description", length = 500)
    val shortDescription: String? = null,

    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    val basePrice: BigDecimal,

    @Column(name = "discounted_price", precision = 12, scale = 2)
    val discountedPrice: BigDecimal? = null,

    @Column(name = "duration_minutes", nullable = false)
    val durationMinutes: Int,

    @Column(name = "buffer_time_minutes", nullable = false)
    val bufferTimeMinutes: Int = 15,

    @Column(name = "skill_level", length = 50)
    val skillLevel: String? = null,

    @Column(name = "max_clients_per_slot", nullable = false)
    val maxClientsPerSlot: Int = 1,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "service_status")
    val status: ServiceStatus = ServiceStatus.ACTIVE,

    @Column(name = "is_featured", nullable = false)
    val isFeatured: Boolean = false,

    @Column(name = "requires_consultation", nullable = false)
    val requiresConsultation: Boolean = false,

    @Column(name = "meta_title", length = 200)
    val metaTitle: String? = null,

    @Column(name = "meta_description", length = 500)
    val metaDescription: String? = null,

    @Column(name = "meta_keywords", columnDefinition = "TEXT")
    val metaKeywords: String? = null,

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

// ========== SERVICE IMAGE ENTITY ==========

@Entity
@Table(name = "service_images")
data class ServiceImageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "service_id", nullable = false)
    val serviceId: UUID,

    @Column(name = "image_url", nullable = false, length = 500)
    val imageUrl: String,

    @Column(name = "alt_text", length = 200)
    val altText: String? = null,

    @Column(name = "display_order")
    val displayOrder: Int = 0,

    @Column(name = "is_primary", nullable = false)
    val isPrimary: Boolean = false,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        createdAt = LocalDateTime.now()
    }
}

// ========== SERVICE ADDON ENTITY ==========

@Entity
@Table(name = "service_addons")
data class ServiceAddonEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "service_id", nullable = false)
    val serviceId: UUID,

    @Column(nullable = false, length = 200)
    val name: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(nullable = false, precision = 12, scale = 2)
    val price: BigDecimal,

    @Column(name = "duration_minutes", nullable = false)
    val durationMinutes: Int = 0,

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

// ========== SERVICE STAFF ENTITY ==========

@Entity
@Table(
    name = "service_staff",
    uniqueConstraints = [UniqueConstraint(columnNames = ["service_id", "staff_id"])]
)
data class ServiceStaffEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "service_id", nullable = false)
    val serviceId: UUID,

    @Column(name = "staff_id", nullable = false)
    val staffId: UUID,

    @Column(name = "is_primary", nullable = false)
    val isPrimary: Boolean = false,

    @Column(name = "assigned_at", nullable = false, updatable = false)
    var assignedAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        assignedAt = LocalDateTime.now()
    }
}
package com.emjay.backend.ims.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "product_images")
data class ProductImageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    
    @Column(name = "product_id", nullable = false)
    val productId: UUID,
    
    @Column(name = "image_url", nullable = false, length = 500)
    val imageUrl: String,
    
    @Column(name = "file_name", nullable = false)
    val fileName: String,
    
    @Column(name = "file_size", nullable = false)
    val fileSize: Long,
    
    @Column(name = "mime_type", nullable = false, length = 100)
    val mimeType: String,
    
    @Column(name = "is_primary", nullable = false)
    val isPrimary: Boolean = false,
    
    @Column(name = "display_order", nullable = false)
    val displayOrder: Int = 0,
    
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        createdAt = LocalDateTime.now()
    }
}

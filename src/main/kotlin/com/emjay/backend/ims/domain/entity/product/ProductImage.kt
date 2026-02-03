package com.emjay.backend.ims.domain.entity.product

import java.time.LocalDateTime
import java.util.*

data class ProductImage(
    val id: UUID? = null,
    val productId: UUID,
    val imageUrl: String,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val isPrimary: Boolean = false,
    val displayOrder: Int = 0,
    val createdAt: LocalDateTime? = null
)

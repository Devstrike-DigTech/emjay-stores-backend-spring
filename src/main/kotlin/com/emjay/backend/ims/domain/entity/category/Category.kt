package com.emjay.backend.ims.domain.entity.category

import java.time.LocalDateTime
import java.util.UUID

/**
 * Category domain entity for product categorization
 */
data class Category(
    val id: UUID? = null,
    val name: String,
    val description: String?,
    val parentId: UUID?,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun isRootCategory(): Boolean = parentId == null
    
    fun isSubcategory(): Boolean = parentId != null
    
    fun activate(): Category = copy(isActive = true)
    
    fun deactivate(): Category = copy(isActive = false)
}

package com.emjay.backend.ims.application.dto.category

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.*

// Request DTOs
data class CreateCategoryRequest(
    @field:NotBlank(message = "Category name is required")
    @field:Size(max = 200, message = "Category name must not exceed 200 characters")
    val name: String,

    val description: String? = null,

    val parentId: UUID? = null
)

data class UpdateCategoryRequest(
    val name: String? = null,
    val description: String? = null,
    val parentId: UUID? = null,
    val isActive: Boolean? = null
)

// Response DTOs
data class CategoryResponse(
    val id: String,
    val name: String,
    val description: String?,
    val parentId: String?,
    val isActive: Boolean,
    val isRootCategory: Boolean,
    val hasSubcategories: Boolean,
    val productCount: Int = 0
)

data class CategoryTreeResponse(
    val id: String,
    val name: String,
    val description: String?,
    val isActive: Boolean,
    val productCount: Int = 0,
    val subcategories: List<CategoryTreeResponse> = emptyList()
)

data class CategoryListResponse(
    val categories: List<CategoryResponse>,
    val totalCount: Int
)
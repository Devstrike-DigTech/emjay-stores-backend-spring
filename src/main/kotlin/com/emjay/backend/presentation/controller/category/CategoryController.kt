package com.emjay.backend.presentation.controller.category

import com.emjay.backend.application.dto.auth.MessageResponse
import com.emjay.backend.application.dto.category.*
import com.emjay.backend.application.service.CategoryService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Category Management", description = "Endpoints for managing product categories")
@SecurityRequirement(name = "bearerAuth")
class CategoryController(
    private val categoryService: CategoryService
) {

    @PostMapping
    @Operation(summary = "Create a new category (Admin/Manager only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun createCategory(@Valid @RequestBody request: CreateCategoryRequest): ResponseEntity<CategoryResponse> {
        val response = categoryService.createCategory(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    @Operation(summary = "Get all categories")
    fun getAllCategories(): ResponseEntity<CategoryListResponse> {
        val response = categoryService.getAllCategories()
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{categoryId}")
    @Operation(summary = "Get category by ID")
    fun getCategoryById(@PathVariable categoryId: UUID): ResponseEntity<CategoryResponse> {
        val response = categoryService.getCategoryById(categoryId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/root")
    @Operation(summary = "Get root categories only (no parent)")
    fun getRootCategories(): ResponseEntity<CategoryListResponse> {
        val response = categoryService.getRootCategories()
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{categoryId}/subcategories")
    @Operation(summary = "Get subcategories of a category")
    fun getSubcategories(@PathVariable categoryId: UUID): ResponseEntity<CategoryListResponse> {
        val response = categoryService.getSubcategories(categoryId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/tree")
    @Operation(summary = "Get category tree (hierarchical structure)")
    fun getCategoryTree(): ResponseEntity<List<CategoryTreeResponse>> {
        val response = categoryService.getCategoryTree()
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{categoryId}")
    @Operation(summary = "Update category (Admin/Manager only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun updateCategory(
        @PathVariable categoryId: UUID,
        @Valid @RequestBody request: UpdateCategoryRequest
    ): ResponseEntity<CategoryResponse> {
        val response = categoryService.updateCategory(categoryId, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{categoryId}")
    @Operation(summary = "Delete category (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteCategory(@PathVariable categoryId: UUID): ResponseEntity<MessageResponse> {
        categoryService.deleteCategory(categoryId)
        return ResponseEntity.ok(MessageResponse("Category deleted successfully"))
    }
}
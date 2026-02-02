package com.emjay.backend.application.service

import com.emjay.backend.application.dto.category.*
import com.emjay.backend.domain.entity.category.Category
import com.emjay.backend.domain.exception.ResourceAlreadyExistsException
import com.emjay.backend.domain.exception.ResourceNotFoundException
import com.emjay.backend.domain.repository.category.CategoryRepository
import com.emjay.backend.domain.repository.product.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository
) {
    @Transactional
    fun createCategory(request: CreateCategoryRequest): CategoryResponse {
        // Check if category name already exists within the same parent
        if (request.parentId == null) {
            // For root categories, just check if name exists at root level
            if (categoryRepository.existsByNameAndParentId(request.name, null)) {
                throw ResourceAlreadyExistsException("Root category '${request.name}' already exists")
            }
        } else {
            // For subcategories, verify parent exists first
            val parent = categoryRepository.findById(request.parentId)
                ?: throw ResourceNotFoundException("Parent category not found")

            // Then check if name exists under this parent
            if (categoryRepository.existsByNameAndParentId(request.name, request.parentId)) {
                throw ResourceAlreadyExistsException("Category '${request.name}' already exists under '${parent.name}'")
            }
        }

        val category = Category(
            name = request.name,
            description = request.description,
            parentId = request.parentId,
            isActive = true
        )


        val saved = categoryRepository.save(category)

        if (saved.id == null) {
            throw IllegalStateException("Failed to save category - ID is null")
        }

        return toCategoryResponse(saved)
    }

    fun getCategoryById(categoryId: UUID): CategoryResponse {
        val category = categoryRepository.findById(categoryId)
            ?: throw ResourceNotFoundException("Category not found")
        return toCategoryResponse(category)
    }

    fun getAllCategories(): CategoryListResponse {
        val categories = categoryRepository.findAll()
        val responses = categories.map { toCategoryResponse(it) }
        return CategoryListResponse(
            categories = responses,
            totalCount = responses.size
        )
    }

    fun getRootCategories(): CategoryListResponse {
        val categories = categoryRepository.findRootCategories()
        val responses = categories.map { toCategoryResponse(it) }
        return CategoryListResponse(
            categories = responses,
            totalCount = responses.size
        )
    }

    fun getSubcategories(parentId: UUID): CategoryListResponse {
        // Verify parent exists
        categoryRepository.findById(parentId)
            ?: throw ResourceNotFoundException("Category not found")

        val subcategories = categoryRepository.findSubcategories(parentId)
        val responses = subcategories.map { toCategoryResponse(it) }
        return CategoryListResponse(
            categories = responses,
            totalCount = responses.size
        )
    }

    fun getCategoryTree(): List<CategoryTreeResponse> {
        val rootCategories = categoryRepository.findRootCategories()
        return rootCategories.map { buildCategoryTree(it) }
    }

    @Transactional
    fun updateCategory(categoryId: UUID, request: UpdateCategoryRequest): CategoryResponse {
        val category = categoryRepository.findById(categoryId)
            ?: throw ResourceNotFoundException("Category not found")

        // Check if new name conflicts with existing category
        request.name?.let { newName ->
            if (newName != category.name && categoryRepository.existsByName(newName)) {
                throw ResourceAlreadyExistsException("Category '$newName' already exists")
            }
        }

        // If updating parentId, verify parent exists and prevent circular reference
        request.parentId?.let { newParentId ->
            if (newParentId == categoryId) {
                throw IllegalArgumentException("Category cannot be its own parent")
            }
            categoryRepository.findById(newParentId)
                ?: throw ResourceNotFoundException("Parent category not found")

            // Check for circular reference
            if (wouldCreateCircularReference(categoryId, newParentId)) {
                throw IllegalArgumentException("Cannot create circular category reference")
            }
        }

        val updatedCategory = category.copy(
            name = request.name ?: category.name,
            description = request.description ?: category.description,
            parentId = request.parentId ?: category.parentId,
            isActive = request.isActive ?: category.isActive
        )

        val saved = categoryRepository.save(updatedCategory)
        return toCategoryResponse(saved)
    }

    @Transactional
    fun deleteCategory(categoryId: UUID) {
        val category = categoryRepository.findById(categoryId)
            ?: throw ResourceNotFoundException("Category not found")

        // Check if category has subcategories
        val subcategoryCount = categoryRepository.countSubcategories(categoryId)
        if (subcategoryCount > 0) {
            throw IllegalStateException("Cannot delete category with subcategories. Delete or reassign subcategories first.")
        }

        // Check if category has products
        val productCount = productRepository.findByCategory(categoryId, org.springframework.data.domain.PageRequest.of(0, 1)).totalElements
        if (productCount > 0) {
            throw IllegalStateException("Cannot delete category with products. Delete or reassign products first.")
        }

        categoryRepository.deleteById(categoryId)
    }

    private fun toCategoryResponse(category: Category): CategoryResponse {
        val categoryId = category.id!!
        val subcategoryCount = categoryRepository.countSubcategories(categoryId)
        val productCount = productRepository.findByCategory(categoryId, org.springframework.data.domain.PageRequest.of(0, 1)).totalElements

        return CategoryResponse(
            id = categoryId.toString(),
            name = category.name,
            description = category.description,
            parentId = category.parentId?.toString(),
            isActive = category.isActive,
            isRootCategory = category.isRootCategory(),
            hasSubcategories = subcategoryCount > 0,
            productCount = productCount.toInt()
        )
    }

    private fun buildCategoryTree(category: Category): CategoryTreeResponse {
        val categoryId = category.id!!
        val subcategories = categoryRepository.findSubcategories(categoryId)
        val productCount = productRepository.findByCategory(categoryId, org.springframework.data.domain.PageRequest.of(0, 1)).totalElements

        return CategoryTreeResponse(
            id = categoryId.toString(),
            name = category.name,
            description = category.description,
            isActive = category.isActive,
            productCount = productCount.toInt(),
            subcategories = subcategories.map { buildCategoryTree(it) }
        )
    }

    private fun wouldCreateCircularReference(categoryId: UUID, newParentId: UUID): Boolean {
        var currentParentId: UUID? = newParentId

        while (currentParentId != null) {
            if (currentParentId == categoryId) {
                return true
            }

            val parent = categoryRepository.findById(currentParentId)
            currentParentId = parent?.parentId
        }

        return false
    }
}
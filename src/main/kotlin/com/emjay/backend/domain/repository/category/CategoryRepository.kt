package com.emjay.backend.domain.repository.category

import com.emjay.backend.domain.entity.category.Category
import java.util.UUID

/**
 * CategoryRepository port for category persistence
 */
interface CategoryRepository {
    fun save(category: Category): Category

    fun findByName(name: String): Category?

    fun findById(id: UUID): Category?
    
    fun findAll(): List<Category>
    
    fun findAllActive(): List<Category>
    
    fun findByParentId(parentId: UUID?): List<Category>
    
    fun findRootCategories(): List<Category>
    
    fun findSubcategories(parentId: UUID): List<Category>
    
    fun existsByName(name: String): Boolean

    fun countSubcategories(categoryId: UUID): Long
    
    fun existsByNameAndParentId(name: String, parentId: UUID?): Boolean
    
    fun deleteById(id: UUID)
    
    fun count(): Long
}

package com.emjay.backend.ims.infrastructure.persistence.repository

import com.emjay.backend.ims.infrastructure.persistence.entity.CategoryEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface JpaCategoryRepository : JpaRepository<CategoryEntity, UUID> {
    
    fun findByName(name: String): CategoryEntity?
    
    fun existsByName(name: String): Boolean
    
    fun findAllByParentIdIsNull(): List<CategoryEntity>

    @Query(
        value = """
        SELECT EXISTS(
            SELECT 1 FROM categories 
            WHERE name = :name 
            AND (
                (:parentId IS NULL AND parent_id IS NULL) 
                OR parent_id = CAST(:parentId AS uuid)
            )
        )
        """,
        nativeQuery = true
    )
    fun existsByNameAndParentId(@Param("name") name: String, @Param("parentId") parentId: String?): Boolean

    fun findAllByParentId(parentId: UUID): List<CategoryEntity>
    
    fun findAllByIsActive(isActive: Boolean): List<CategoryEntity>
    
    @Query("SELECT COUNT(c) FROM CategoryEntity c WHERE c.parentId = :categoryId")
    fun countSubcategories(categoryId: UUID): Long
}

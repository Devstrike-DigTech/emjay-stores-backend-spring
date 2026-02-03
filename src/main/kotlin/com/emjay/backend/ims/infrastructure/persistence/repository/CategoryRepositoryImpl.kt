package com.emjay.backend.ims.infrastructure.persistence.repository

import com.emjay.backend.ims.domain.entity.category.Category
import com.emjay.backend.ims.domain.repository.category.CategoryRepository
import com.emjay.backend.ims.infrastructure.persistence.entity.CategoryEntity
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
class CategoryRepositoryImpl(
    private val jpaRepository: JpaCategoryRepository
) : CategoryRepository {

    override fun save(category: Category): Category {
        val entity = toEntity(category)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findById(id: UUID): Category? {
        return jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    }

    override fun findByName(name: String): Category? {
        return jpaRepository.findByName(name)?.let { toDomain(it) }
    }

    override fun existsByName(name: String): Boolean {
        return jpaRepository.existsByName(name)
    }

    override fun existsByNameAndParentId(name: String, parentId: UUID?): Boolean {
        return jpaRepository.existsByNameAndParentId(name, parentId?.toString())
    }

    override fun findAll(): List<Category> {
        return jpaRepository.findAll().map { toDomain(it) }
    }

    override fun findRootCategories(): List<Category> {
        return jpaRepository.findAllByParentIdIsNull().map { toDomain(it) }
    }

    override fun findSubcategories(parentId: UUID): List<Category> {
        return jpaRepository.findAllByParentId(parentId).map { toDomain(it) }
    }

    override fun findAllActive(): List<Category> {
        return jpaRepository.findAllByIsActive(true).map { toDomain(it) }
    }

    override fun findByParentId(parentId: UUID?): List<Category> {
        return if (parentId == null) {
            jpaRepository.findAllByParentIdIsNull().map { toDomain(it) }
        } else {
            jpaRepository.findAllByParentId(parentId).map { toDomain(it) }
        }
    }

    override fun deleteById(id: UUID) {
        jpaRepository.deleteById(id)
    }

    override fun count(): Long {
        return jpaRepository.count()
    }

    override fun countSubcategories(categoryId: UUID): Long {
        return jpaRepository.countSubcategories(categoryId)
    }

    private fun toDomain(entity: CategoryEntity): Category {
        return Category(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            parentId = entity.parentId,
            isActive = entity.isActive,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    private fun toEntity(domain: Category): CategoryEntity {
        return CategoryEntity(
            id = domain.id,
            name = domain.name,
            description = domain.description,
            parentId = domain.parentId,
            isActive = domain.isActive,
            createdAt = domain.createdAt ?: LocalDateTime.now(),
            updatedAt = domain.updatedAt ?: LocalDateTime.now()
        )
    }
}
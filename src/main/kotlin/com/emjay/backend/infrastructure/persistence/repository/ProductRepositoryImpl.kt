package com.emjay.backend.infrastructure.persistence.repository

import com.emjay.backend.domain.entity.product.Product
import com.emjay.backend.domain.entity.product.ProductStatus
import com.emjay.backend.domain.repository.product.ProductRepository
import com.emjay.backend.infrastructure.persistence.entity.ProductEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.*

@Component
class ProductRepositoryImpl(
    private val jpaRepository: JpaProductRepository
) : ProductRepository {

    override fun save(product: Product): Product {
        val entity = toEntity(product)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findById(id: UUID): Product? {
        return jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    }

    override fun findBySku(sku: String): Product? {
        return jpaRepository.findBySku(sku)?.let { toDomain(it) }
    }

    override fun existsBySku(sku: String): Boolean {
        return jpaRepository.existsBySku(sku)
    }

    override fun findAll(pageable: Pageable): Page<Product> {
        return jpaRepository.findAll(pageable).map { toDomain(it) }
    }

    override fun findByCategory(categoryId: UUID, pageable: Pageable): Page<Product> {
        return jpaRepository.findAllByCategoryId(categoryId, pageable).map { toDomain(it) }
    }

    override fun findBySupplier(supplierId: UUID, pageable: Pageable): Page<Product> {
        return jpaRepository.findAllBySupplierId(supplierId, pageable).map { toDomain(it) }
    }

    override fun findByStatus(status: ProductStatus, pageable: Pageable): Page<Product> {
        TODO("Not yet implemented")
    }

    override fun findOutOfStock(): List<Product> {
        TODO("Not yet implemented")
    }

    override fun findLowStock(pageable: Pageable): Page<Product> {
        return jpaRepository.findLowStockProducts(pageable).map { toDomain(it) }
    }

    override fun searchByName(searchTerm: String, pageable: Pageable): Page<Product> {
        return jpaRepository.searchByNameOrSku(searchTerm, pageable).map { toDomain(it) }
    }

    override fun deleteById(id: UUID) {
        jpaRepository.deleteById(id)
    }

    override fun count(): Long {
        return jpaRepository.count()
    }

    override fun countByCategory(categoryId: UUID): Long {
        TODO("Not yet implemented")
    }

    override fun countByStatus(status: ProductStatus): Long {
        TODO("Not yet implemented")
    }

    private fun toDomain(entity: ProductEntity): Product {
        return Product(
            id = entity.id,
            sku = entity.sku,
            name = entity.name,
            description = entity.description,
            categoryId = entity.categoryId,
            supplierId = entity.supplierId,
            retailPrice = entity.retailPrice,
            wholesalePrice = entity.wholesalePrice,
            costPrice = entity.costPrice,
            stockQuantity = entity.stockQuantity,
            minStockThreshold = entity.minStockThreshold,
            brand = entity.brand,
            status = entity.status,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    private fun toEntity(domain: Product): ProductEntity {
        return ProductEntity(
            id = domain.id,
            sku = domain.sku,
            name = domain.name,
            description = domain.description,
            categoryId = domain.categoryId,
            supplierId = domain.supplierId,
            retailPrice = domain.retailPrice,
            wholesalePrice = domain.wholesalePrice,
            costPrice = domain.costPrice,
            stockQuantity = domain.stockQuantity,
            minStockThreshold = domain.minStockThreshold,
            brand = domain.brand,
            status = domain.status,
            createdAt = domain.createdAt!!,
            updatedAt = domain.updatedAt!!
        )
    }
}
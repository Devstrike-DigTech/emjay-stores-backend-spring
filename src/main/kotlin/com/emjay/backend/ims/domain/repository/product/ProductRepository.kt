package com.emjay.backend.ims.domain.repository.product

import com.emjay.backend.ims.domain.entity.product.Product
import com.emjay.backend.ims.domain.entity.product.ProductStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

/**
 * ProductRepository port for product persistence
 */
interface ProductRepository {
    fun save(product: Product): Product
    
    fun findById(id: UUID): Product?
    
    fun findBySku(sku: String): Product?
    
    fun findAll(pageable: Pageable): Page<Product>
    
    fun findByCategory(categoryId: UUID, pageable: Pageable): Page<Product>
    
    fun findBySupplier(supplierId: UUID, pageable: Pageable): Page<Product>
    
    fun findByStatus(status: ProductStatus, pageable: Pageable): Page<Product>
    
//    fun findLowStock(): List<Product>
    fun findLowStock(pageable: Pageable): Page<Product>
    
    fun findOutOfStock(): List<Product>
    
    fun searchByName(query: String, pageable: Pageable): Page<Product>
    
    fun existsBySku(sku: String): Boolean
    
    fun deleteById(id: UUID)
    
    fun count(): Long
    
    fun countByCategory(categoryId: UUID): Long
    
    fun countByStatus(status: ProductStatus): Long
}

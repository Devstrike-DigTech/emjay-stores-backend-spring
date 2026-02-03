package com.emjay.backend.ims.domain.repository.supplier

import com.emjay.backend.ims.domain.entity.supplier.Supplier
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

/**
 * SupplierRepository port for supplier persistence
 */
interface SupplierRepository {
    fun save(supplier: Supplier): Supplier
    
    fun findById(id: UUID): Supplier?
    
    fun findAll(pageable: Pageable): Page<Supplier>
    
    fun findAllActive(): List<Supplier>
    
    fun findByName(name: String): Supplier?
    
    fun searchByName(query: String, pageable: Pageable): Page<Supplier>
    
    fun existsByName(name: String): Boolean
    
    fun existsByEmail(email: String): Boolean
    
    fun deleteById(id: UUID)
    
    fun count(): Long
    
    fun countActive(): Long
}

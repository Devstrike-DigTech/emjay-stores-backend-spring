package com.emjay.backend.infrastructure.persistence.repository

import com.emjay.backend.domain.entity.supplier.Supplier
import com.emjay.backend.domain.repository.supplier.SupplierRepository
import com.emjay.backend.infrastructure.persistence.entity.SupplierEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class SupplierRepositoryImpl(
    private val jpaRepository: JpaSupplierRepository
) : SupplierRepository {

    override fun save(supplier: Supplier): Supplier {
        val entity = toEntity(supplier)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findById(id: UUID): Supplier? {
        return jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    }

    override fun findByName(name: String): Supplier? {
        return jpaRepository.findByName(name)?.let { toDomain(it) }
    }

    override fun searchByName(query: String, pageable: Pageable): Page<Supplier> {
        return jpaRepository.findByNameContainingIgnoreCase(query, pageable).map { toDomain(it) }

    }

    override fun existsByName(name: String): Boolean {
        return jpaRepository.existsByName(name)
    }

    override fun existsByEmail(email: String): Boolean {
        return jpaRepository.existsByEmail(email)

    }

    override fun findAll(pageable: Pageable): Page<Supplier> {
        return jpaRepository.findAll(pageable).map { toDomain(it) }
    }


    override fun findAllActive(): List<Supplier> {
        return jpaRepository.findAllByIsActive(true).map { toDomain(it) }
    }

    override fun deleteById(id: UUID) {
        jpaRepository.deleteById(id)
    }

    override fun count(): Long {
        return jpaRepository.count()
    }

    override fun countActive(): Long {
        return jpaRepository.countByIsActive(true)

    }

    private fun toDomain(entity: SupplierEntity): Supplier {
        return Supplier(
            id = entity.id,
            name = entity.name,
            contactPerson = entity.contactPerson,
            email = entity.email,
            phone = entity.phone,
            address = entity.address,
            paymentTerms = entity.paymentTerms,
            isActive = entity.isActive,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    private fun toEntity(domain: Supplier): SupplierEntity {
        return SupplierEntity(
            id = domain.id,
            name = domain.name,
            contactPerson = domain.contactPerson,
            email = domain.email,
            phone = domain.phone,
            address = domain.address,
            paymentTerms = domain.paymentTerms,
            isActive = domain.isActive,
            createdAt = domain.createdAt ?: java.time.LocalDateTime.now(),
            updatedAt = domain.updatedAt ?: java.time.LocalDateTime.now()
        )
    }
}

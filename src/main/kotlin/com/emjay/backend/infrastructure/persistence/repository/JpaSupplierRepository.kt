package com.emjay.backend.infrastructure.persistence.repository

import com.emjay.backend.infrastructure.persistence.entity.SupplierEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.*

@Repository
interface JpaSupplierRepository : JpaRepository<SupplierEntity, UUID> {

    fun findByName(name: String): SupplierEntity?

    fun existsByName(name: String): Boolean

    fun existsByEmail(email: String): Boolean

    fun findAllByIsActive(isActive: Boolean): List<SupplierEntity>

    fun findByNameContainingIgnoreCase(query: String, pageable: Pageable): Page<SupplierEntity>

    fun countByIsActive(isActive: Boolean): Long
}
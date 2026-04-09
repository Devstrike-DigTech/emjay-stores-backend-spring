package com.emjay.backend.ims.infrastructure.persistence.repository

import com.emjay.backend.ims.infrastructure.persistence.entity.ProductTargetEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface JpaProductTargetRepository : JpaRepository<ProductTargetEntity, UUID> {
    fun findByProductId(productId: UUID): List<ProductTargetEntity>
    fun findByProductIdAndTargetYearAndTargetMonth(productId: UUID, year: Int, month: Int): ProductTargetEntity?
}

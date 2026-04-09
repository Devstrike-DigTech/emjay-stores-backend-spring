package com.emjay.backend.ims.infrastructure.persistence.repository

import com.emjay.backend.ims.domain.entity.product.ProductTarget
import com.emjay.backend.ims.domain.repository.product.ProductTargetRepository
import com.emjay.backend.ims.infrastructure.persistence.entity.ProductTargetEntity
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class ProductTargetRepositoryImpl(
    private val jpaRepository: JpaProductTargetRepository
) : ProductTargetRepository {

    override fun save(target: ProductTarget): ProductTarget =
        toDomain(jpaRepository.save(toEntity(target)))

    override fun findByProductId(productId: UUID): List<ProductTarget> =
        jpaRepository.findByProductId(productId).map { toDomain(it) }

    override fun findByProductIdAndYearMonth(productId: UUID, year: Int, month: Int): ProductTarget? =
        jpaRepository.findByProductIdAndTargetYearAndTargetMonth(productId, year, month)?.let { toDomain(it) }

    override fun findCurrentTarget(productId: UUID): ProductTarget? {
        val now = java.time.YearMonth.now()
        return findByProductIdAndYearMonth(productId, now.year, now.monthValue)
    }

    override fun delete(id: UUID) = jpaRepository.deleteById(id)

    private fun toDomain(entity: ProductTargetEntity) = ProductTarget(
        id = entity.id,
        productId = entity.productId,
        targetYear = entity.targetYear,
        targetMonth = entity.targetMonth,
        targetUnits = entity.targetUnits,
        createdBy = entity.createdBy,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )

    private fun toEntity(domain: ProductTarget) = ProductTargetEntity(
        id = domain.id,
        productId = domain.productId,
        targetYear = domain.targetYear,
        targetMonth = domain.targetMonth,
        targetUnits = domain.targetUnits,
        createdBy = domain.createdBy,
        createdAt = domain.createdAt ?: java.time.LocalDateTime.now(),
        updatedAt = domain.updatedAt ?: java.time.LocalDateTime.now()
    )
}

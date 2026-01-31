package com.emjay.backend.infrastructure.persistence.repository

import com.emjay.backend.domain.entity.product.ProductImage
import com.emjay.backend.domain.repository.product.ProductImageRepository
import com.emjay.backend.infrastructure.persistence.entity.ProductImageEntity
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
class ProductImageRepositoryImpl(
    private val jpaRepository: JpaProductImageRepository
) : ProductImageRepository {

    override fun save(productImage: ProductImage): ProductImage {
        val entity = toEntity(productImage)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findById(id: UUID): ProductImage? {
        return jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    }

    override fun findByProductId(productId: UUID): List<ProductImage> {
        return jpaRepository.findAllByProductIdOrderByDisplayOrderAsc(productId).map { toDomain(it) }
    }

    override fun findPrimaryByProductId(productId: UUID): ProductImage? {
        return jpaRepository.findByProductIdAndIsPrimaryTrue(productId)?.let { toDomain(it) }
    }

    @Transactional
    override fun deleteById(id: UUID) {
        jpaRepository.deleteById(id)
    }

    @Transactional
    override fun deleteAllByProductId(productId: UUID) {
        jpaRepository.deleteAllByProductId(productId)
    }

    private fun toDomain(entity: ProductImageEntity): ProductImage {
        return ProductImage(
            id = entity.id,
            productId = entity.productId,
            imageUrl = entity.imageUrl,
            fileName = entity.fileName,
            fileSize = entity.fileSize,
            mimeType = entity.mimeType,
            isPrimary = entity.isPrimary,
            displayOrder = entity.displayOrder,
            createdAt = entity.createdAt
        )
    }

    private fun toEntity(domain: ProductImage): ProductImageEntity {
        return ProductImageEntity(
            id = domain.id,
            productId = domain.productId,
            imageUrl = domain.imageUrl,
            fileName = domain.fileName,
            fileSize = domain.fileSize,
            mimeType = domain.mimeType,
            isPrimary = domain.isPrimary,
            displayOrder = domain.displayOrder,
            createdAt = domain.createdAt ?: java.time.LocalDateTime.now()
        )
    }
}
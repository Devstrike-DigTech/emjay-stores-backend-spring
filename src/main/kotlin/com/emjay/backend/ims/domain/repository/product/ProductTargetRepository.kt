package com.emjay.backend.ims.domain.repository.product

import com.emjay.backend.ims.domain.entity.product.ProductTarget
import java.util.UUID

interface ProductTargetRepository {
    fun save(target: ProductTarget): ProductTarget
    fun findByProductId(productId: UUID): List<ProductTarget>
    fun findByProductIdAndYearMonth(productId: UUID, year: Int, month: Int): ProductTarget?
    fun findCurrentTarget(productId: UUID): ProductTarget?
    fun delete(id: UUID)
}

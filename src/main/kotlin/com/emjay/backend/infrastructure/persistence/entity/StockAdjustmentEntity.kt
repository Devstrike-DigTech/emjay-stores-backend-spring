package com.emjay.backend.infrastructure.persistence.entity

import com.emjay.backend.domain.entity.inventory.AdjustmentType
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "stock_adjustments")
data class StockAdjustmentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "product_id", nullable = false)
    val productId: UUID,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "adjustment_type", nullable = false, columnDefinition = "adjustment_type")
    val adjustmentType: AdjustmentType,

    @Column(nullable = false)
    val quantity: Int,

    @Column(name = "previous_quantity", nullable = false)
    val previousQuantity: Int,

    @Column(name = "new_quantity", nullable = false)
    val newQuantity: Int,

    @Column(columnDefinition = "TEXT")
    val reason: String? = null,

    @Column(name = "sale_price", precision = 12, scale = 2)
    val salePrice: BigDecimal? = null,


    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        createdAt = LocalDateTime.now()
    }
}
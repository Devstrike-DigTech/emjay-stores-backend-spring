package com.emjay.backend.ims.infrastructure.persistence.entity

import com.emjay.backend.ims.domain.entity.product.ProductStatus
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "products")
data class ProductEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    
    @Column(nullable = false, unique = true, length = 100)
    val sku: String,
    
    @Column(nullable = false, length = 300)
    val name: String,
    
    @Column(columnDefinition = "TEXT")
    val description: String? = null,
    
    @Column(name = "category_id", nullable = false)
    val categoryId: UUID,
    
    @Column(name = "supplier_id")
    val supplierId: UUID? = null,
    
    @Column(name = "retail_price", nullable = false, precision = 12, scale = 2)
    val retailPrice: BigDecimal,
    
    @Column(name = "wholesale_price", precision = 12, scale = 2)
    val wholesalePrice: BigDecimal? = null,
    
    @Column(name = "cost_price", nullable = false, precision = 12, scale = 2)
    val costPrice: BigDecimal,
    
    @Column(name = "stock_quantity", nullable = false)
    val stockQuantity: Int = 0,
    
    @Column(name = "min_stock_threshold", nullable = false)
    val minStockThreshold: Int = 10,
    
    @Column(length = 200)
    val brand: String? = null,
    
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "product_status")
    val status: ProductStatus = ProductStatus.ACTIVE,
    
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        val now = LocalDateTime.now()
        createdAt = now
        updatedAt = now
    }
    
    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}

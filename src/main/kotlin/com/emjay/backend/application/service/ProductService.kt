package com.emjay.backend.application.service

import com.emjay.backend.application.dto.product.CreateProductRequest
import com.emjay.backend.application.dto.product.ProductPageResponse
import com.emjay.backend.application.dto.product.ProductResponse
import com.emjay.backend.application.dto.product.UpdateProductRequest
import com.emjay.backend.domain.entity.product.Product
import com.emjay.backend.domain.exception.ResourceAlreadyExistsException
import com.emjay.backend.domain.exception.ResourceNotFoundException
import com.emjay.backend.domain.repository.product.ProductImageRepository
import com.emjay.backend.domain.repository.product.ProductRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val productImageRepository: ProductImageRepository
) {

    @Transactional
    fun createProduct(request: CreateProductRequest): ProductResponse {
        // Check if SKU already exists
        if (productRepository.existsBySku(request.sku)) {
            throw ResourceAlreadyExistsException("Product with SKU '${request.sku}' already exists")
        }

        val product = Product(
            sku = request.sku,
            name = request.name,
            description = request.description,
            categoryId = request.categoryId,
            supplierId = request.supplierId,
            retailPrice = request.retailPrice,
            wholesalePrice = request.wholesalePrice,
            costPrice = request.costPrice,
            stockQuantity = request.stockQuantity,
            minStockThreshold = request.minStockThreshold,
            brand = request.brand,
            status = request.status
        )

        val saved = productRepository.save(product)
        return toProductResponse(saved)
    }

    fun getProductById(productId: UUID): ProductResponse {
        val product = productRepository.findById(productId)
            ?: throw ResourceNotFoundException("Product not found")
        return toProductResponse(product)
    }

    fun getProductBySku(sku: String): ProductResponse {
        val product = productRepository.findBySku(sku)
            ?: throw ResourceNotFoundException("Product with SKU '$sku' not found")
        return toProductResponse(product)
    }

    fun getAllProducts(page: Int = 0, size: Int = 20, sortBy: String = "name"): ProductPageResponse {
        val pageable = PageRequest.of(page, size, Sort.by(sortBy))
        val productsPage = productRepository.findAll(pageable)
        return toProductPageResponse(productsPage)
    }

    fun getProductsByCategory(categoryId: UUID, page: Int = 0, size: Int = 20): ProductPageResponse {
        val pageable = PageRequest.of(page, size, Sort.by("name"))
        val productsPage = productRepository.findByCategory(categoryId, pageable)
        return toProductPageResponse(productsPage)
    }

    fun getProductsBySupplier(supplierId: UUID, page: Int = 0, size: Int = 20): ProductPageResponse {
        val pageable = PageRequest.of(page, size, Sort.by("name"))
        val productsPage = productRepository.findBySupplier(supplierId, pageable)
        return toProductPageResponse(productsPage)
    }

    fun getLowStockProducts(page: Int = 0, size: Int = 20): ProductPageResponse {
        val pageable = PageRequest.of(page, size, Sort.by("stockQuantity"))
        val productsPage = productRepository.findLowStock(pageable)
        return toProductPageResponse(productsPage)
    }

    fun searchProducts(searchTerm: String, page: Int = 0, size: Int = 20): ProductPageResponse {
        val pageable = PageRequest.of(page, size, Sort.by("name"))
        val productsPage = productRepository.searchByName(searchTerm, pageable)
        return toProductPageResponse(productsPage)
    }

    @Transactional
    fun updateProduct(productId: UUID, request: UpdateProductRequest): ProductResponse {
        val product = productRepository.findById(productId)
            ?: throw ResourceNotFoundException("Product not found")

        val updatedProduct = product.copy(
            name = request.name ?: product.name,
            description = request.description ?: product.description,
            categoryId = request.categoryId ?: product.categoryId,
            supplierId = request.supplierId ?: product.supplierId,
            retailPrice = request.retailPrice ?: product.retailPrice,
            wholesalePrice = request.wholesalePrice ?: product.wholesalePrice,
            costPrice = request.costPrice ?: product.costPrice,
            stockQuantity = request.stockQuantity ?: product.stockQuantity,
            minStockThreshold = request.minStockThreshold ?: product.minStockThreshold,
            brand = request.brand ?: product.brand,
            status = request.status ?: product.status
        )

        val saved = productRepository.save(updatedProduct)
        return toProductResponse(saved)
    }

    @Transactional
    fun deleteProduct(productId: UUID) {
        if (!productRepository.findById(productId)?.let { true }!! ?: false) {
            throw ResourceNotFoundException("Product not found")
        }
        productRepository.deleteById(productId)
    }

    private fun toProductResponse(product: Product): ProductResponse {
        val images = productImageRepository.findByProductId(product.id!!)
            .map { image ->
                com.emjay.backend.application.dto.product.ProductImageInfo(
                    id = image.id.toString(),
                    imageUrl = image.imageUrl,
                    isPrimary = image.isPrimary,
                    displayOrder = image.displayOrder
                )
            }
        return ProductResponse(
            id = product.id.toString(),
            sku = product.sku,
            name = product.name,
            description = product.description,
            categoryId = product.categoryId.toString(),
            supplierId = product.supplierId?.toString(),
            retailPrice = product.retailPrice,
            wholesalePrice = product.wholesalePrice,
            costPrice = product.costPrice,
            stockQuantity = product.stockQuantity,
            minStockThreshold = product.minStockThreshold,
            brand = product.brand,
            status = product.status,
            isLowStock = product.isLowStock(),
            isOutOfStock = product.isOutOfStock(),
            profitMargin = product.profitMargin(),
            totalValue = product.totalValue(),
            images = images

        )
    }

    private fun toProductPageResponse(page: Page<Product>): ProductPageResponse {
        return ProductPageResponse(
            content = page.content.map { toProductResponse(it) },
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            currentPage = page.number,
            pageSize = page.size
        )
    }
}
package com.emjay.backend.application.service

import com.emjay.backend.domain.entity.product.ProductImage
import com.emjay.backend.domain.exception.ResourceNotFoundException
import com.emjay.backend.domain.repository.product.ProductImageRepository
import com.emjay.backend.domain.repository.product.ProductRepository
import com.emjay.backend.infrastructure.storage.FileStorageService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
class ProductImageService(
    private val productImageRepository: ProductImageRepository,
    private val productRepository: ProductRepository,
    private val fileStorageService: FileStorageService
) {

    @Transactional
    fun uploadProductImage(
        productId: UUID,
        file: MultipartFile,
        isPrimary: Boolean = false,
        displayOrder: Int = 0
    ): ProductImageResponse {
        // Verify product exists
        productRepository.findById(productId)
            ?: throw ResourceNotFoundException("Product not found")

        // If setting as primary, unset other primary images
        if (isPrimary) {
            val existingPrimary = productImageRepository.findPrimaryByProductId(productId)
            existingPrimary?.let {
                val updated = it.copy(isPrimary = false)
                productImageRepository.save(updated)
            }
        }

        // Upload file
        val uploadResult = fileStorageService.storeFile(file)

        // Save image record
        val productImage = ProductImage(
            productId = productId,
            imageUrl = uploadResult.fileUrl,
            fileName = uploadResult.fileName,
            fileSize = uploadResult.fileSize,
            mimeType = uploadResult.mimeType,
            isPrimary = isPrimary,
            displayOrder = displayOrder
        )

        val saved = productImageRepository.save(productImage)
        return toProductImageResponse(saved)
    }

    fun getProductImages(productId: UUID): List<ProductImageResponse> {
        return productImageRepository.findByProductId(productId).map { toProductImageResponse(it) }
    }

    fun getPrimaryImage(productId: UUID): ProductImageResponse? {
        return productImageRepository.findPrimaryByProductId(productId)?.let { toProductImageResponse(it) }
    }

    @Transactional
    fun setPrimaryImage(productId: UUID, imageId: UUID): ProductImageResponse {
        // Get the image
        val image = productImageRepository.findById(imageId)
            ?: throw ResourceNotFoundException("Image not found")

        if (image.productId != productId) {
            throw IllegalArgumentException("Image does not belong to this product")
        }

        // Unset current primary
        val currentPrimary = productImageRepository.findPrimaryByProductId(productId)
        currentPrimary?.let {
            productImageRepository.save(it.copy(isPrimary = false))
        }

        // Set new primary
        val updated = productImageRepository.save(image.copy(isPrimary = true))
        return toProductImageResponse(updated)
    }

    @Transactional
    fun deleteProductImage(productId: UUID, imageId: UUID) {
        val image = productImageRepository.findById(imageId)
            ?: throw ResourceNotFoundException("Image not found")

        if (image.productId != productId) {
            throw IllegalArgumentException("Image does not belong to this product")
        }

        // Delete file from storage
        fileStorageService.deleteFile(image.fileName)

        // Delete record
        productImageRepository.deleteById(imageId)
    }

    private fun toProductImageResponse(image: ProductImage): ProductImageResponse {
        return ProductImageResponse(
            id = image.id.toString(),
            productId = image.productId.toString(),
            imageUrl = image.imageUrl,
            fileName = image.fileName,
            fileSize = image.fileSize,
            mimeType = image.mimeType,
            isPrimary = image.isPrimary,
            displayOrder = image.displayOrder
        )
    }
}

data class ProductImageResponse(
    val id: String,
    val productId: String,
    val imageUrl: String,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val isPrimary: Boolean,
    val displayOrder: Int
)
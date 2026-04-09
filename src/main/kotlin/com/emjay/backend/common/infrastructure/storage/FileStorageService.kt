package com.emjay.backend.infrastructure.storage

import com.cloudinary.Cloudinary
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class FileStorageService(private val cloudinary: Cloudinary) {

    @Value("\${cloudinary.folder:emjay}")
    private lateinit var baseFolder: String

    private val allowedImageTypes = setOf("image/jpeg", "image/jpg", "image/png", "image/webp")
    private val maxFileSize = 5 * 1024 * 1024L // 5 MB

    /**
     * Upload file to the default 'products' folder in Cloudinary.
     */
    fun storeFile(file: MultipartFile): FileUploadResult {
        return storeFile(file, "products")
    }

    /**
     * Upload file to Cloudinary under [baseFolder]/[subdirectory].
     * Returns a [FileUploadResult] whose [FileUploadResult.fileName] and
     * [FileUploadResult.filePath] both hold the Cloudinary public_id — the
     * value needed later to delete the asset.
     */
    fun storeFile(file: MultipartFile, subdirectory: String): FileUploadResult {
        validateFile(file)

        val folder = "$baseFolder/$subdirectory"

        @Suppress("UNCHECKED_CAST")
        val result = cloudinary.uploader().upload(
            file.bytes,
            mapOf("folder" to folder, "resource_type" to "image")
        ) as Map<String, Any>

        val publicId  = result["public_id"]  as String
        val secureUrl = result["secure_url"] as String
        val bytes     = (result["bytes"] as? Number)?.toLong() ?: file.size

        return FileUploadResult(
            fileName         = publicId,   // stored in ProductImage.fileName / used for deletion
            originalFileName = file.originalFilename ?: "",
            filePath         = publicId,   // stored in StaffProfile.profileImageUrl / used for deletion
            fileUrl          = secureUrl,
            fileSize         = bytes,
            mimeType         = file.contentType ?: "image/jpeg"
        )
    }

    /**
     * Delete an asset from Cloudinary by its public_id.
     * Pass the value that was stored in [FileUploadResult.fileName] or [FileUploadResult.filePath].
     */
    fun deleteFile(publicId: String): Boolean {
        return try {
            cloudinary.uploader().destroy(publicId, emptyMap<String, Any>())
            true
        } catch (e: Exception) {
            false
        }
    }

    // ------------------------------------------------------------------ //

    private fun validateFile(file: MultipartFile) {
        if (file.isEmpty) throw IllegalArgumentException("Cannot upload an empty file")
        if (file.size > maxFileSize) throw IllegalArgumentException("File size exceeds the 5 MB limit")

        val contentType = file.contentType
        if (contentType == null || contentType !in allowedImageTypes) {
            throw IllegalArgumentException("Only JPEG, JPG, PNG, and WEBP images are allowed")
        }

        val name = file.originalFilename
        if (name == null || name.contains("..")) throw IllegalArgumentException("Invalid filename")
    }
}

data class FileUploadResult(
    val fileName: String,
    val originalFileName: String,
    val filePath: String,
    val fileUrl: String,
    val fileSize: Long,
    val mimeType: String
)

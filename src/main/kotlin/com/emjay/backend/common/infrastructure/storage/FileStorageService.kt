package com.emjay.backend.infrastructure.storage

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

@Service
class FileStorageService {
    
    @Value("\${file.upload-dir:uploads}")
    private lateinit var uploadDir: String
    
    private val allowedImageTypes = setOf("image/jpeg", "image/jpg", "image/png", "image/webp")
    private val maxFileSize = 5 * 1024 * 1024 // 5MB
    
    fun init() {
        try {
            val uploadPath = Paths.get(uploadDir)
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath)
            }
        } catch (e: IOException) {
            throw RuntimeException("Could not create upload directory!", e)
        }
    }
    
    /**
     * Store file in default 'products' subdirectory (for backward compatibility)
     */
    fun storeFile(file: MultipartFile): FileUploadResult {
        return storeFile(file, "products")
    }
    
    /**
     * Store file in specified subdirectory
     * @param file The file to upload
     * @param subdirectory The subdirectory within uploadDir (e.g., "products", "staff-profiles")
     */
    fun storeFile(file: MultipartFile, subdirectory: String): FileUploadResult {
        // Validate file
        validateFile(file)
        
        // Create subdirectory if it doesn't exist
        val targetDir = Paths.get(uploadDir, subdirectory)
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir)
        }
        
        // Generate unique filename
        val originalFilename = file.originalFilename ?: throw IllegalArgumentException("Filename cannot be null")
        val fileExtension = originalFilename.substringAfterLast(".", "")
        val uniqueFilename = "${UUID.randomUUID()}_${System.currentTimeMillis()}.$fileExtension"
        
        try {
            // Copy file to upload directory
            val targetLocation = targetDir.resolve(uniqueFilename)
            Files.copy(file.inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING)
            
            // Store relative path with subdirectory
            val filePath = "$subdirectory/$uniqueFilename"
            
            return FileUploadResult(
                fileName = uniqueFilename,
                originalFileName = originalFilename,
                filePath = filePath,
                fileUrl = "/uploads/$filePath",
                fileSize = file.size,
                mimeType = file.contentType ?: "application/octet-stream"
            )
        } catch (e: IOException) {
            throw RuntimeException("Failed to store file: ${e.message}", e)
        }
    }
    
    /**
     * Delete file given relative path (e.g., "products/abc.jpg" or "staff-profiles/xyz.jpg")
     */
    fun deleteFile(relativePath: String): Boolean {
        return try {
            val filePath = Paths.get(uploadDir).resolve(relativePath)
            Files.deleteIfExists(filePath)
        } catch (e: IOException) {
            false
        }
    }
    
    /**
     * Load file given relative path
     */
    fun loadFile(relativePath: String): Path {
        return Paths.get(uploadDir).resolve(relativePath).normalize()
    }
    
    private fun validateFile(file: MultipartFile) {
        // Check if file is empty
        if (file.isEmpty) {
            throw IllegalArgumentException("Cannot upload empty file")
        }
        
        // Check file size
        if (file.size > maxFileSize) {
            throw IllegalArgumentException("File size exceeds maximum limit of 5MB")
        }
        
        // Check file type
        val contentType = file.contentType
        if (contentType == null || contentType !in allowedImageTypes) {
            throw IllegalArgumentException("Only JPEG, JPG, PNG, and WEBP images are allowed")
        }
        
        // Check filename
        val originalFilename = file.originalFilename
        if (originalFilename == null || originalFilename.contains("..")) {
            throw IllegalArgumentException("Invalid filename")
        }
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

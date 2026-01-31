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
    
    @Value("\${file.upload-dir:uploads/products}")
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
    
    fun storeFile(file: MultipartFile): FileUploadResult {
        // Validate file
        validateFile(file)
        
        // Generate unique filename
        val originalFilename = file.originalFilename ?: throw IllegalArgumentException("Filename cannot be null")
        val fileExtension = originalFilename.substringAfterLast(".", "")
        val uniqueFilename = "${UUID.randomUUID()}_${System.currentTimeMillis()}.$fileExtension"
        
        try {
            // Copy file to upload directory
            val targetLocation = Paths.get(uploadDir).resolve(uniqueFilename)
            Files.copy(file.inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING)
            
            return FileUploadResult(
                fileName = uniqueFilename,
                originalFileName = originalFilename,
                fileUrl = "/uploads/products/$uniqueFilename",
                fileSize = file.size,
                mimeType = file.contentType ?: "application/octet-stream"
            )
        } catch (e: IOException) {
            throw RuntimeException("Failed to store file: ${e.message}", e)
        }
    }
    
    fun deleteFile(fileName: String): Boolean {
        return try {
            val filePath = Paths.get(uploadDir).resolve(fileName)
            Files.deleteIfExists(filePath)
        } catch (e: IOException) {
            false
        }
    }
    
    fun loadFile(fileName: String): Path {
        return Paths.get(uploadDir).resolve(fileName).normalize()
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
    val fileUrl: String,
    val fileSize: Long,
    val mimeType: String
)

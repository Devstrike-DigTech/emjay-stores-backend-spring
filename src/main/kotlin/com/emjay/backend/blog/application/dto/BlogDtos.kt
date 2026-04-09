package com.emjay.backend.blog.application.dto

import com.emjay.backend.blog.domain.entity.BlogLinkType
import com.emjay.backend.blog.domain.entity.BlogPostStatus
import com.emjay.backend.blog.domain.entity.VideoProvider
import jakarta.validation.constraints.*
import java.time.LocalDateTime
import java.util.*

// ========== CATEGORY DTOs ==========

data class CreateBlogCategoryRequest(
    @field:NotBlank(message = "Category name is required")
    val name: String,

    val description: String? = null,
    val parentId: UUID? = null,
    val displayOrder: Int = 0
)

data class BlogCategoryResponse(
    val id: String,
    val name: String,
    val slug: String,
    val description: String?,
    val parentId: String?,
    val displayOrder: Int,
    val isActive: Boolean,
    val postCount: Int = 0
)

// ========== TAG DTOs ==========

data class CreateBlogTagRequest(
    @field:NotBlank(message = "Tag name is required")
    val name: String
)

data class BlogTagResponse(
    val id: String,
    val name: String,
    val slug: String
)

// ========== POST DTOs ==========

data class CreateBlogPostRequest(
    @field:NotBlank(message = "Title is required")
    val title: String,

    val excerpt: String? = null,

    @field:NotBlank(message = "Content is required")
    val content: String,

    val categoryId: UUID? = null,
    val tagIds: List<UUID> = emptyList(),
    val featuredImageUrl: String? = null,
    val featuredImageAlt: String? = null,
    val status: BlogPostStatus = BlogPostStatus.DRAFT,
    val scheduledPublishAt: LocalDateTime? = null,
    val isFeatured: Boolean = false,
    val allowComments: Boolean = true,
    val metaTitle: String? = null,
    val metaDescription: String? = null,
    val metaKeywords: String? = null
)

data class UpdateBlogPostRequest(
    val title: String? = null,
    val excerpt: String? = null,
    val content: String? = null,
    val categoryId: UUID? = null,
    val featuredImageUrl: String? = null,
    val status: BlogPostStatus? = null,
    val isFeatured: Boolean? = null
)

data class BlogPostResponse(
    val id: String,
    val title: String,
    val slug: String,
    val excerpt: String?,
    val content: String,
    val featuredImageUrl: String?,
    val featuredImageAlt: String?,
    val category: BlogCategoryResponse?,
    val author: AuthorInfo,
    val tags: List<BlogTagResponse>,
    val images: List<BlogPostImageResponse>,
    val videos: List<BlogPostVideoResponse>,
    val links: List<BlogPostLinkResponse>,
    val status: BlogPostStatus,
    val publishedAt: LocalDateTime?,
    val viewCount: Int,
    val isFeatured: Boolean,
    val allowComments: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class BlogPostSummaryResponse(
    val id: String,
    val title: String,
    val slug: String,
    val excerpt: String?,
    val featuredImageUrl: String?,
    val categoryName: String?,
    val authorName: String,
    val status: BlogPostStatus,
    val publishedAt: LocalDateTime?,
    val viewCount: Int,
    val isFeatured: Boolean
)

data class AuthorInfo(
    val id: String,
    val name: String,
    val email: String? = null
)

// ========== IMAGE DTOs ==========

data class AddBlogPostImageRequest(
    @field:NotBlank(message = "Image URL is required")
    val imageUrl: String,

    val altText: String? = null,
    val caption: String? = null,
    val displayOrder: Int = 0
)

data class BlogPostImageResponse(
    val id: String,
    val imageUrl: String,
    val altText: String?,
    val caption: String?,
    val displayOrder: Int
)

// ========== VIDEO DTOs ==========

data class AddBlogPostVideoRequest(
    @field:NotBlank(message = "Video URL is required")
    val videoUrl: String,

    val videoProvider: VideoProvider = VideoProvider.YOUTUBE,
    val title: String? = null,
    val description: String? = null,
    val displayOrder: Int = 0
)

data class BlogPostVideoResponse(
    val id: String,
    val videoUrl: String,
    val embedUrl: String,  // Auto-generated embed URL
    val videoProvider: VideoProvider,
    val videoId: String?,
    val title: String?,
    val description: String?,
    val thumbnailUrl: String?,
    val displayOrder: Int
)

// ========== LINK DTOs ==========

data class AddBlogPostLinkRequest(
    @field:NotNull(message = "Link type is required")
    val linkType: BlogLinkType,

    @field:NotNull(message = "Link ID is required")
    val linkId: UUID,

    val displayText: String? = null,
    val context: String? = null,
    val displayOrder: Int = 0
)

data class BlogPostLinkResponse(
    val id: String,
    val linkType: BlogLinkType,
    val linkId: String,
    val linkUrl: String,  // Auto-generated URL
    val displayText: String?,
    val context: String?,
    val displayOrder: Int,
    val linkDetails: Any? = null  // Product/Service/Bundle/Promotion details
)

// ========== COMMENT DTOs ==========

data class CreateBlogCommentRequest(
    @field:NotBlank(message = "Author name is required")
    val authorName: String,

    @field:Email
    val authorEmail: String? = null,

    @field:NotBlank(message = "Comment content is required")
    val content: String,

    val parentCommentId: UUID? = null
)

data class BlogCommentResponse(
    val id: String,
    val postId: String,
    val authorName: String,
    val content: String,
    val isApproved: Boolean,
    val parentCommentId: String?,
    val replies: List<BlogCommentResponse> = emptyList(),
    val createdAt: LocalDateTime
)
package com.emjay.backend.blog.domain.entity

import java.time.LocalDateTime
import java.util.*

// ========== ENUMS ==========

enum class BlogPostStatus {
    DRAFT,
    PUBLISHED,
    SCHEDULED,
    ARCHIVED
}

enum class BlogLinkType {
    PRODUCT,
    SERVICE,
    BUNDLE,
    PROMOTION
}

enum class VideoProvider {
    YOUTUBE,
    VIMEO,
    CUSTOM
}

// ========== BLOG CATEGORY ==========

data class BlogCategory(
    val id: UUID? = null,
    val name: String,
    val slug: String,
    val description: String? = null,
    val parentId: UUID? = null,
    val displayOrder: Int = 0,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun isTopLevel(): Boolean = parentId == null
}

// ========== BLOG TAG ==========

data class BlogTag(
    val id: UUID? = null,
    val name: String,
    val slug: String,
    val createdAt: LocalDateTime? = null
)

// ========== BLOG POST ==========

data class BlogPost(
    val id: UUID? = null,
    val title: String,
    val slug: String,
    val excerpt: String? = null,
    val content: String,

    // Featured image
    val featuredImageUrl: String? = null,
    val featuredImageAlt: String? = null,

    // Category & Author
    val categoryId: UUID? = null,
    val authorId: UUID,

    // Status & Publishing
    val status: BlogPostStatus = BlogPostStatus.DRAFT,
    val publishedAt: LocalDateTime? = null,
    val scheduledPublishAt: LocalDateTime? = null,

    // Engagement
    val viewCount: Int = 0,
    val isFeatured: Boolean = false,
    val allowComments: Boolean = true,

    // SEO
    val metaTitle: String? = null,
    val metaDescription: String? = null,
    val metaKeywords: String? = null,

    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun isPublished(): Boolean = status == BlogPostStatus.PUBLISHED

    fun isDraft(): Boolean = status == BlogPostStatus.DRAFT

    fun isScheduled(): Boolean = status == BlogPostStatus.SCHEDULED

    fun canBePublished(): Boolean = status == BlogPostStatus.DRAFT || status == BlogPostStatus.SCHEDULED

    fun shouldPublishNow(): Boolean {
        return status == BlogPostStatus.SCHEDULED &&
                scheduledPublishAt != null &&
                LocalDateTime.now().isAfter(scheduledPublishAt)
    }

    fun incrementViewCount(): BlogPost = copy(viewCount = viewCount + 1)
}

// ========== BLOG POST TAG (Join) ==========

data class BlogPostTag(
    val id: UUID? = null,
    val postId: UUID,
    val tagId: UUID,
    val createdAt: LocalDateTime? = null
)

// ========== BLOG POST IMAGE ==========

data class BlogPostImage(
    val id: UUID? = null,
    val postId: UUID,
    val imageUrl: String,
    val altText: String? = null,
    val caption: String? = null,
    val displayOrder: Int = 0,
    val createdAt: LocalDateTime? = null
)

// ========== BLOG POST VIDEO ==========

data class BlogPostVideo(
    val id: UUID? = null,
    val postId: UUID,
    val videoUrl: String,
    val videoProvider: VideoProvider = VideoProvider.YOUTUBE,
    val videoId: String? = null,
    val title: String? = null,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val displayOrder: Int = 0,
    val createdAt: LocalDateTime? = null
) {
    fun getEmbedUrl(): String {
        return when (videoProvider) {
            VideoProvider.YOUTUBE -> {
                if (videoId != null) {
                    "https://www.youtube.com/embed/$videoId"
                } else {
                    extractYouTubeEmbedUrl(videoUrl)
                }
            }
            VideoProvider.VIMEO -> {
                if (videoId != null) {
                    "https://player.vimeo.com/video/$videoId"
                } else {
                    extractVimeoEmbedUrl(videoUrl)
                }
            }
            VideoProvider.CUSTOM -> videoUrl
        }
    }

    private fun extractYouTubeEmbedUrl(url: String): String {
        // Extract video ID from various YouTube URL formats
        val patterns = listOf(
            Regex("(?:youtube\\.com/watch\\?v=|youtu\\.be/)([^&\\s]+)"),
            Regex("youtube\\.com/embed/([^&\\s]+)")
        )

        for (pattern in patterns) {
            val match = pattern.find(url)
            if (match != null) {
                return "https://www.youtube.com/embed/${match.groupValues[1]}"
            }
        }
        return url
    }

    private fun extractVimeoEmbedUrl(url: String): String {
        val pattern = Regex("vimeo\\.com/(\\d+)")
        val match = pattern.find(url)
        return if (match != null) {
            "https://player.vimeo.com/video/${match.groupValues[1]}"
        } else url
    }
}

// ========== BLOG POST LINK ==========

data class BlogPostLink(
    val id: UUID? = null,
    val postId: UUID,
    val linkType: BlogLinkType,
    val linkId: UUID,
    val displayText: String? = null,
    val displayOrder: Int = 0,
    val context: String? = null,
    val createdAt: LocalDateTime? = null
) {
    fun getLinkUrl(): String {
        return when (linkType) {
            BlogLinkType.PRODUCT -> "/products/$linkId"
            BlogLinkType.SERVICE -> "/services/$linkId"
            BlogLinkType.BUNDLE -> "/bundles/$linkId"
            BlogLinkType.PROMOTION -> "/promotions/$linkId"
        }
    }
}

// ========== BLOG COMMENT ==========

data class BlogComment(
    val id: UUID? = null,
    val postId: UUID,
    val authorId: UUID? = null, // Null for guest comments
    val authorName: String,
    val authorEmail: String? = null,
    val content: String,
    val isApproved: Boolean = false,
    val isSpam: Boolean = false,
    val parentCommentId: UUID? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    fun isReply(): Boolean = parentCommentId != null

    fun needsModeration(): Boolean = !isApproved && !isSpam

    fun canBeDisplayed(): Boolean = isApproved && !isSpam
}
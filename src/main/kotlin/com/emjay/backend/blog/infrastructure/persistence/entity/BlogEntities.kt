package com.emjay.backend.blog.infrastructure.persistence.entity

import com.emjay.backend.blog.domain.entity.BlogLinkType
import com.emjay.backend.blog.domain.entity.BlogPostStatus
import com.emjay.backend.blog.domain.entity.VideoProvider
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime
import java.util.*

// ========== BLOG CATEGORY ENTITY ==========

@Entity
@Table(name = "blog_categories")
data class BlogCategoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(nullable = false, length = 200)
    val name: String,

    @Column(nullable = false, unique = true, length = 200)
    val slug: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "parent_id")
    val parentId: UUID? = null,

    @Column(name = "display_order")
    val displayOrder: Int = 0,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

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

// ========== BLOG TAG ENTITY ==========

@Entity
@Table(name = "blog_tags")
data class BlogTagEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(nullable = false, unique = true, length = 100)
    val name: String,

    @Column(nullable = false, unique = true, length = 100)
    val slug: String,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        createdAt = LocalDateTime.now()
    }
}

// ========== BLOG POST ENTITY ==========

@Entity
@Table(name = "blog_posts")
data class BlogPostEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(nullable = false, length = 300)
    val title: String,

    @Column(nullable = false, unique = true, length = 300)
    val slug: String,

    @Column(length = 500)
    val excerpt: String? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    val content: String,

    @Column(name = "featured_image_url", length = 500)
    val featuredImageUrl: String? = null,

    @Column(name = "featured_image_alt", columnDefinition = "TEXT")
    val featuredImageAlt: String? = null,

    @Column(name = "category_id")
    val categoryId: UUID? = null,

    @Column(name = "author_id", nullable = false)
    val authorId: UUID,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "blog_post_status")
    val status: BlogPostStatus = BlogPostStatus.DRAFT,

    @Column(name = "published_at")
    val publishedAt: LocalDateTime? = null,

    @Column(name = "scheduled_publish_at")
    val scheduledPublishAt: LocalDateTime? = null,

    @Column(name = "view_count", nullable = false)
    val viewCount: Int = 0,

    @Column(name = "is_featured", nullable = false)
    val isFeatured: Boolean = false,

    @Column(name = "allow_comments", nullable = false)
    val allowComments: Boolean = true,

    @Column(name = "meta_title", length = 200)
    val metaTitle: String? = null,

    @Column(name = "meta_description", length = 500)
    val metaDescription: String? = null,

    @Column(name = "meta_keywords", columnDefinition = "TEXT")
    val metaKeywords: String? = null,

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

// ========== BLOG POST TAG ENTITY ==========

@Entity
@Table(
    name = "blog_post_tags",
    uniqueConstraints = [UniqueConstraint(columnNames = ["post_id", "tag_id"])]
)
data class BlogPostTagEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "post_id", nullable = false)
    val postId: UUID,

    @Column(name = "tag_id", nullable = false)
    val tagId: UUID,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        createdAt = LocalDateTime.now()
    }
}

// ========== BLOG POST IMAGE ENTITY ==========

@Entity
@Table(name = "blog_post_images")
data class BlogPostImageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "post_id", nullable = false)
    val postId: UUID,

    @Column(name = "image_url", nullable = false, length = 500)
    val imageUrl: String,

    @Column(name = "alt_text", columnDefinition = "TEXT")
    val altText: String? = null,

    @Column(columnDefinition = "TEXT")
    val caption: String? = null,

    @Column(name = "display_order")
    val displayOrder: Int = 0,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        createdAt = LocalDateTime.now()
    }
}

// ========== BLOG POST VIDEO ENTITY ==========

@Entity
@Table(name = "blog_post_videos")
data class BlogPostVideoEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "post_id", nullable = false)
    val postId: UUID,

    @Column(name = "video_url", nullable = false, length = 500)
    val videoUrl: String,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "video_provider", nullable = false, columnDefinition = "blog_video_provider")
    val videoProvider: VideoProvider = VideoProvider.YOUTUBE,

    @Column(name = "video_id", length = 200)
    val videoId: String? = null,

    @Column(length = 300)
    val title: String? = null,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "thumbnail_url", length = 500)
    val thumbnailUrl: String? = null,

    @Column(name = "display_order")
    val displayOrder: Int = 0,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        createdAt = LocalDateTime.now()
    }
}

// ========== BLOG POST LINK ENTITY ==========

@Entity
@Table(name = "blog_post_links")
data class BlogPostLinkEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "post_id", nullable = false)
    val postId: UUID,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "link_type", nullable = false, columnDefinition = "blog_link_type")
    val linkType: BlogLinkType,

    @Column(name = "link_id", nullable = false)
    val linkId: UUID,

    @Column(name = "display_text", length = 300)
    val displayText: String? = null,

    @Column(name = "display_order")
    val displayOrder: Int = 0,

    @Column(columnDefinition = "TEXT")
    val context: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        createdAt = LocalDateTime.now()
    }
}

// ========== BLOG COMMENT ENTITY ==========

@Entity
@Table(name = "blog_comments")
data class BlogCommentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "post_id", nullable = false)
    val postId: UUID,

    @Column(name = "author_id")
    val authorId: UUID? = null,

    @Column(name = "author_name", nullable = false, length = 200)
    val authorName: String,

    @Column(name = "author_email", length = 200)
    val authorEmail: String? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    val content: String,

    @Column(name = "is_approved", nullable = false)
    val isApproved: Boolean = false,

    @Column(name = "is_spam", nullable = false)
    val isSpam: Boolean = false,

    @Column(name = "parent_comment_id")
    val parentCommentId: UUID? = null,

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
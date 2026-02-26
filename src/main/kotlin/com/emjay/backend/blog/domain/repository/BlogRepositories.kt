package com.emjay.backend.blog.domain.repository

import com.emjay.backend.blog.domain.entity.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.*

// ========== BLOG CATEGORY REPOSITORY ==========

interface BlogCategoryRepository {
    fun save(category: BlogCategory): BlogCategory
    fun findById(id: UUID): BlogCategory?
    fun findBySlug(slug: String): BlogCategory?
    fun findAll(): List<BlogCategory>
    fun findActive(): List<BlogCategory>
    fun findByParentId(parentId: UUID?): List<BlogCategory>
    fun delete(category: BlogCategory)
}

// ========== BLOG TAG REPOSITORY ==========

interface BlogTagRepository {
    fun save(tag: BlogTag): BlogTag
    fun findById(id: UUID): BlogTag?
    fun findBySlug(slug: String): BlogTag?
    fun findAll(): List<BlogTag>
    fun delete(tag: BlogTag)
}

// ========== BLOG POST REPOSITORY ==========

interface BlogPostRepository {
    fun save(post: BlogPost): BlogPost
    fun findById(id: UUID): BlogPost?
    fun findBySlug(slug: String): BlogPost?
    fun findAll(pageable: Pageable): Page<BlogPost>
    fun findByStatus(status: BlogPostStatus, pageable: Pageable): Page<BlogPost>
    fun findPublished(pageable: Pageable): Page<BlogPost>
    fun findByCategoryId(categoryId: UUID, pageable: Pageable): Page<BlogPost>
    fun findFeatured(): List<BlogPost>
    fun search(query: String, pageable: Pageable): Page<BlogPost>
    fun incrementViewCount(id: UUID)
    fun delete(post: BlogPost)
}

// ========== BLOG POST TAG REPOSITORY ==========

interface BlogPostTagRepository {
    fun save(postTag: BlogPostTag): BlogPostTag
    fun saveAll(postTags: List<BlogPostTag>): List<BlogPostTag>
    fun findByPostId(postId: UUID): List<BlogPostTag>
    fun findByTagId(tagId: UUID): List<BlogPostTag>
    fun deleteByPostId(postId: UUID)
}

// ========== BLOG POST IMAGE REPOSITORY ==========

interface BlogPostImageRepository {
    fun save(image: BlogPostImage): BlogPostImage
    fun saveAll(images: List<BlogPostImage>): List<BlogPostImage>
    fun findByPostId(postId: UUID): List<BlogPostImage>
    fun delete(image: BlogPostImage)
}

// ========== BLOG POST VIDEO REPOSITORY ==========

interface BlogPostVideoRepository {
    fun save(video: BlogPostVideo): BlogPostVideo
    fun saveAll(videos: List<BlogPostVideo>): List<BlogPostVideo>
    fun findByPostId(postId: UUID): List<BlogPostVideo>
    fun delete(video: BlogPostVideo)
}

// ========== BLOG POST LINK REPOSITORY ==========

interface BlogPostLinkRepository {
    fun save(link: BlogPostLink): BlogPostLink
    fun saveAll(links: List<BlogPostLink>): List<BlogPostLink>
    fun findByPostId(postId: UUID): List<BlogPostLink>
    fun findByLinkTypeAndLinkId(linkType: BlogLinkType, linkId: UUID): List<BlogPostLink>
    fun delete(link: BlogPostLink)
}

// ========== BLOG COMMENT REPOSITORY ==========

interface BlogCommentRepository {
    fun save(comment: BlogComment): BlogComment
    fun findById(id: UUID): BlogComment?
    fun findByPostId(postId: UUID): List<BlogComment>
    fun findApprovedByPostId(postId: UUID): List<BlogComment>
    fun findPendingModeration(): List<BlogComment>
    fun delete(comment: BlogComment)
}
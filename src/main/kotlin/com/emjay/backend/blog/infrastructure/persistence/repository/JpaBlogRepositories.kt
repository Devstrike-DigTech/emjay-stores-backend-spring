package com.emjay.backend.blog.infrastructure.persistence.repository

import com.emjay.backend.blog.domain.entity.BlogLinkType
import com.emjay.backend.blog.domain.entity.BlogPostStatus
import com.emjay.backend.blog.infrastructure.persistence.entity.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

// ========== BLOG CATEGORY ==========

@Repository
interface JpaBlogCategoryRepository : JpaRepository<BlogCategoryEntity, UUID> {
    fun findBySlug(slug: String): BlogCategoryEntity?
    fun findByIsActiveTrue(): List<BlogCategoryEntity>
    fun findByParentId(parentId: UUID?): List<BlogCategoryEntity>
}

// ========== BLOG TAG ==========

@Repository
interface JpaBlogTagRepository : JpaRepository<BlogTagEntity, UUID> {
    fun findBySlug(slug: String): BlogTagEntity?
}

// ========== BLOG POST ==========

@Repository
interface JpaBlogPostRepository : JpaRepository<BlogPostEntity, UUID> {
    fun findBySlug(slug: String): BlogPostEntity?
    fun findByStatus(status: BlogPostStatus, pageable: Pageable): Page<BlogPostEntity>

    @Query("SELECT p FROM BlogPostEntity p WHERE p.status = 'PUBLISHED' ORDER BY p.publishedAt DESC")
    fun findPublished(pageable: Pageable): Page<BlogPostEntity>

    fun findByCategoryId(categoryId: UUID, pageable: Pageable): Page<BlogPostEntity>
    fun findByIsFeaturedTrue(): List<BlogPostEntity>

    @Query("SELECT p FROM BlogPostEntity p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun searchByTitleOrContent(@Param("query") query: String, pageable: Pageable): Page<BlogPostEntity>

    @Modifying
    @Query("UPDATE BlogPostEntity p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    fun incrementViewCount(@Param("id") id: UUID)
}

// ========== BLOG POST TAG ==========

@Repository
interface JpaBlogPostTagRepository : JpaRepository<BlogPostTagEntity, UUID> {
    fun findByPostId(postId: UUID): List<BlogPostTagEntity>
    fun findByTagId(tagId: UUID): List<BlogPostTagEntity>
    fun deleteByPostId(postId: UUID)
}

// ========== BLOG POST IMAGE ==========

@Repository
interface JpaBlogPostImageRepository : JpaRepository<BlogPostImageEntity, UUID> {
    fun findByPostId(postId: UUID): List<BlogPostImageEntity>
}

// ========== BLOG POST VIDEO ==========

@Repository
interface JpaBlogPostVideoRepository : JpaRepository<BlogPostVideoEntity, UUID> {
    fun findByPostId(postId: UUID): List<BlogPostVideoEntity>
}

// ========== BLOG POST LINK ==========

@Repository
interface JpaBlogPostLinkRepository : JpaRepository<BlogPostLinkEntity, UUID> {
    fun findByPostId(postId: UUID): List<BlogPostLinkEntity>
    fun findByLinkTypeAndLinkId(linkType: BlogLinkType, linkId: UUID): List<BlogPostLinkEntity>
}

// ========== BLOG COMMENT ==========

@Repository
interface JpaBlogCommentRepository : JpaRepository<BlogCommentEntity, UUID> {
    fun findByPostId(postId: UUID): List<BlogCommentEntity>

    @Query("SELECT c FROM BlogCommentEntity c WHERE c.postId = :postId AND c.isApproved = true AND c.isSpam = false")
    fun findApprovedByPostId(@Param("postId") postId: UUID): List<BlogCommentEntity>

    @Query("SELECT c FROM BlogCommentEntity c WHERE c.isApproved = false AND c.isSpam = false")
    fun findPendingModeration(): List<BlogCommentEntity>
}
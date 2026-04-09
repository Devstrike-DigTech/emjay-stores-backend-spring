package com.emjay.backend.blog.infrastructure.persistence.repository

import com.emjay.backend.blog.domain.entity.*
import com.emjay.backend.blog.domain.repository.*
import com.emjay.backend.blog.infrastructure.persistence.entity.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

// ========== BLOG CATEGORY REPOSITORY IMPL ==========

@Repository
class BlogCategoryRepositoryImpl(
    private val jpaRepository: JpaBlogCategoryRepository
) : BlogCategoryRepository {

    override fun save(category: BlogCategory): BlogCategory {
        val entity = toEntity(category)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findById(id: UUID): BlogCategory? =
        jpaRepository.findById(id).map { toDomain(it) }.orElse(null)

    override fun findBySlug(slug: String): BlogCategory? =
        jpaRepository.findBySlug(slug)?.let { toDomain(it) }

    override fun findAll(): List<BlogCategory> =
        jpaRepository.findAll().map { toDomain(it) }

    override fun findActive(): List<BlogCategory> =
        jpaRepository.findByIsActiveTrue().map { toDomain(it) }

    override fun findByParentId(parentId: UUID?): List<BlogCategory> =
        jpaRepository.findByParentId(parentId).map { toDomain(it) }

    override fun delete(category: BlogCategory) {
        category.id?.let { jpaRepository.deleteById(it) }
    }

    private fun toDomain(entity: BlogCategoryEntity) = BlogCategory(
        id = entity.id,
        name = entity.name,
        slug = entity.slug,
        description = entity.description,
        parentId = entity.parentId,
        displayOrder = entity.displayOrder,
        isActive = entity.isActive,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )

    private fun toEntity(domain: BlogCategory) = BlogCategoryEntity(
        id = domain.id,
        name = domain.name,
        slug = domain.slug,
        description = domain.description,
        parentId = domain.parentId,
        displayOrder = domain.displayOrder,
        isActive = domain.isActive,
        createdAt = domain.createdAt ?: LocalDateTime.now(),
        updatedAt = domain.updatedAt ?: LocalDateTime.now()
    )
}

// ========== BLOG TAG REPOSITORY IMPL ==========

@Repository
class BlogTagRepositoryImpl(
    private val jpaRepository: JpaBlogTagRepository
) : BlogTagRepository {

    override fun save(tag: BlogTag) = toDomain(jpaRepository.save(toEntity(tag)))
    override fun findById(id: UUID) = jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    override fun findBySlug(slug: String) = jpaRepository.findBySlug(slug)?.let { toDomain(it) }
    override fun findAll() = jpaRepository.findAll().map { toDomain(it) }
    override fun delete(tag: BlogTag) { tag.id?.let { jpaRepository.deleteById(it) } }

    private fun toDomain(entity: BlogTagEntity) = BlogTag(
        id = entity.id, name = entity.name, slug = entity.slug, createdAt = entity.createdAt
    )

    private fun toEntity(domain: BlogTag) = BlogTagEntity(
        id = domain.id, name = domain.name, slug = domain.slug,
        createdAt = domain.createdAt ?: LocalDateTime.now()
    )
}

// ========== BLOG POST REPOSITORY IMPL ==========

@Repository
class BlogPostRepositoryImpl(
    private val jpaRepository: JpaBlogPostRepository
) : BlogPostRepository {

    override fun save(post: BlogPost): BlogPost {
        val entity = toEntity(post)
        val saved = jpaRepository.save(entity)
        return toDomain(saved)
    }

    override fun findById(id: UUID): BlogPost? =
        jpaRepository.findById(id).map { toDomain(it) }.orElse(null)

    override fun findBySlug(slug: String): BlogPost? =
        jpaRepository.findBySlug(slug)?.let { toDomain(it) }

    override fun findAll(pageable: Pageable): Page<BlogPost> =
        jpaRepository.findAll(pageable).map { toDomain(it) }

    override fun findByStatus(status: BlogPostStatus, pageable: Pageable): Page<BlogPost> =
        jpaRepository.findByStatus(status, pageable).map { toDomain(it) }

    override fun findPublished(pageable: Pageable): Page<BlogPost> =
        jpaRepository.findPublished(pageable).map { toDomain(it) }

    override fun findByCategoryId(categoryId: UUID, pageable: Pageable): Page<BlogPost> =
        jpaRepository.findByCategoryId(categoryId, pageable).map { toDomain(it) }

    override fun findFeatured(): List<BlogPost> =
        jpaRepository.findByIsFeaturedTrue().map { toDomain(it) }

    override fun search(query: String, pageable: Pageable): Page<BlogPost> =
        jpaRepository.searchByTitleOrContent(query, pageable).map { toDomain(it) }

    @Transactional
    override fun incrementViewCount(id: UUID) {
        jpaRepository.incrementViewCount(id)
    }

    override fun delete(post: BlogPost) {
        post.id?.let { jpaRepository.deleteById(it) }
    }

    private fun toDomain(entity: BlogPostEntity) = BlogPost(
        id = entity.id,
        title = entity.title,
        slug = entity.slug,
        excerpt = entity.excerpt,
        content = entity.content,
        featuredImageUrl = entity.featuredImageUrl,
        featuredImageAlt = entity.featuredImageAlt,
        categoryId = entity.categoryId,
        authorId = entity.authorId,
        status = entity.status,
        publishedAt = entity.publishedAt,
        scheduledPublishAt = entity.scheduledPublishAt,
        viewCount = entity.viewCount,
        isFeatured = entity.isFeatured,
        allowComments = entity.allowComments,
        metaTitle = entity.metaTitle,
        metaDescription = entity.metaDescription,
        metaKeywords = entity.metaKeywords,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )

    private fun toEntity(domain: BlogPost) = BlogPostEntity(
        id = domain.id,
        title = domain.title,
        slug = domain.slug,
        excerpt = domain.excerpt,
        content = domain.content,
        featuredImageUrl = domain.featuredImageUrl,
        featuredImageAlt = domain.featuredImageAlt,
        categoryId = domain.categoryId,
        authorId = domain.authorId,
        status = domain.status,
        publishedAt = domain.publishedAt,
        scheduledPublishAt = domain.scheduledPublishAt,
        viewCount = domain.viewCount,
        isFeatured = domain.isFeatured,
        allowComments = domain.allowComments,
        metaTitle = domain.metaTitle,
        metaDescription = domain.metaDescription,
        metaKeywords = domain.metaKeywords,
        createdAt = domain.createdAt ?: LocalDateTime.now(),
        updatedAt = domain.updatedAt ?: LocalDateTime.now()
    )
}

// ========== OTHER REPOSITORY IMPLEMENTATIONS ==========
// Following same pattern for: BlogPostTag, BlogPostImage, BlogPostVideo, BlogPostLink, BlogComment

@Repository
class BlogPostTagRepositoryImpl(
    private val jpaRepository: JpaBlogPostTagRepository
) : BlogPostTagRepository {
    override fun save(postTag: BlogPostTag) = toDomain(jpaRepository.save(toEntity(postTag)))
    override fun saveAll(postTags: List<BlogPostTag>) =
        jpaRepository.saveAll(postTags.map { toEntity(it) }).map { toDomain(it) }
    override fun findByPostId(postId: UUID) = jpaRepository.findByPostId(postId).map { toDomain(it) }
    override fun findByTagId(tagId: UUID) = jpaRepository.findByTagId(tagId).map { toDomain(it) }
    @Transactional
    override fun deleteByPostId(postId: UUID) = jpaRepository.deleteByPostId(postId)

    private fun toDomain(entity: BlogPostTagEntity) = BlogPostTag(
        id = entity.id, postId = entity.postId, tagId = entity.tagId, createdAt = entity.createdAt
    )
    private fun toEntity(domain: BlogPostTag) = BlogPostTagEntity(
        id = domain.id, postId = domain.postId, tagId = domain.tagId,
        createdAt = domain.createdAt ?: LocalDateTime.now()
    )
}

@Repository
class BlogPostImageRepositoryImpl(
    private val jpaRepository: JpaBlogPostImageRepository
) : BlogPostImageRepository {
    override fun save(image: BlogPostImage) = toDomain(jpaRepository.save(toEntity(image)))
    override fun saveAll(images: List<BlogPostImage>) =
        jpaRepository.saveAll(images.map { toEntity(it) }).map { toDomain(it) }
    override fun findByPostId(postId: UUID) = jpaRepository.findByPostId(postId).map { toDomain(it) }
    override fun delete(image: BlogPostImage) { image.id?.let { jpaRepository.deleteById(it) } }

    private fun toDomain(entity: BlogPostImageEntity) = BlogPostImage(
        id = entity.id, postId = entity.postId, imageUrl = entity.imageUrl,
        altText = entity.altText, caption = entity.caption,
        displayOrder = entity.displayOrder, createdAt = entity.createdAt
    )
    private fun toEntity(domain: BlogPostImage) = BlogPostImageEntity(
        id = domain.id, postId = domain.postId, imageUrl = domain.imageUrl,
        altText = domain.altText, caption = domain.caption,
        displayOrder = domain.displayOrder, createdAt = domain.createdAt ?: LocalDateTime.now()
    )
}

@Repository
class BlogPostVideoRepositoryImpl(
    private val jpaRepository: JpaBlogPostVideoRepository
) : BlogPostVideoRepository {
    override fun save(video: BlogPostVideo) = toDomain(jpaRepository.save(toEntity(video)))
    override fun saveAll(videos: List<BlogPostVideo>) =
        jpaRepository.saveAll(videos.map { toEntity(it) }).map { toDomain(it) }
    override fun findByPostId(postId: UUID) = jpaRepository.findByPostId(postId).map { toDomain(it) }
    override fun delete(video: BlogPostVideo) { video.id?.let { jpaRepository.deleteById(it) } }

    private fun toDomain(entity: BlogPostVideoEntity) = BlogPostVideo(
        id = entity.id, postId = entity.postId, videoUrl = entity.videoUrl,
        videoProvider = entity.videoProvider, videoId = entity.videoId,
        title = entity.title, description = entity.description,
        thumbnailUrl = entity.thumbnailUrl, displayOrder = entity.displayOrder,
        createdAt = entity.createdAt
    )
    private fun toEntity(domain: BlogPostVideo) = BlogPostVideoEntity(
        id = domain.id, postId = domain.postId, videoUrl = domain.videoUrl,
        videoProvider = domain.videoProvider, videoId = domain.videoId,
        title = domain.title, description = domain.description,
        thumbnailUrl = domain.thumbnailUrl, displayOrder = domain.displayOrder,
        createdAt = domain.createdAt ?: LocalDateTime.now()
    )
}

@Repository
class BlogPostLinkRepositoryImpl(
    private val jpaRepository: JpaBlogPostLinkRepository
) : BlogPostLinkRepository {
    override fun save(link: BlogPostLink) = toDomain(jpaRepository.save(toEntity(link)))
    override fun saveAll(links: List<BlogPostLink>) =
        jpaRepository.saveAll(links.map { toEntity(it) }).map { toDomain(it) }
    override fun findByPostId(postId: UUID) = jpaRepository.findByPostId(postId).map { toDomain(it) }
    override fun findByLinkTypeAndLinkId(linkType: BlogLinkType, linkId: UUID) =
        jpaRepository.findByLinkTypeAndLinkId(linkType, linkId).map { toDomain(it) }
    override fun delete(link: BlogPostLink) { link.id?.let { jpaRepository.deleteById(it) } }

    private fun toDomain(entity: BlogPostLinkEntity) = BlogPostLink(
        id = entity.id, postId = entity.postId, linkType = entity.linkType,
        linkId = entity.linkId, displayText = entity.displayText,
        displayOrder = entity.displayOrder, context = entity.context, createdAt = entity.createdAt
    )
    private fun toEntity(domain: BlogPostLink) = BlogPostLinkEntity(
        id = domain.id, postId = domain.postId, linkType = domain.linkType,
        linkId = domain.linkId, displayText = domain.displayText,
        displayOrder = domain.displayOrder, context = domain.context,
        createdAt = domain.createdAt ?: LocalDateTime.now()
    )
}

@Repository
class BlogCommentRepositoryImpl(
    private val jpaRepository: JpaBlogCommentRepository
) : BlogCommentRepository {
    override fun save(comment: BlogComment) = toDomain(jpaRepository.save(toEntity(comment)))
    override fun findById(id: UUID) = jpaRepository.findById(id).map { toDomain(it) }.orElse(null)
    override fun findByPostId(postId: UUID) = jpaRepository.findByPostId(postId).map { toDomain(it) }
    override fun findApprovedByPostId(postId: UUID) =
        jpaRepository.findApprovedByPostId(postId).map { toDomain(it) }
    override fun findPendingModeration() = jpaRepository.findPendingModeration().map { toDomain(it) }
    override fun delete(comment: BlogComment) { comment.id?.let { jpaRepository.deleteById(it) } }

    private fun toDomain(entity: BlogCommentEntity) = BlogComment(
        id = entity.id, postId = entity.postId, authorId = entity.authorId,
        authorName = entity.authorName, authorEmail = entity.authorEmail,
        content = entity.content, isApproved = entity.isApproved,
        isSpam = entity.isSpam, parentCommentId = entity.parentCommentId,
        createdAt = entity.createdAt, updatedAt = entity.updatedAt
    )
    private fun toEntity(domain: BlogComment) = BlogCommentEntity(
        id = domain.id, postId = domain.postId, authorId = domain.authorId,
        authorName = domain.authorName, authorEmail = domain.authorEmail,
        content = domain.content, isApproved = domain.isApproved,
        isSpam = domain.isSpam, parentCommentId = domain.parentCommentId,
        createdAt = domain.createdAt ?: LocalDateTime.now(),
        updatedAt = domain.updatedAt ?: LocalDateTime.now()
    )
}
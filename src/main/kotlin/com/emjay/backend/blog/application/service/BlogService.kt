package com.emjay.backend.blog.application.service

import com.emjay.backend.blog.application.dto.*
import com.emjay.backend.blog.domain.entity.*
import com.emjay.backend.blog.domain.repository.*
import com.emjay.backend.domain.exception.ResourceNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class BlogService(
    private val postRepository: BlogPostRepository,
    private val categoryRepository: BlogCategoryRepository,
    private val tagRepository: BlogTagRepository,
    private val postTagRepository: BlogPostTagRepository,
    private val postImageRepository: BlogPostImageRepository,
    private val postVideoRepository: BlogPostVideoRepository,
    private val postLinkRepository: BlogPostLinkRepository,
    private val commentRepository: BlogCommentRepository
) {

    // ========== CATEGORIES ==========

    @Transactional
    fun createCategory(request: CreateBlogCategoryRequest): BlogCategoryResponse {
        val slug = generateSlug(request.name)

        val category = BlogCategory(
            name = request.name,
            slug = slug,
            description = request.description,
            parentId = request.parentId,
            displayOrder = request.displayOrder
        )

        val saved = categoryRepository.save(category)
        return BlogCategoryResponse(
            id = saved.id.toString(),
            name = saved.name,
            slug = saved.slug,
            description = saved.description,
            parentId = saved.parentId?.toString(),
            displayOrder = saved.displayOrder,
            isActive = saved.isActive
        )
    }

    fun getCategories(): List<BlogCategoryResponse> {
        return categoryRepository.findActive().map { category ->
            BlogCategoryResponse(
                id = category.id.toString(),
                name = category.name,
                slug = category.slug,
                description = category.description,
                parentId = category.parentId?.toString(),
                displayOrder = category.displayOrder,
                isActive = category.isActive
            )
        }
    }

    // ========== TAGS ==========

    @Transactional
    fun createTag(request: CreateBlogTagRequest): BlogTagResponse {
        val slug = generateSlug(request.name)

        val tag = BlogTag(
            name = request.name,
            slug = slug
        )

        val saved = tagRepository.save(tag)
        return BlogTagResponse(
            id = saved.id.toString(),
            name = saved.name,
            slug = saved.slug
        )
    }

    fun getTags(): List<BlogTagResponse> {
        return tagRepository.findAll().map { tag ->
            BlogTagResponse(
                id = tag.id.toString(),
                name = tag.name,
                slug = tag.slug
            )
        }
    }

    // ========== POSTS ==========

    @Transactional
    fun createPost(request: CreateBlogPostRequest, authorId: UUID): BlogPostResponse {
        val slug = generateSlug(request.title)

        val post = BlogPost(
            title = request.title,
            slug = slug,
            excerpt = request.excerpt,
            content = request.content,
            categoryId = request.categoryId,
            authorId = authorId,
            featuredImageUrl = request.featuredImageUrl,
            featuredImageAlt = request.featuredImageAlt,
            status = request.status,
            scheduledPublishAt = request.scheduledPublishAt,
            isFeatured = request.isFeatured,
            allowComments = request.allowComments,
            metaTitle = request.metaTitle,
            metaDescription = request.metaDescription,
            metaKeywords = request.metaKeywords
        )

        val saved = postRepository.save(post)

        // Save tags
        if (request.tagIds.isNotEmpty()) {
            val postTags = request.tagIds.map { tagId ->
                BlogPostTag(
                    postId = saved.id!!,
                    tagId = tagId
                )
            }
            postTagRepository.saveAll(postTags)
        }

        return toPostResponse(saved)
    }

    fun getPublishedPosts(page: Int = 0, size: Int = 20): Page<BlogPostSummaryResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending())
        return postRepository.findPublished(pageable).map { toPostSummary(it) }
    }

    fun getPost(slug: String): BlogPostResponse {
        val post = postRepository.findBySlug(slug)
            ?: throw ResourceNotFoundException("Blog post not found")

        // Increment view count
        postRepository.incrementViewCount(post.id!!)

        return toPostResponse(post.incrementViewCount())
    }

    @Transactional
    fun updatePost(id: UUID, request: UpdateBlogPostRequest): BlogPostResponse {
        val post = postRepository.findById(id)
            ?: throw ResourceNotFoundException("Blog post not found")

        val updated = post.copy(
            title = request.title ?: post.title,
            excerpt = request.excerpt ?: post.excerpt,
            content = request.content ?: post.content,
            categoryId = request.categoryId ?: post.categoryId,
            featuredImageUrl = request.featuredImageUrl ?: post.featuredImageUrl,
            status = request.status ?: post.status,
            isFeatured = request.isFeatured ?: post.isFeatured
        )

        val saved = postRepository.save(updated)
        return toPostResponse(saved)
    }

    @Transactional
    fun publishPost(id: UUID): BlogPostResponse {
        val post = postRepository.findById(id)
            ?: throw ResourceNotFoundException("Blog post not found")

        if (!post.canBePublished()) {
            throw IllegalStateException("Post cannot be published in current status")
        }

        val published = post.copy(
            status = BlogPostStatus.PUBLISHED,
            publishedAt = LocalDateTime.now()
        )

        val saved = postRepository.save(published)
        return toPostResponse(saved)
    }

    fun searchPosts(query: String, page: Int = 0, size: Int = 20): Page<BlogPostSummaryResponse> {
        val pageable = PageRequest.of(page, size)
        return postRepository.search(query, pageable).map { toPostSummary(it) }
    }

    fun getPostsByCategory(categoryId: UUID, page: Int = 0, size: Int = 20): Page<BlogPostSummaryResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending())
        return postRepository.findByCategoryId(categoryId, pageable).map { toPostSummary(it) }
    }

    // ========== POST IMAGES ==========

    @Transactional
    fun addImage(postId: UUID, request: AddBlogPostImageRequest): BlogPostImageResponse {
        val post = postRepository.findById(postId)
            ?: throw ResourceNotFoundException("Blog post not found")

        val image = BlogPostImage(
            postId = postId,
            imageUrl = request.imageUrl,
            altText = request.altText,
            caption = request.caption,
            displayOrder = request.displayOrder
        )

        val saved = postImageRepository.save(image)
        return BlogPostImageResponse(
            id = saved.id.toString(),
            imageUrl = saved.imageUrl,
            altText = saved.altText,
            caption = saved.caption,
            displayOrder = saved.displayOrder
        )
    }

    // ========== POST VIDEOS ==========

    @Transactional
    fun addVideo(postId: UUID, request: AddBlogPostVideoRequest): BlogPostVideoResponse {
        val post = postRepository.findById(postId)
            ?: throw ResourceNotFoundException("Blog post not found")

        val video = BlogPostVideo(
            postId = postId,
            videoUrl = request.videoUrl,
            videoProvider = request.videoProvider,
            title = request.title,
            description = request.description,
            displayOrder = request.displayOrder
        )

        val saved = postVideoRepository.save(video)
        return BlogPostVideoResponse(
            id = saved.id.toString(),
            videoUrl = saved.videoUrl,
            embedUrl = saved.getEmbedUrl(),
            videoProvider = saved.videoProvider,
            videoId = saved.videoId,
            title = saved.title,
            description = saved.description,
            thumbnailUrl = saved.thumbnailUrl,
            displayOrder = saved.displayOrder
        )
    }

    // ========== POST LINKS (Products/Services/Bundles/Promotions) ==========

    @Transactional
    fun addPostLink(postId: UUID, request: AddBlogPostLinkRequest): BlogPostLinkResponse {
        val post = postRepository.findById(postId)
            ?: throw ResourceNotFoundException("Blog post not found")

        val link = BlogPostLink(
            postId = postId,
            linkType = request.linkType,
            linkId = request.linkId,
            displayText = request.displayText,
            context = request.context,
            displayOrder = request.displayOrder
        )

        val saved = postLinkRepository.save(link)
        return BlogPostLinkResponse(
            id = saved.id.toString(),
            linkType = saved.linkType,
            linkId = saved.linkId.toString(),
            linkUrl = saved.getLinkUrl(),
            displayText = saved.displayText,
            context = saved.context,
            displayOrder = saved.displayOrder,
            linkDetails = null  // TODO: Fetch product/service/bundle details
        )
    }

    // ========== COMMENTS ==========

    @Transactional
    fun addComment(postId: UUID, request: CreateBlogCommentRequest, customerId: UUID?): BlogCommentResponse {
        val post = postRepository.findById(postId)
            ?: throw ResourceNotFoundException("Blog post not found")

        if (!post.allowComments) {
            throw IllegalStateException("Comments are not allowed on this post")
        }

        val comment = BlogComment(
            postId = postId,
            authorId = customerId,
            authorName = request.authorName,
            authorEmail = request.authorEmail,
            content = request.content,
            parentCommentId = request.parentCommentId,
            isApproved = false  // Requires moderation
        )

        val saved = commentRepository.save(comment)
        return toCommentResponse(saved)
    }

    fun getApprovedComments(postId: UUID): List<BlogCommentResponse> {
        return commentRepository.findApprovedByPostId(postId).map { toCommentResponse(it) }
    }

    // ========== HELPER METHODS ==========

    private fun generateSlug(name: String): String {
        return name.lowercase()
            .replace(Regex("[^a-z0-9\\s-]"), "")
            .replace(Regex("\\s+"), "-")
            .trim('-')
    }

    private fun toPostResponse(post: BlogPost): BlogPostResponse {
        val tags = postTagRepository.findByPostId(post.id!!).mapNotNull { postTag ->
            tagRepository.findById(postTag.tagId)
        }
        val images = postImageRepository.findByPostId(post.id)
        val videos = postVideoRepository.findByPostId(post.id)
        val links = postLinkRepository.findByPostId(post.id)

        return BlogPostResponse(
            id = post.id.toString(),
            title = post.title,
            slug = post.slug,
            excerpt = post.excerpt,
            content = post.content,
            featuredImageUrl = post.featuredImageUrl,
            featuredImageAlt = post.featuredImageAlt,
            category = post.categoryId?.let { categoryRepository.findById(it) }?.let { cat ->
                BlogCategoryResponse(
                    id = cat.id.toString(),
                    name = cat.name,
                    slug = cat.slug,
                    description = cat.description,
                    parentId = cat.parentId?.toString(),
                    displayOrder = cat.displayOrder,
                    isActive = cat.isActive
                )
            },
            author = AuthorInfo(
                id = post.authorId.toString(),
                name = "Author"  // TODO: Fetch from user service
            ),
            tags = tags.map { tag ->
                BlogTagResponse(
                    id = tag.id.toString(),
                    name = tag.name,
                    slug = tag.slug
                )
            },
            images = images.map { img ->
                BlogPostImageResponse(
                    id = img.id.toString(),
                    imageUrl = img.imageUrl,
                    altText = img.altText,
                    caption = img.caption,
                    displayOrder = img.displayOrder
                )
            },
            videos = videos.map { video ->
                BlogPostVideoResponse(
                    id = video.id.toString(),
                    videoUrl = video.videoUrl,
                    embedUrl = video.getEmbedUrl(),
                    videoProvider = video.videoProvider,
                    videoId = video.videoId,
                    title = video.title,
                    description = video.description,
                    thumbnailUrl = video.thumbnailUrl,
                    displayOrder = video.displayOrder
                )
            },
            links = links.map { link ->
                BlogPostLinkResponse(
                    id = link.id.toString(),
                    linkType = link.linkType,
                    linkId = link.linkId.toString(),
                    linkUrl = link.getLinkUrl(),
                    displayText = link.displayText,
                    context = link.context,
                    displayOrder = link.displayOrder
                )
            },
            status = post.status,
            publishedAt = post.publishedAt,
            viewCount = post.viewCount,
            isFeatured = post.isFeatured,
            allowComments = post.allowComments,
            createdAt = post.createdAt!!,
            updatedAt = post.updatedAt!!
        )
    }

    private fun toPostSummary(post: BlogPost): BlogPostSummaryResponse {
        return BlogPostSummaryResponse(
            id = post.id.toString(),
            title = post.title,
            slug = post.slug,
            excerpt = post.excerpt,
            featuredImageUrl = post.featuredImageUrl,
            categoryName = post.categoryId?.let { categoryRepository.findById(it)?.name },
            authorName = "Author",  // TODO: Fetch
            status = post.status,
            publishedAt = post.publishedAt,
            viewCount = post.viewCount,
            isFeatured = post.isFeatured
        )
    }

    private fun toCommentResponse(comment: BlogComment): BlogCommentResponse {
        return BlogCommentResponse(
            id = comment.id.toString(),
            postId = comment.postId.toString(),
            authorName = comment.authorName,
            content = comment.content,
            isApproved = comment.isApproved,
            parentCommentId = comment.parentCommentId?.toString(),
            createdAt = comment.createdAt!!
        )
    }
}
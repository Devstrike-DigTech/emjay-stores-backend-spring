package com.emjay.backend.blog.presentation.controller

import com.emjay.backend.blog.application.dto.*
import com.emjay.backend.blog.application.service.BlogService
import com.emjay.backend.common.infrastructure.security.jwt.JwtTokenProvider
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/blog")
@Tag(name = "Blog", description = "Blog and CMS management")
class BlogController(
    private val blogService: BlogService,
    private val jwtUtil: JwtTokenProvider
) {

    // ========== CATEGORIES ==========

    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create blog category")
    fun createCategory(
        @Valid @RequestBody request: CreateBlogCategoryRequest
    ): ResponseEntity<BlogCategoryResponse> {
        val response = blogService.createCategory(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all categories (public)")
    fun getCategories(): ResponseEntity<List<BlogCategoryResponse>> {
        val categories = blogService.getCategories()
        return ResponseEntity.ok(categories)
    }

    // ========== TAGS ==========

    @PostMapping("/tags")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create blog tag")
    fun createTag(
        @Valid @RequestBody request: CreateBlogTagRequest
    ): ResponseEntity<BlogTagResponse> {
        val response = blogService.createTag(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/tags")
    @Operation(summary = "Get all tags (public)")
    fun getTags(): ResponseEntity<List<BlogTagResponse>> {
        val tags = blogService.getTags()
        return ResponseEntity.ok(tags)
    }

    // ========== POSTS ==========

    @PostMapping("/posts")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create blog post")
    fun createPost(
        @Valid @RequestBody request: CreateBlogPostRequest,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<BlogPostResponse> {
        val authorId = extractUserIdFromToken(token)
        val response = blogService.createPost(request, authorId)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/posts")
    @Operation(summary = "Get published posts (public)")
    fun getPosts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<BlogPostSummaryResponse>> {
        val posts = blogService.getPublishedPosts(page, size)
        return ResponseEntity.ok(posts)
    }

    @GetMapping("/posts/{slug}")
    @Operation(summary = "View blog post by slug (public)")
    fun getPost(@PathVariable slug: String): ResponseEntity<BlogPostResponse> {
        val post = blogService.getPost(slug)
        return ResponseEntity.ok(post)
    }

    @PutMapping("/posts/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update blog post")
    fun updatePost(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateBlogPostRequest
    ): ResponseEntity<BlogPostResponse> {
        val response = blogService.updatePost(id, request)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/posts/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Publish blog post")
    fun publishPost(@PathVariable id: UUID): ResponseEntity<BlogPostResponse> {
        val response = blogService.publishPost(id)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/posts/{postId}/images")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Add image to blog post")
    fun addImage(
        @PathVariable postId: UUID,
        @Valid @RequestBody request: AddBlogPostImageRequest
    ): ResponseEntity<BlogPostImageResponse> {
        val response = blogService.addImage(postId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/posts/{postId}/videos")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Add video to blog post")
    fun addVideo(
        @PathVariable postId: UUID,
        @Valid @RequestBody request: AddBlogPostVideoRequest
    ): ResponseEntity<BlogPostVideoResponse> {
        val response = blogService.addVideo(postId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/posts/{postId}/links")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Add product/service/bundle/promotion link to blog post")
    fun addLink(
        @PathVariable postId: UUID,
        @Valid @RequestBody request: AddBlogPostLinkRequest
    ): ResponseEntity<BlogPostLinkResponse> {
        val response = blogService.addPostLink(postId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    // ========== SEARCH & FILTER ==========

    @GetMapping("/search")
    @Operation(summary = "Search blog posts (public)")
    fun searchPosts(
        @RequestParam q: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<BlogPostSummaryResponse>> {
        val results = blogService.searchPosts(q, page, size)
        return ResponseEntity.ok(results)
    }

    @GetMapping("/posts/category/{categoryId}")
    @Operation(summary = "Get posts by category (public)")
    fun getPostsByCategory(
        @PathVariable categoryId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<BlogPostSummaryResponse>> {
        val posts = blogService.getPostsByCategory(categoryId, page, size)
        return ResponseEntity.ok(posts)
    }

    // ========== COMMENTS ==========

    @PostMapping("/posts/{postId}/comments")
    @Operation(summary = "Add comment to blog post (public or authenticated)")
    fun addComment(
        @PathVariable postId: UUID,
        @Valid @RequestBody request: CreateBlogCommentRequest,
        @RequestHeader(value = "Authorization", required = false) token: String?
    ): ResponseEntity<BlogCommentResponse> {
        val customerId = token?.let { extractUserIdFromToken(it) }
        val response = blogService.addComment(postId, request, customerId)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/posts/{postId}/comments")
    @Operation(summary = "Get approved comments for post (public)")
    fun getComments(@PathVariable postId: UUID): ResponseEntity<List<BlogCommentResponse>> {
        val comments = blogService.getApprovedComments(postId)
        return ResponseEntity.ok(comments)
    }

    // ========== HELPER ==========

    private fun extractUserIdFromToken(token: String): UUID {
        val jwt = token.removePrefix("Bearer ")
        val userId = jwtUtil.getUserIdFromToken(jwt)
        return UUID.fromString(userId.toString())
    }
}
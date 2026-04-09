package com.emjay.backend.services.application.service

import com.emjay.backend.domain.exception.ResourceNotFoundException
import com.emjay.backend.services.application.dto.*
import com.emjay.backend.services.domain.entity.*
import com.emjay.backend.services.domain.repository.*
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class ServiceManagementService(
    private val serviceCategoryRepository: ServiceCategoryRepository,
    private val serviceSubcategoryRepository: ServiceSubcategoryRepository,
    private val serviceRepository: ServiceRepository,
    private val serviceImageRepository: ServiceImageRepository,
    private val serviceAddonRepository: ServiceAddonRepository,
    private val serviceStaffRepository: ServiceStaffRepository
) {

    // ========== CATEGORIES ==========

    @Transactional
    fun createCategory(request: CreateServiceCategoryRequest): ServiceCategoryResponse {
        val slug = generateSlug(request.name)

        if (serviceCategoryRepository.existsBySlug(slug)) {
            throw IllegalArgumentException("Category with this name already exists")
        }

        val category = ServiceCategory(
            name = request.name,
            slug = slug,
            description = request.description,
            imageUrl = request.imageUrl,
            displayOrder = request.displayOrder
        )

        val saved = serviceCategoryRepository.save(category)
        return toCategoryResponse(saved)
    }

    fun getCategories(): List<ServiceCategoryResponse> {
        return serviceCategoryRepository.findAllActive()
            .sortedBy { it.displayOrder }
            .map { toCategoryResponse(it) }
    }

    @Transactional
    fun updateCategory(id: UUID, request: UpdateServiceCategoryRequest): ServiceCategoryResponse {
        val category = serviceCategoryRepository.findById(id)
            ?: throw ResourceNotFoundException("Category not found")

        val updated = category.copy(
            name = request.name ?: category.name,
            description = request.description ?: category.description,
            imageUrl = request.imageUrl ?: category.imageUrl,
            displayOrder = request.displayOrder ?: category.displayOrder,
            isActive = request.isActive ?: category.isActive
        )

        val saved = serviceCategoryRepository.save(updated)
        return toCategoryResponse(saved)
    }

    // ========== SUBCATEGORIES ==========

    @Transactional
    fun createSubcategory(
        categoryId: UUID,
        request: CreateServiceSubcategoryRequest
    ): ServiceSubcategoryResponse {
        val category = serviceCategoryRepository.findById(categoryId)
            ?: throw ResourceNotFoundException("Category not found")

        val slug = generateSlug(request.name)

        val subcategory = ServiceSubcategory(
            categoryId = categoryId,
            name = request.name,
            slug = slug,
            description = request.description,
            displayOrder = request.displayOrder
        )

        val saved = serviceSubcategoryRepository.save(subcategory)
        return toSubcategoryResponse(saved)
    }

    fun getSubcategories(categoryId: UUID): List<ServiceSubcategoryResponse> {
        return serviceSubcategoryRepository.findByCategoryIdAndActive(categoryId)
            .sortedBy { it.displayOrder }
            .map { toSubcategoryResponse(it) }
    }

    // ========== SERVICES ==========

    @Transactional
    fun createService(request: CreateServiceRequest): ServiceResponse {
        val slug = generateSlug(request.name)

        val service = com.emjay.backend.services.domain.entity.Service(
            categoryId = request.categoryId,
            subcategoryId = request.subcategoryId,
            name = request.name,
            slug = slug,
            description = request.description,
            shortDescription = request.shortDescription,
            basePrice = request.basePrice,
            discountedPrice = request.discountedPrice,
            durationMinutes = request.durationMinutes,
            bufferTimeMinutes = request.bufferTimeMinutes,
            skillLevel = request.skillLevel,
            maxClientsPerSlot = request.maxClientsPerSlot,
            requiresConsultation = request.requiresConsultation,
            isFeatured = request.isFeatured,
            metaTitle = request.metaTitle,
            metaDescription = request.metaDescription,
            metaKeywords = request.metaKeywords
        )

        val saved = serviceRepository.save(service)
        return toServiceResponse(saved)
    }

    fun getService(id: UUID): ServiceResponse {
        val service = serviceRepository.findById(id)
            ?: throw ResourceNotFoundException("Service not found")
        return toServiceResponse(service)
    }

    fun getServices(page: Int = 0, size: Int = 20): ServiceListResponse {
        val pageable = PageRequest.of(page, size, Sort.by("name"))
        val services = serviceRepository.findAll(pageable)

        return ServiceListResponse(
            content = services.content.map { toServiceSummary(it) },
            totalElements = services.totalElements,
            totalPages = services.totalPages,
            currentPage = page,
            pageSize = size
        )
    }

    @Transactional
    fun updateService(id: UUID, request: UpdateServiceRequest): ServiceResponse {
        val service = serviceRepository.findById(id)
            ?: throw ResourceNotFoundException("Service not found")

        val updated = service.copy(
            name = request.name ?: service.name,
            categoryId = request.categoryId ?: service.categoryId,
            subcategoryId = request.subcategoryId ?: service.subcategoryId,
            description = request.description ?: service.description,
            shortDescription = request.shortDescription ?: service.shortDescription,
            basePrice = request.basePrice ?: service.basePrice,
            discountedPrice = request.discountedPrice ?: service.discountedPrice,
            durationMinutes = request.durationMinutes ?: service.durationMinutes,
            bufferTimeMinutes = request.bufferTimeMinutes ?: service.bufferTimeMinutes,
            skillLevel = request.skillLevel ?: service.skillLevel,
            status = request.status ?: service.status,
            isFeatured = request.isFeatured ?: service.isFeatured,
            requiresConsultation = request.requiresConsultation ?: service.requiresConsultation
        )

        val saved = serviceRepository.save(updated)
        return toServiceResponse(saved)
    }

    // ========== SERVICE IMAGES ==========

    @Transactional
    fun addServiceImage(serviceId: UUID, request: AddServiceImageRequest): ServiceImageResponse {
        val service = serviceRepository.findById(serviceId)
            ?: throw ResourceNotFoundException("Service not found")

        val image = ServiceImage(
            serviceId = serviceId,
            imageUrl = request.imageUrl,
            altText = request.altText,
            displayOrder = request.displayOrder,
            isPrimary = request.isPrimary
        )

        val saved = serviceImageRepository.save(image)
        return toImageResponse(saved)
    }

    // ========== SERVICE ADDONS ==========

    @Transactional
    fun addServiceAddon(serviceId: UUID, request: CreateServiceAddonRequest): ServiceAddonResponse {
        val service = serviceRepository.findById(serviceId)
            ?: throw ResourceNotFoundException("Service not found")

        val addon = ServiceAddon(
            serviceId = serviceId,
            name = request.name,
            description = request.description,
            price = request.price,
            durationMinutes = request.durationMinutes
        )

        val saved = serviceAddonRepository.save(addon)
        return toAddonResponse(saved)
    }

    // ========== STAFF ASSIGNMENT ==========

    @Transactional
    fun assignStaffToService(serviceId: UUID, request: AssignStaffToServiceRequest): StaffAssignmentResponse {
        val service = serviceRepository.findById(serviceId)
            ?: throw ResourceNotFoundException("Service not found")

        if (serviceStaffRepository.existsByServiceAndStaff(serviceId, request.staffId)) {
            throw IllegalArgumentException("Staff already assigned to this service")
        }

        val assignment = ServiceStaff(
            serviceId = serviceId,
            staffId = request.staffId,
            isPrimary = request.isPrimary
        )

        val saved = serviceStaffRepository.save(assignment)
        return StaffAssignmentResponse(
            id = saved.id.toString(),
            staffId = saved.staffId.toString(),
            staffName = "Staff Member", // TODO: Fetch from user service
            isPrimary = saved.isPrimary,
            assignedAt = saved.assignedAt!!
        )
    }

    // ========== HELPER METHODS ==========

    private fun generateSlug(name: String): String {
        return name.lowercase()
            .replace(Regex("[^a-z0-9\\s-]"), "")
            .replace(Regex("\\s+"), "-")
            .trim('-')
    }

    private fun toCategoryResponse(category: ServiceCategory) = ServiceCategoryResponse(
        id = category.id.toString(),
        name = category.name,
        slug = category.slug,
        description = category.description,
        imageUrl = category.imageUrl,
        displayOrder = category.displayOrder,
        isActive = category.isActive,
        createdAt = category.createdAt!!
    )

    private fun toSubcategoryResponse(subcategory: ServiceSubcategory) = ServiceSubcategoryResponse(
        id = subcategory.id.toString(),
        categoryId = subcategory.categoryId.toString(),
        name = subcategory.name,
        slug = subcategory.slug,
        description = subcategory.description,
        displayOrder = subcategory.displayOrder,
        isActive = subcategory.isActive
    )

    private fun toServiceResponse(service: com.emjay.backend.services.domain.entity.Service): ServiceResponse {
        val images = serviceImageRepository.findByServiceId(service.id!!)
        val addons = serviceAddonRepository.findActiveByServiceId(service.id)
        val staff = serviceStaffRepository.findByServiceId(service.id)

        return ServiceResponse(
            id = service.id.toString(),
            categoryId = service.categoryId.toString(),
            categoryName = "Category", // TODO: Fetch
            subcategoryId = service.subcategoryId?.toString(),
            subcategoryName = null,
            name = service.name,
            slug = service.slug,
            description = service.description,
            shortDescription = service.shortDescription,
            basePrice = service.basePrice,
            discountedPrice = service.discountedPrice,
            currentPrice = service.currentPrice(),
            hasDiscount = service.hasDiscount(),
            discountPercentage = service.discountPercentage(),
            durationMinutes = service.durationMinutes,
            bufferTimeMinutes = service.bufferTimeMinutes,
            totalTimeMinutes = service.totalTimeMinutes(),
            skillLevel = service.skillLevel,
            status = service.status,
            isFeatured = service.isFeatured,
            requiresConsultation = service.requiresConsultation,
            images = images.map { toImageResponse(it) },
            addons = addons.map { toAddonResponse(it) },
            assignedStaff = emptyList(), // TODO: Populate
            createdAt = service.createdAt!!
        )
    }

    private fun toServiceSummary(service: com.emjay.backend.services.domain.entity.Service): ServiceSummaryResponse {
        val primaryImage = serviceImageRepository.findPrimaryImage(service.id!!)

        return ServiceSummaryResponse(
            id = service.id.toString(),
            name = service.name,
            slug = service.slug,
            categoryName = "Category",
            shortDescription = service.shortDescription,
            currentPrice = service.currentPrice(),
            hasDiscount = service.hasDiscount(),
            durationMinutes = service.durationMinutes,
            primaryImage = primaryImage?.imageUrl,
            isFeatured = service.isFeatured,
            status = service.status
        )
    }

    private fun toImageResponse(image: ServiceImage) = ServiceImageResponse(
        id = image.id.toString(),
        imageUrl = image.imageUrl,
        altText = image.altText,
        displayOrder = image.displayOrder,
        isPrimary = image.isPrimary
    )

    private fun toAddonResponse(addon: ServiceAddon) = ServiceAddonResponse(
        id = addon.id.toString(),
        name = addon.name,
        description = addon.description,
        price = addon.price,
        durationMinutes = addon.durationMinutes,
        isActive = addon.isActive
    )
}
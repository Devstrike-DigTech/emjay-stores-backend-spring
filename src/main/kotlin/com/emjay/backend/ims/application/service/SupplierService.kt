package com.emjay.backend.ims.application.service

import com.emjay.backend.ims.application.dto.supplier.*
import com.emjay.backend.ims.domain.entity.supplier.Supplier
import com.emjay.backend.domain.exception.ResourceAlreadyExistsException
import com.emjay.backend.domain.exception.ResourceNotFoundException
import com.emjay.backend.ims.domain.repository.product.ProductRepository
import com.emjay.backend.ims.domain.repository.supplier.SupplierRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class SupplierService(
    private val supplierRepository: SupplierRepository,
    private val productRepository: ProductRepository
) {

    @Transactional
    fun createSupplier(request: CreateSupplierRequest): SupplierResponse {
        // Check if supplier name already exists
        if (supplierRepository.existsByName(request.name)) {
            throw ResourceAlreadyExistsException("Supplier '${request.name}' already exists")
        }

        val supplier = Supplier(
            name = request.name,
            contactPerson = request.contactPerson,
            email = request.email,
            phone = request.phone,
            address = request.address,
            paymentTerms = request.paymentTerms,
            isActive = true
        )

        val saved = supplierRepository.save(supplier)
        return toSupplierResponse(saved)
    }

    fun getSupplierById(supplierId: UUID): SupplierResponse {
        val supplier = supplierRepository.findById(supplierId)
            ?: throw ResourceNotFoundException("Supplier not found")
        return toSupplierResponse(supplier)
    }

    fun getAllSuppliers(): SupplierListResponse {
        val suppliers = supplierRepository.findAll(PageRequest.of(0, 1000)).content
        val responses = suppliers.map { toSupplierResponse(it) }
        return SupplierListResponse(
            suppliers = responses,
            totalCount = responses.size
        )
    }

    fun getActiveSuppliers(): SupplierListResponse {
        val suppliers = supplierRepository.findAllActive()
        val responses = suppliers.map { toSupplierResponse(it) }
        return SupplierListResponse(
            suppliers = responses,
            totalCount = responses.size
        )
    }

    @Transactional
    fun updateSupplier(supplierId: UUID, request: UpdateSupplierRequest): SupplierResponse {
        val supplier = supplierRepository.findById(supplierId)
            ?: throw ResourceNotFoundException("Supplier not found")

        // Check if new name conflicts with existing supplier
        request.name?.let { newName ->
            if (newName != supplier.name && supplierRepository.existsByName(newName)) {
                throw ResourceAlreadyExistsException("Supplier '$newName' already exists")
            }
        }

        val updatedSupplier = supplier.copy(
            name = request.name ?: supplier.name,
            contactPerson = request.contactPerson ?: supplier.contactPerson,
            email = request.email ?: supplier.email,
            phone = request.phone ?: supplier.phone,
            address = request.address ?: supplier.address,
            paymentTerms = request.paymentTerms ?: supplier.paymentTerms,
            isActive = request.isActive ?: supplier.isActive
        )

        val saved = supplierRepository.save(updatedSupplier)
        return toSupplierResponse(saved)
    }

    @Transactional
    fun deleteSupplier(supplierId: UUID) {
        val supplier = supplierRepository.findById(supplierId)
            ?: throw ResourceNotFoundException("Supplier not found")

        // Check if supplier has products
        val productCount = productRepository.findBySupplier(supplierId, PageRequest.of(0, 1)).totalElements
        if (productCount > 0) {
            throw IllegalStateException("Cannot delete supplier with products. Delete or reassign products first.")
        }

        supplierRepository.deleteById(supplierId)
    }

    private fun toSupplierResponse(supplier: Supplier): SupplierResponse {
        val supplierId = supplier.id ?: throw IllegalStateException("Supplier ID cannot be null")
        val productCount = productRepository.findBySupplier(supplierId, PageRequest.of(0, 1)).totalElements

        return SupplierResponse(
            id = supplierId.toString(),
            name = supplier.name,
            contactPerson = supplier.contactPerson,
            email = supplier.email,
            phone = supplier.phone,
            address = supplier.address,
            paymentTerms = supplier.paymentTerms,
            isActive = supplier.isActive,
            productCount = productCount.toInt()
        )
    }
}
package com.emjay.backend.domain.entity.user

/**
 * User roles in the system
 */
enum class UserRole {
    ADMIN,      // Full system access
    MANAGER,    // Inventory, staff, and order management
    STAFF       // Limited access to personal data and assigned tasks
}

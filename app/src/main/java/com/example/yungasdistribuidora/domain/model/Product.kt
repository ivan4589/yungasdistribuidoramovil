package com.example.yungasdistribuidora.domain.model

enum class AvailabilityStatus {
    AVAILABLE,
    LOW_STOCK,
    OUT_OF_STOCK
}

data class Product(
    val id: String,
    val code: String,
    val name: String,
    val description: String?,
    val providerId: String,
    val categoryId: String,
    val categoryName: String?,
    val subCategoryId: String?,
    val subCategoryName: String?,
    val weight: String?,
    val priceNormal: Double,
    val priceCamino: Double,
    val priceEspecial: Double,
    val priceMayorista: Double?,
    val minQuantityWholesale: Double?,
    val stock: Double,
    val centralStock: Double?,
    val centralReservedStock: Double?,
    val centralAvailableStock: Double?,
    val minStock: Double,
    val unit: String,
    val reserveQuantity: Double,
    val additionalInfo: String?,
    val imageUrl: String?,
    val isActive: Boolean,
    val deletedAt: String?,
    val createdAt: String,
    val updatedAt: String
) {
    val availableStock: Double
        get() = centralAvailableStock ?: maxOf((centralStock ?: stock) - (centralReservedStock ?: 0.0), 0.0)

    val availabilityStatus: AvailabilityStatus
        get() = when {
            availableStock > minStock -> AvailabilityStatus.AVAILABLE
            availableStock > 0.0 -> AvailabilityStatus.LOW_STOCK
            else -> AvailabilityStatus.OUT_OF_STOCK
        }
}

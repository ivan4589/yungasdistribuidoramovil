package com.example.yungasdistribuidora.data.local.product

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val code: String,
    val name: String,
    val description: String?,
    val providerId: String,
    val categoryId: String,
    val subCategoryId: String?,
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
)

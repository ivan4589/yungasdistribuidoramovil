package com.example.yungasdistribuidora.data.remote.dto.product

import com.google.gson.annotations.SerializedName

data class ProductDto(
    @SerializedName("id") val id: String,
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("providerId") val providerId: String,
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("subCategoryId") val subCategoryId: String?,
    @SerializedName("weight") val weight: String?,
    @SerializedName("priceNormal") val priceNormal: Double,
    @SerializedName("priceCamino") val priceCamino: Double,
    @SerializedName("priceEspecial") val priceEspecial: Double,
    @SerializedName("priceMayorista") val priceMayorista: Double?,
    @SerializedName("minQuantityWholesale") val minQuantityWholesale: Double?,
    @SerializedName("stock") val stock: Double,
    @SerializedName("centralStock") val centralStock: Double?,
    @SerializedName("centralReservedStock") val centralReservedStock: Double?,
    @SerializedName("centralAvailableStock") val centralAvailableStock: Double?,
    @SerializedName("minStock") val minStock: Double,
    @SerializedName("unit") val unit: String,
    @SerializedName("reserveQuantity") val reserveQuantity: Double,
    @SerializedName("additionalInfo") val additionalInfo: String?,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("isActive") val isActive: Boolean,
    @SerializedName("deletedAt") val deletedAt: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

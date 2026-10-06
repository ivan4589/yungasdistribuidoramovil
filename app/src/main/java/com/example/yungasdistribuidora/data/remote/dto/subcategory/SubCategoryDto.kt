package com.example.yungasdistribuidora.data.remote.dto.subcategory

import com.google.gson.annotations.SerializedName

data class SubCategoryDto(
    @SerializedName("id") val id: String,
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("name") val name: String,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
)

package com.example.yungasdistribuidora.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BonusRuleDto(
    @SerializedName("id") val id: String,
    @SerializedName("subCategoryId") val subCategoryId: String,
    @SerializedName("minQuantity") val minQuantity: Int,
    @SerializedName("bonusQuantity") val bonusQuantity: Int,
    @SerializedName("startDate") val startDate: String?,
    @SerializedName("endDate") val endDate: String?,
    @SerializedName("isActive") val isActive: Boolean
)

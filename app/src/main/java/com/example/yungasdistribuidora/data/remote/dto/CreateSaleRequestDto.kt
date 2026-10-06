package com.example.yungasdistribuidora.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateSaleRequestDto(
    @SerializedName("clientId") val clientId: String,
    @SerializedName("details") val details: List<SaleDetailRequestDto>,
    @SerializedName("discount") val discount: Double?,
    @SerializedName("observations") val observations: String?,
    @SerializedName("saleType") val saleType: String,
    @SerializedName("dueDate") val dueDate: String?,
    @SerializedName("initialPayment") val initialPayment: Double?,
    @SerializedName("paymentMethod") val paymentMethod: String?,
    @SerializedName("paymentReference") val paymentReference: String?
)

data class SaleDetailRequestDto(
    @SerializedName("productId") val productId: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("unitPrice") val unitPrice: Double,
    @SerializedName("manualPrice") val manualPrice: Boolean
)

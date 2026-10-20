package com.example.yungasdistribuidora.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SaleDetailResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("productId") val productId: String,
    @SerializedName("productName") val productName: String?,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("unitPrice") val unitPrice: Double,
    @SerializedName("manualPrice") val manualPrice: Boolean,
    @SerializedName("subtotal") val subtotal: Double
)

data class SaleBonusResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("productId") val productId: String,
    @SerializedName("productName") val productName: String?,
    @SerializedName("quantity") val quantity: Int
)

data class SaleResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("saleNumber") val saleNumber: String,
    @SerializedName("clientId") val clientId: String,
    @SerializedName("clientName") val clientName: String?,
    @SerializedName("status") val status: String,
    @SerializedName("paymentStatus") val paymentStatus: String,
    @SerializedName("saleType") val saleType: String,
    @SerializedName("dueDate") val dueDate: String?,
    @SerializedName("subtotal") val subtotal: Double,
    @SerializedName("discount") val discount: Double,
    @SerializedName("total") val total: Double,
    @SerializedName("paidAmount") val paidAmount: Double,
    @SerializedName("balance") val balance: Double,
    @SerializedName("observations") val observations: String?,
    @SerializedName("pdfUrl") val pdfUrl: String?,
    @SerializedName("sellerId") val sellerId: String?,
    @SerializedName("sellerName") val sellerName: String?,
    @SerializedName("details") val details: List<SaleDetailResponseDto>?,
    @SerializedName("bonuses") val bonuses: List<SaleBonusResponseDto>?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

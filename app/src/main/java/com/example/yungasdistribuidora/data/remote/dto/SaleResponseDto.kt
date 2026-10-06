package com.example.yungasdistribuidora.data.remote.dto

import com.google.gson.annotations.SerializedName

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
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

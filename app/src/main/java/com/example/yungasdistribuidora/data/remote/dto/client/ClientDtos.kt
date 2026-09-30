package com.example.yungasdistribuidora.data.remote.dto.client

import com.google.gson.annotations.SerializedName

data class ClientDto(
    @SerializedName("id") val id: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("alias") val alias: String?,
    @SerializedName("type") val type: String,
    @SerializedName("locationId") val locationId: String,
    @SerializedName("locationName") val locationName: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("whatsappConsent") val whatsappConsent: Boolean,
    @SerializedName("additionalInfo") val additionalInfo: String?,
    @SerializedName("isActive") val isActive: Boolean,
    @SerializedName("deletedAt") val deletedAt: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

data class CreateClientRequest(
    @SerializedName("fullName") val fullName: String,
    @SerializedName("alias") val alias: String?,
    @SerializedName("type") val type: String,
    @SerializedName("locationId") val locationId: String,
    @SerializedName("phone") val phone: String?,
    @SerializedName("whatsappConsent") val whatsappConsent: Boolean,
    @SerializedName("additionalInfo") val additionalInfo: String?
)

data class DeactivateClientRequest(
    @SerializedName("reason") val reason: String
)

data class ReactivateClientRequest(
    @SerializedName("reason") val reason: String
)

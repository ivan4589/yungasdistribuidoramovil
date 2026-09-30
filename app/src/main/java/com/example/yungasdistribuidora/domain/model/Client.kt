package com.example.yungasdistribuidora.domain.model

data class Client(
    val id: String,
    val fullName: String,
    val alias: String?,
    val type: ClientType,
    val locationId: String,
    val locationName: String?,
    val phone: String?,
    val whatsappConsent: Boolean,
    val additionalInfo: String?,
    val isActive: Boolean,
    val deletedAt: String?,
    val createdAt: String,
    val updatedAt: String
)

package com.example.yungasdistribuidora.data.local.client

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val alias: String?,
    val type: String,
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

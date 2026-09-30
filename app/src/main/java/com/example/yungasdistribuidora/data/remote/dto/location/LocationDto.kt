package com.example.yungasdistribuidora.data.remote.dto.location

import com.google.gson.annotations.SerializedName

data class LocationDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
)

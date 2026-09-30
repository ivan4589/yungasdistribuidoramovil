package com.example.yungasdistribuidora.data.local.location

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAt: String?,
    val updatedAt: String?
)

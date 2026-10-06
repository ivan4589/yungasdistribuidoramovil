package com.example.yungasdistribuidora.data.local.subcategory

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subcategories")
data class SubCategoryEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val name: String,
    val createdAt: String?,
    val updatedAt: String?
)

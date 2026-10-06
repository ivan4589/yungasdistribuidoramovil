package com.example.yungasdistribuidora.data.mapper

import com.example.yungasdistribuidora.data.local.category.CategoryEntity
import com.example.yungasdistribuidora.data.remote.dto.category.CategoryDto
import com.example.yungasdistribuidora.domain.model.Category

fun CategoryDto.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = id,
        name = name,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun CategoryEntity.toDomain(): Category {
    return Category(
        id = id,
        name = name,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun CategoryDto.toDomain(): Category {
    return Category(
        id = id,
        name = name,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

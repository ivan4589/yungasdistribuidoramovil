package com.example.yungasdistribuidora.data.mapper

import com.example.yungasdistribuidora.data.local.subcategory.SubCategoryEntity
import com.example.yungasdistribuidora.data.remote.dto.subcategory.SubCategoryDto
import com.example.yungasdistribuidora.domain.model.SubCategory

fun SubCategoryDto.toEntity(): SubCategoryEntity {
    return SubCategoryEntity(
        id = id,
        categoryId = categoryId,
        name = name,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun SubCategoryEntity.toDomain(): SubCategory {
    return SubCategory(
        id = id,
        categoryId = categoryId,
        name = name,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun SubCategoryDto.toDomain(): SubCategory {
    return SubCategory(
        id = id,
        categoryId = categoryId,
        name = name,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

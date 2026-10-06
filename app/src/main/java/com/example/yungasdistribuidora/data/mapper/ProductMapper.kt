package com.example.yungasdistribuidora.data.mapper

import com.example.yungasdistribuidora.data.local.product.ProductEntity
import com.example.yungasdistribuidora.data.remote.dto.product.ProductDto
import com.example.yungasdistribuidora.domain.model.Product

fun ProductDto.toEntity(): ProductEntity {
    return ProductEntity(
        id = id,
        code = code,
        name = name,
        description = description,
        providerId = providerId,
        categoryId = categoryId,
        subCategoryId = subCategoryId,
        weight = weight,
        priceNormal = priceNormal,
        priceCamino = priceCamino,
        priceEspecial = priceEspecial,
        priceMayorista = priceMayorista,
        minQuantityWholesale = minQuantityWholesale,
        stock = stock,
        centralStock = centralStock,
        centralReservedStock = centralReservedStock,
        centralAvailableStock = centralAvailableStock,
        minStock = minStock,
        unit = unit,
        reserveQuantity = reserveQuantity,
        additionalInfo = additionalInfo,
        imageUrl = imageUrl,
        isActive = isActive,
        deletedAt = deletedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun ProductEntity.toDomain(categoryName: String?, subCategoryName: String?): Product {
    return Product(
        id = id,
        code = code,
        name = name,
        description = description,
        providerId = providerId,
        categoryId = categoryId,
        categoryName = categoryName,
        subCategoryId = subCategoryId,
        subCategoryName = subCategoryName,
        weight = weight,
        priceNormal = priceNormal,
        priceCamino = priceCamino,
        priceEspecial = priceEspecial,
        priceMayorista = priceMayorista,
        minQuantityWholesale = minQuantityWholesale,
        stock = stock,
        centralStock = centralStock,
        centralReservedStock = centralReservedStock,
        centralAvailableStock = centralAvailableStock,
        minStock = minStock,
        unit = unit,
        reserveQuantity = reserveQuantity,
        additionalInfo = additionalInfo,
        imageUrl = imageUrl,
        isActive = isActive,
        deletedAt = deletedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun ProductDto.toDomain(categoryName: String?, subCategoryName: String?): Product {
    return Product(
        id = id,
        code = code,
        name = name,
        description = description,
        providerId = providerId,
        categoryId = categoryId,
        categoryName = categoryName,
        subCategoryId = subCategoryId,
        subCategoryName = subCategoryName,
        weight = weight,
        priceNormal = priceNormal,
        priceCamino = priceCamino,
        priceEspecial = priceEspecial,
        priceMayorista = priceMayorista,
        minQuantityWholesale = minQuantityWholesale,
        stock = stock,
        centralStock = centralStock,
        centralReservedStock = centralReservedStock,
        centralAvailableStock = centralAvailableStock,
        minStock = minStock,
        unit = unit,
        reserveQuantity = reserveQuantity,
        additionalInfo = additionalInfo,
        imageUrl = imageUrl,
        isActive = isActive,
        deletedAt = deletedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

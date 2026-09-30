package com.example.yungasdistribuidora.data.mapper

import com.example.yungasdistribuidora.data.local.client.ClientEntity
import com.example.yungasdistribuidora.data.local.location.LocationEntity
import com.example.yungasdistribuidora.data.remote.dto.client.ClientDto
import com.example.yungasdistribuidora.data.remote.dto.location.LocationDto
import com.example.yungasdistribuidora.domain.model.Client
import com.example.yungasdistribuidora.domain.model.ClientType
import com.example.yungasdistribuidora.domain.model.Location

fun ClientDto.toEntity(): ClientEntity {
    return ClientEntity(
        id = id,
        fullName = fullName,
        alias = alias,
        type = type,
        locationId = locationId,
        locationName = locationName,
        phone = phone,
        whatsappConsent = whatsappConsent,
        additionalInfo = additionalInfo,
        isActive = isActive,
        deletedAt = deletedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun ClientEntity.toDomain(): Client {
    return Client(
        id = id,
        fullName = fullName,
        alias = alias,
        type = ClientType.fromString(type),
        locationId = locationId,
        locationName = locationName,
        phone = phone,
        whatsappConsent = whatsappConsent,
        additionalInfo = additionalInfo,
        isActive = isActive,
        deletedAt = deletedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun ClientDto.toDomain(): Client {
    return Client(
        id = id,
        fullName = fullName,
        alias = alias,
        type = ClientType.fromString(type),
        locationId = locationId,
        locationName = locationName,
        phone = phone,
        whatsappConsent = whatsappConsent,
        additionalInfo = additionalInfo,
        isActive = isActive,
        deletedAt = deletedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun LocationDto.toEntity(): LocationEntity {
    return LocationEntity(
        id = id,
        name = name,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun LocationEntity.toDomain(): Location {
    return Location(
        id = id,
        name = name,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun LocationDto.toDomain(): Location {
    return Location(
        id = id,
        name = name,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

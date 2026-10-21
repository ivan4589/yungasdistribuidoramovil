package com.example.yungasdistribuidora.data.mapper

import com.example.yungasdistribuidora.data.local.sale.SaleBonusEntity
import com.example.yungasdistribuidora.data.local.sale.SaleDetailEntity
import com.example.yungasdistribuidora.data.local.sale.SaleEntity
import com.example.yungasdistribuidora.data.remote.dto.SaleBonusResponseDto
import com.example.yungasdistribuidora.data.remote.dto.SaleDetailResponseDto
import com.example.yungasdistribuidora.data.remote.dto.SaleResponseDto

object SaleMapper {
    fun toEntity(dto: SaleResponseDto): SaleEntity {
        return SaleEntity(
            id = dto.id,
            saleNumber = dto.saleNumber,
            clientId = dto.clientId,
            clientName = dto.clientName,
            status = dto.status,
            paymentStatus = dto.paymentStatus,
            saleType = dto.saleType,
            dueDate = dto.dueDate,
            subtotal = dto.subtotal,
            discount = dto.discount,
            total = dto.total,
            paidAmount = dto.paidAmount,
            balance = dto.balance,
            observations = dto.observations,
            pdfUrl = dto.pdfUrl,
            sellerId = dto.sellerId,
            sellerName = dto.sellerName,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt
        )
    }

    fun toDetailEntities(saleId: String, details: List<SaleDetailResponseDto>?): List<SaleDetailEntity> {
        return details?.map { detail ->
            SaleDetailEntity(
                id = detail.id,
                saleId = saleId,
                productId = detail.productId,
                productName = detail.productName,
                quantity = detail.quantity,
                unitPrice = detail.unitPrice,
                manualPrice = detail.manualPrice,
                subtotal = detail.subtotal
            )
        } ?: emptyList()
    }

    fun toBonusEntities(saleId: String, bonuses: List<SaleBonusResponseDto>?): List<SaleBonusEntity> {
        return bonuses?.map { bonus ->
            SaleBonusEntity(
                id = bonus.id,
                saleId = saleId,
                productId = bonus.productId,
                productName = bonus.productName,
                quantity = bonus.quantity
            )
        } ?: emptyList()
    }

    fun toResponseDto(
        entity: SaleEntity,
        details: List<SaleDetailEntity>,
        bonuses: List<SaleBonusEntity>
    ): SaleResponseDto {
        return SaleResponseDto(
            id = entity.id,
            saleNumber = entity.saleNumber,
            clientId = entity.clientId,
            clientName = entity.clientName,
            status = entity.status,
            paymentStatus = entity.paymentStatus,
            saleType = entity.saleType,
            dueDate = entity.dueDate,
            subtotal = entity.subtotal,
            discount = entity.discount,
            total = entity.total,
            paidAmount = entity.paidAmount,
            balance = entity.balance,
            observations = entity.observations,
            pdfUrl = entity.pdfUrl,
            sellerId = entity.sellerId,
            sellerName = entity.sellerName,
            details = details.map {
                SaleDetailResponseDto(
                    id = it.id,
                    productId = it.productId,
                    productName = it.productName,
                    quantity = it.quantity,
                    unitPrice = it.unitPrice,
                    manualPrice = it.manualPrice,
                    subtotal = it.subtotal
                )
            },
            bonuses = bonuses.map {
                SaleBonusResponseDto(
                    id = it.id,
                    productId = it.productId,
                    productName = it.productName,
                    quantity = it.quantity
                )
            },
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}

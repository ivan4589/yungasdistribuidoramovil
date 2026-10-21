package com.example.yungasdistribuidora.data.mapper

import com.example.yungasdistribuidora.data.local.bonus.BonusRuleEntity
import com.example.yungasdistribuidora.data.remote.dto.BonusRuleDto

object BonusRuleMapper {
    fun toEntity(dto: BonusRuleDto): BonusRuleEntity {
        return BonusRuleEntity(
            id = dto.id,
            subCategoryId = dto.subCategoryId,
            minQuantity = dto.minQuantity,
            bonusQuantity = dto.bonusQuantity,
            startDate = dto.startDate,
            endDate = dto.endDate,
            isActive = dto.isActive
        )
    }

    fun toDto(entity: BonusRuleEntity): BonusRuleDto {
        return BonusRuleDto(
            id = entity.id,
            subCategoryId = entity.subCategoryId,
            minQuantity = entity.minQuantity,
            bonusQuantity = entity.bonusQuantity,
            startDate = entity.startDate,
            endDate = entity.endDate,
            isActive = entity.isActive
        )
    }
}

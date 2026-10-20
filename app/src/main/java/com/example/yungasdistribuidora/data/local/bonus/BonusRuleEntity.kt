package com.example.yungasdistribuidora.data.local.bonus

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bonus_rules")
data class BonusRuleEntity(
    @PrimaryKey val id: String,
    val subCategoryId: String,
    val minQuantity: Int,
    val bonusQuantity: Int,
    val startDate: String?,
    val endDate: String?,
    val isActive: Boolean
)

package com.example.yungasdistribuidora.domain.usecase

import com.example.yungasdistribuidora.data.local.bonus.BonusRuleEntity
import com.example.yungasdistribuidora.domain.model.PresaleItem

class CalculateBonusUseCase {
    fun calculate(
        rules: List<BonusRuleEntity>,
        items: List<PresaleItem>
    ): Map<String, Int> {
        val subCategoryQuantities = mutableMapOf<String, Int>()
        for (item in items) {
            val subCatId = item.product.subCategoryId
            if (!subCatId.isNullOrBlank()) {
                subCategoryQuantities[subCatId] = subCategoryQuantities.getOrDefault(subCatId, 0) + item.quantity
            }
        }

        val earnedBonuses = mutableMapOf<String, Int>()
        for (rule in rules) {
            if (!rule.isActive) continue
            val soldQty = subCategoryQuantities[rule.subCategoryId] ?: 0
            if (soldQty >= rule.minQuantity && rule.minQuantity > 0) {
                val times = soldQty / rule.minQuantity
                val earned = times * rule.bonusQuantity
                earnedBonuses[rule.subCategoryId] = earnedBonuses.getOrDefault(rule.subCategoryId, 0) + earned
            }
        }
        return earnedBonuses
    }
}

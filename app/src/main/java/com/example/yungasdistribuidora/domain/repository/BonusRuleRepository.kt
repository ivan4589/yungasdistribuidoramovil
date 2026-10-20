package com.example.yungasdistribuidora.domain.repository

import com.example.yungasdistribuidora.data.local.bonus.BonusRuleEntity

interface BonusRuleRepository {
    suspend fun getBonusRules(): Result<List<BonusRuleEntity>>
    suspend fun refreshBonusRules(): Result<Unit>
}

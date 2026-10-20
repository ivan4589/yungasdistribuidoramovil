package com.example.yungasdistribuidora.data.repository

import com.example.yungasdistribuidora.data.local.bonus.BonusRuleDao
import com.example.yungasdistribuidora.data.local.bonus.BonusRuleEntity
import com.example.yungasdistribuidora.data.mapper.BonusRuleMapper
import com.example.yungasdistribuidora.data.remote.api.BonusRulesApi
import com.example.yungasdistribuidora.domain.repository.BonusRuleRepository
import com.example.yungasdistribuidora.util.ErrorMapper

class BonusRuleRepositoryImpl(
    private val bonusRulesApi: BonusRulesApi,
    private val bonusRuleDao: BonusRuleDao
) : BonusRuleRepository {

    override suspend fun getBonusRules(): Result<List<BonusRuleEntity>> {
        return try {
            val local = bonusRuleDao.getActiveBonusRules()
            if (local.isNotEmpty()) {
                Result.success(local)
            } else {
                refreshBonusRules()
                Result.success(bonusRuleDao.getActiveBonusRules())
            }
        } catch (e: Exception) {
            Result.success(emptyList())
        }
    }

    override suspend fun refreshBonusRules(): Result<Unit> {
        return try {
            val response = bonusRulesApi.getBonusRules()
            if (response.isSuccessful && response.body() != null) {
                val entities = response.body()!!.map { BonusRuleMapper.toEntity(it) }
                bonusRuleDao.clearBonusRules()
                bonusRuleDao.insertBonusRules(entities)
                Result.success(Unit)
            } else {
                Result.failure(Exception(ErrorMapper.mapHttpCode(response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }
}

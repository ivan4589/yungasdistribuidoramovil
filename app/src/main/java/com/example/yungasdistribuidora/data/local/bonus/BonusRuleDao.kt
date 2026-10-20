package com.example.yungasdistribuidora.data.local.bonus

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BonusRuleDao {
    @Query("SELECT * FROM bonus_rules WHERE isActive = 1")
    suspend fun getActiveBonusRules(): List<BonusRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBonusRules(rules: List<BonusRuleEntity>)

    @Query("DELETE FROM bonus_rules")
    suspend fun clearBonusRules()
}

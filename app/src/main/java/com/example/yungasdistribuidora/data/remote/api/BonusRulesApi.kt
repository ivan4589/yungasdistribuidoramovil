package com.example.yungasdistribuidora.data.remote.api

import com.example.yungasdistribuidora.data.remote.dto.BonusRuleDto
import retrofit2.Response
import retrofit2.http.GET

interface BonusRulesApi {
    @GET("bonus-rules")
    suspend fun getBonusRules(): Response<List<BonusRuleDto>>
}

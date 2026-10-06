package com.example.yungasdistribuidora.data.remote.api

import com.example.yungasdistribuidora.data.remote.dto.subcategory.SubCategoryDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SubCategoryApi {
    @GET("sub-categories")
    suspend fun getSubCategories(
        @Query("categoryId") categoryId: String?
    ): Response<List<SubCategoryDto>>

    @GET("sub-categories/{id}")
    suspend fun getSubCategoryById(@Path("id") id: String): Response<SubCategoryDto>
}

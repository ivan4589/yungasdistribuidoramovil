package com.example.yungasdistribuidora.domain.repository

import com.example.yungasdistribuidora.domain.model.Category
import com.example.yungasdistribuidora.domain.model.Product
import com.example.yungasdistribuidora.domain.model.SubCategory
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProducts(categoryId: String?, subCategoryId: String?): Flow<List<Product>>
    suspend fun refreshCatalog(): Result<Unit>
    suspend fun getProductById(id: String): Result<Product?>
    fun getCategories(): Flow<List<Category>>
    fun getSubCategories(categoryId: String?): Flow<List<SubCategory>>
    fun getLastSyncTime(): Flow<Long?>
}

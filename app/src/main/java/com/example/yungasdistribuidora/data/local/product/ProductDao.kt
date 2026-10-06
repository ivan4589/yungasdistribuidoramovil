package com.example.yungasdistribuidora.data.local.product

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("""
        SELECT * FROM products 
        WHERE (:categoryId IS NULL OR categoryId = :categoryId)
        AND (:subCategoryId IS NULL OR subCategoryId = :subCategoryId)
        AND isActive = 1
        ORDER BY name ASC
    """)
    fun getProducts(categoryId: String?, subCategoryId: String?): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()
}

package com.example.yungasdistribuidora.data.local.subcategory

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SubCategoryDao {
    @Query("""
        SELECT * FROM subcategories 
        WHERE (:categoryId IS NULL OR categoryId = :categoryId)
        ORDER BY name ASC
    """)
    fun getSubCategories(categoryId: String?): Flow<List<SubCategoryEntity>>

    @Query("SELECT * FROM subcategories WHERE id = :id")
    suspend fun getSubCategoryById(id: String): SubCategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubCategories(subCategories: List<SubCategoryEntity>)

    @Query("DELETE FROM subcategories")
    suspend fun deleteAllSubCategories()
}

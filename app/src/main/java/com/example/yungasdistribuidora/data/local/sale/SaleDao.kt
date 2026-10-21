package com.example.yungasdistribuidora.data.local.sale

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY createdAt DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleById(id: String): SaleEntity?

    @Query("SELECT * FROM sale_details WHERE saleId = :saleId")
    suspend fun getSaleDetails(saleId: String): List<SaleDetailEntity>

    @Query("SELECT * FROM sale_bonuses WHERE saleId = :saleId")
    suspend fun getSaleBonuses(saleId: String): List<SaleBonusEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetails(details: List<SaleDetailEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBonuses(bonuses: List<SaleBonusEntity>)

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteSale(id: String)

    @Query("DELETE FROM sales")
    suspend fun clearSales()
}

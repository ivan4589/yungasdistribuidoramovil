package com.example.yungasdistribuidora.data.local.sale

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Transaction
    suspend fun saveCompleteSale(sale: SaleEntity, details: List<SaleDetailEntity>?, bonuses: List<SaleBonusEntity>?) {
        require(details == null || (details.all { it.saleId == sale.id } && details.map { it.id }.distinct().size == details.size))
        require(bonuses == null || (bonuses.all { it.saleId == sale.id } && bonuses.map { it.id }.distinct().size == bonuses.size))
        insertSale(sale)
        // Null means omitted by a partial response; only an explicit list replaces rows.
        details?.let {
            deleteDetails(sale.id)
            insertDetails(it)
        }
        bonuses?.let {
            deleteBonuses(sale.id)
            insertBonuses(it)
        }
    }

    /**
     * Persist a downloaded set atomically. Never delete missing local sales here:
     * the server response may be paginated or scoped by user permissions.
     */
    @Transaction
    suspend fun saveRemoteBatch(
        sales: List<SaleEntity>,
        details: List<List<SaleDetailEntity>?>,
        bonuses: List<List<SaleBonusEntity>?>
    ) {
        require(sales.size == details.size && sales.size == bonuses.size)
        sales.forEachIndexed { index, sale ->
            saveCompleteSale(sale, details[index], bonuses[index])
        }
    }

    @Query("DELETE FROM sale_details WHERE saleId = :id")
    suspend fun deleteDetails(id: String)

    @Query("DELETE FROM sale_bonuses WHERE saleId = :id")
    suspend fun deleteBonuses(id: String)

    @Query("SELECT * FROM sales ORDER BY createdAt DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales")
    suspend fun getAllSalesList(): List<SaleEntity>

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleById(id: String): SaleEntity?

    @Query("SELECT * FROM sale_details WHERE saleId = :saleId")
    suspend fun getSaleDetails(saleId: String): List<SaleDetailEntity>

    @Query("SELECT * FROM sale_bonuses WHERE saleId = :saleId")
    suspend fun getSaleBonuses(saleId: String): List<SaleBonusEntity>

    // REPLACE deletes the parent and cascades children, including on header-only updates.
    @Upsert
    suspend fun insertSale(sale: SaleEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDetails(details: List<SaleDetailEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBonuses(bonuses: List<SaleBonusEntity>)

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteSale(id: String)

    @Query("DELETE FROM sales")
    suspend fun clearSales()
}

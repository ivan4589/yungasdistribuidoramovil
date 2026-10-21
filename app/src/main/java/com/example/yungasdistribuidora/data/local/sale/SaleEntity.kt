package com.example.yungasdistribuidora.data.local.sale

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey val id: String,
    val saleNumber: String,
    val clientId: String,
    val clientName: String?,
    val status: String,
    val paymentStatus: String,
    val saleType: String,
    val dueDate: String?,
    val subtotal: Double,
    val discount: Double,
    val total: Double,
    val paidAmount: Double,
    val balance: Double,
    val observations: String?,
    val pdfUrl: String?,
    val sellerId: String?,
    val sellerName: String?,
    val createdAt: String,
    val updatedAt: String
)

@Entity(
    tableName = "sale_details",
    foreignKeys = [
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("saleId")]
)
data class SaleDetailEntity(
    @PrimaryKey val id: String,
    val saleId: String,
    val productId: String,
    val productName: String?,
    val quantity: Int,
    val unitPrice: Double,
    val manualPrice: Boolean,
    val subtotal: Double
)

@Entity(
    tableName = "sale_bonuses",
    foreignKeys = [
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("saleId")]
)
data class SaleBonusEntity(
    @PrimaryKey val id: String,
    val saleId: String,
    val productId: String,
    val productName: String?,
    val quantity: Int
)

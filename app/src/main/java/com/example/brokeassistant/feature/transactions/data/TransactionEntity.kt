package com.example.brokeassistant.feature.transactions.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val categoryId: Int,
    val dateMillis: Long,
    val type: String // "INCOME" or "EXPENSE"
)

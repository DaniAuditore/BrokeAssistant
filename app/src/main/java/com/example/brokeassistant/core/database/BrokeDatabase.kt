package com.example.brokeassistant.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.brokeassistant.feature.budget.data.CategoryDao
import com.example.brokeassistant.feature.budget.data.CategoryEntity
import com.example.brokeassistant.feature.transactions.data.TransactionDao
import com.example.brokeassistant.feature.transactions.data.TransactionEntity

@Database(entities = [CategoryEntity::class, TransactionEntity::class], version = 1, exportSchema = false)
abstract class BrokeDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
}

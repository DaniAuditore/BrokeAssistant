package com.example.brokeassistant.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.brokeassistant.feature.budget.data.CategoryDao
import com.example.brokeassistant.feature.budget.data.CategoryEntity
import com.example.brokeassistant.feature.transactions.data.TransactionDao
import com.example.brokeassistant.feature.transactions.data.TransactionEntity

@Database(entities = [CategoryEntity::class, TransactionEntity::class], version = 1, exportSchema = false)
abstract class BrokeDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: BrokeDatabase? = null

        fun getDatabase(context: Context): BrokeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BrokeDatabase::class.java,
                    "broke_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

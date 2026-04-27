package com.example.brokeassistant.feature.budget.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoryAndReturnId(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("SELECT SUM(percentage) FROM categories")
    suspend fun getTotalPercentage(): Int?

    @Query("UPDATE transactions SET categoryId = :fallbackId WHERE categoryId = :deletedId")
    suspend fun reassignTransactions(deletedId: Int, fallbackId: Int)

    @androidx.room.Transaction
    suspend fun deleteCategoryWithFallback(deletedId: Int, fallbackId: Int) {
        val deletedCategory = getCategoryById(deletedId) 
            ?: throw IllegalArgumentException("Category to delete (id=$deletedId) not found")
        val fallbackCategory = getCategoryById(fallbackId)
            ?: throw IllegalArgumentException("Fallback category (id=$fallbackId) not found")

        println("Starting category migration: Deleting $deletedId, moving to $fallbackId")

        // Reassign transactions
        reassignTransactions(deletedId, fallbackId)

        // Delete the category
        deleteCategory(deletedCategory)
    }
}
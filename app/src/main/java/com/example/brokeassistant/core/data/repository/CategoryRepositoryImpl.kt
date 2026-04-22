package com.example.brokeassistant.core.data.repository

import com.example.brokeassistant.core.domain.model.Category
import com.example.brokeassistant.core.domain.repository.CategoryRepository
import com.example.brokeassistant.feature.budget.data.CategoryDao
import com.example.brokeassistant.feature.budget.data.CategoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {
    override fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { list ->
            list.map { entity ->
                Category(
                    id = entity.id.toLong(),
                    name = entity.name,
                    percentage = entity.percentage,
                    isDefaultFree = entity.isSystemGenerated
                )
            }
        }
    }

    override suspend fun getCategoryById(id: Long): Category? {
        return categoryDao.getCategoryById(id.toInt())?.let { entity ->
            Category(
                id = entity.id.toLong(),
                name = entity.name,
                percentage = entity.percentage,
                isDefaultFree = entity.isSystemGenerated
            )
        }
    }

    override suspend fun insertCategory(category: Category): Long {
        val entity = CategoryEntity(
            id = category.id.toInt(),
            name = category.name,
            percentage = category.percentage,
            isSystemGenerated = category.isDefaultFree
        )
        return categoryDao.insertCategoryAndReturnId(entity)
    }

    override suspend fun updateCategory(category: Category) {
        val entity = CategoryEntity(
            id = category.id.toInt(),
            name = category.name,
            percentage = category.percentage,
            isSystemGenerated = category.isDefaultFree
        )
        categoryDao.updateCategory(entity)
    }

    override suspend fun deleteCategory(category: Category) {
        val entity = CategoryEntity(
            id = category.id.toInt(),
            name = category.name,
            percentage = category.percentage,
            isSystemGenerated = category.isDefaultFree
        )
        categoryDao.deleteCategory(entity)
    }

    override suspend fun deleteCategoryWithFallback(deletedId: Long, fallbackId: Long) {
        categoryDao.deleteCategoryWithFallback(deletedId.toInt(), fallbackId.toInt())
    }
}

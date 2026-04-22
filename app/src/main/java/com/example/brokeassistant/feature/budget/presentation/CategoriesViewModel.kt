package com.example.brokeassistant.feature.budget.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brokeassistant.core.domain.model.Category
import com.example.brokeassistant.core.domain.repository.CategoryRepository
import com.example.brokeassistant.core.domain.usecase.ValidateCategoryPercentagesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoriesState(
    val categories: List<Category> = emptyList(),
    val totalPercentage: Int = 0,
    val isSaveEnabled: Boolean = false,
    val newCategoryName: String = "",
    val newCategoryPercentage: String = ""
)

sealed class CategoriesIntent {
    data class UpdateNewCategoryName(val name: String) : CategoriesIntent()
    data class UpdateNewCategoryPercentage(val percentage: String) : CategoriesIntent()
    data class UpdatePercentage(val categoryId: Long, val percentage: Int) : CategoriesIntent()
    data class DeleteCategoryWithFallback(val deletedId: Long, val fallbackId: Long) : CategoriesIntent()
    object AddCategory : CategoriesIntent()
    object SaveCategories : CategoriesIntent()
}

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val validateCategoryPercentagesUseCase: ValidateCategoryPercentagesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CategoriesState())
    val state: StateFlow<CategoriesState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { categories ->
                updateStateWithCategories(categories)
            }
        }
    }

    fun onIntent(intent: CategoriesIntent) {
        when (intent) {
            is CategoriesIntent.UpdateNewCategoryName -> {
                _state.update { it.copy(newCategoryName = intent.name) }
            }
            is CategoriesIntent.UpdateNewCategoryPercentage -> {
                _state.update { it.copy(newCategoryPercentage = intent.percentage) }
            }
            is CategoriesIntent.UpdatePercentage -> {
                val updatedCategories = _state.value.categories.map {
                    if (it.id == intent.categoryId) it.copy(percentage = intent.percentage) else it
                }
                updateStateWithCategories(updatedCategories)
            }
            CategoriesIntent.AddCategory -> {
                val name = _state.value.newCategoryName
                val pct = _state.value.newCategoryPercentage.toIntOrNull() ?: 0
                if (name.isNotBlank()) {
                    val newCat = Category(name = name, percentage = pct)
                    val updatedCategories = _state.value.categories + newCat
                    updateStateWithCategories(updatedCategories)
                    _state.update { it.copy(newCategoryName = "", newCategoryPercentage = "") }
                }
            }
            CategoriesIntent.SaveCategories -> {
                viewModelScope.launch {
                    val isValid = validateCategoryPercentagesUseCase(_state.value.categories)
                    if (isValid) {
                        _state.value.categories.forEach { category ->
                            if (category.id == 0L) {
                                categoryRepository.insertCategory(category)
                            } else {
                                categoryRepository.updateCategory(category)
                            }
                        }
                    }
                }
            }
            is CategoriesIntent.DeleteCategoryWithFallback -> {
                viewModelScope.launch {
                    categoryRepository.deleteCategoryWithFallback(
                        deletedId = intent.deletedId,
                        fallbackId = intent.fallbackId
                    )
                }
            }
        }
    }

    private fun updateStateWithCategories(categories: List<Category>) {
        val total = categories.sumOf { it.percentage }
        val isValid = validateCategoryPercentagesUseCase(categories)
        _state.update { 
            it.copy(
                categories = categories, 
                totalPercentage = total, 
                isSaveEnabled = isValid
            ) 
        }
    }
}

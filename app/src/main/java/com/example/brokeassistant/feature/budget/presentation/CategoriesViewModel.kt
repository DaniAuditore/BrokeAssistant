package com.example.brokeassistant.feature.budget.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brokeassistant.core.domain.model.Category
import com.example.brokeassistant.core.domain.repository.CategoryRepository
import com.example.brokeassistant.core.domain.usecase.ValidateCategoryPercentagesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

    private val _userInputs = MutableStateFlow(
        UserInputs(
            newCategoryName = "",
            newCategoryPercentage = ""
        )
    )

    private data class UserInputs(
        val newCategoryName: String,
        val newCategoryPercentage: String
    )

    val state: StateFlow<CategoriesState> = combine(
        categoryRepository.getAllCategories(),
        _userInputs
    ) { categories, inputs ->
        val total = categories.sumOf { it.percentage }
        val isValid = validateCategoryPercentagesUseCase(categories)
        CategoriesState(
            categories = categories,
            totalPercentage = total,
            isSaveEnabled = isValid,
            newCategoryName = inputs.newCategoryName,
            newCategoryPercentage = inputs.newCategoryPercentage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CategoriesState()
    )

    fun onIntent(intent: CategoriesIntent) {
        when (intent) {
            is CategoriesIntent.UpdateNewCategoryName -> {
                _userInputs.update { it.copy(newCategoryName = intent.name) }
            }
            is CategoriesIntent.UpdateNewCategoryPercentage -> {
                _userInputs.update { it.copy(newCategoryPercentage = intent.percentage) }
            }
            is CategoriesIntent.UpdatePercentage -> {
                viewModelScope.launch {
                    val category = state.value.categories.find { it.id == intent.categoryId }
                    category?.let {
                        categoryRepository.updateCategory(it.copy(percentage = intent.percentage))
                    }
                }
            }
            CategoriesIntent.AddCategory -> {
                val name = _userInputs.value.newCategoryName
                val pct = _userInputs.value.newCategoryPercentage.toIntOrNull() ?: 0
                if (name.isNotBlank()) {
                    viewModelScope.launch {
                        categoryRepository.insertCategory(Category(name = name, percentage = pct))
                        _userInputs.update { it.copy(newCategoryName = "", newCategoryPercentage = "") }
                    }
                }
            }
            CategoriesIntent.SaveCategories -> {
                viewModelScope.launch {
                    val isValid = validateCategoryPercentagesUseCase(state.value.categories)
                    if (isValid) {
                        state.value.categories.forEach { category ->
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
}

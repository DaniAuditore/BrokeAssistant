package com.example.brokeassistant.feature.transactions.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brokeassistant.core.domain.model.Category
import com.example.brokeassistant.core.domain.model.Transaction
import com.example.brokeassistant.core.domain.model.TransactionType
import com.example.brokeassistant.core.domain.repository.CategoryRepository
import com.example.brokeassistant.core.domain.repository.TransactionRepository
import com.example.brokeassistant.core.domain.usecase.DistributeIncomeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class TransactionsState(
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val amount: String = "",
    val description: String = "",
    val categories: List<Category> = emptyList(),
    val selectedCategory: Category? = null
)

sealed class TransactionsIntent {
    data class UpdateType(val type: TransactionType) : TransactionsIntent()
    data class UpdateAmount(val amount: String) : TransactionsIntent()
    data class UpdateDescription(val description: String) : TransactionsIntent()
    data class SelectCategory(val category: Category) : TransactionsIntent()
    object AddTransaction : TransactionsIntent()
}

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val distributeIncomeUseCase: DistributeIncomeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionsState())
    val state: StateFlow<TransactionsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { categories ->
                _state.update { 
                    it.copy(
                        categories = categories,
                        selectedCategory = if (it.selectedCategory == null && categories.isNotEmpty()) categories.first() else it.selectedCategory
                    ) 
                }
            }
        }
    }

    fun onIntent(intent: TransactionsIntent) {
        when (intent) {
            is TransactionsIntent.UpdateType -> _state.update { it.copy(transactionType = intent.type) }
            is TransactionsIntent.UpdateAmount -> _state.update { it.copy(amount = intent.amount) }
            is TransactionsIntent.UpdateDescription -> _state.update { it.copy(description = intent.description) }
            is TransactionsIntent.SelectCategory -> _state.update { it.copy(selectedCategory = intent.category) }
            TransactionsIntent.AddTransaction -> {
                viewModelScope.launch {
                    val amountInCents = (_state.value.amount.toDoubleOrNull()?.times(100))?.toLong() ?: 0L
                    if (amountInCents > 0) {
                        if (_state.value.transactionType == TransactionType.INCOME) {
                            val transactions = distributeIncomeUseCase(
                                amountInCents = amountInCents,
                                description = _state.value.description,
                                categories = _state.value.categories,
                                date = LocalDateTime.now()
                            )
                            transactions.forEach { tx ->
                                transactionRepository.insertTransaction(tx)
                            }
                        } else {
                            val tx = Transaction(
                                categoryId = _state.value.selectedCategory?.id,
                                type = TransactionType.EXPENSE,
                                amountInCents = amountInCents,
                                description = _state.value.description,
                                date = LocalDateTime.now()
                            )
                            transactionRepository.insertTransaction(tx)
                        }
                        // Reset form
                        _state.update { it.copy(amount = "", description = "") }
                    }
                }
            }
        }
    }
}

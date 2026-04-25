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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
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
    private val distributeIncomeUseCase: DistributeIncomeUseCase,
    private val clock: Clock
) : ViewModel() {

    private val _userInputs = MutableStateFlow(
        UserInputs(
            transactionType = TransactionType.EXPENSE,
            amount = "",
            description = "",
            selectedCategory = null
        )
    )

    private data class UserInputs(
        val transactionType: TransactionType,
        val amount: String,
        val description: String,
        val selectedCategory: Category?
    )

    val state: StateFlow<TransactionsState> = combine(
        categoryRepository.getAllCategories(),
        _userInputs
    ) { categories, inputs ->
        TransactionsState(
            transactionType = inputs.transactionType,
            amount = inputs.amount,
            description = inputs.description,
            categories = categories,
            selectedCategory = inputs.selectedCategory ?: categories.firstOrNull()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TransactionsState()
    )

    fun onIntent(intent: TransactionsIntent) {
        when (intent) {
            is TransactionsIntent.UpdateType -> _userInputs.update { it.copy(transactionType = intent.type) }
            is TransactionsIntent.UpdateAmount -> _userInputs.update { it.copy(amount = intent.amount) }
            is TransactionsIntent.UpdateDescription -> _userInputs.update { it.copy(description = intent.description) }
            is TransactionsIntent.SelectCategory -> _userInputs.update { it.copy(selectedCategory = intent.category) }
            TransactionsIntent.AddTransaction -> {
                viewModelScope.launch {
                    val currentState = state.value
                    val amountInCents = (currentState.amount.toDoubleOrNull()?.times(100))?.toLong() ?: 0L
                    if (amountInCents > 0) {
                        if (currentState.transactionType == TransactionType.INCOME) {
                            val transactions = distributeIncomeUseCase(
                                amountInCents = amountInCents,
                                description = currentState.description,
                                categories = currentState.categories,
                                date = LocalDateTime.now(clock)
                            )
                            transactions.forEach { tx ->
                                transactionRepository.insertTransaction(tx)
                            }
                        } else {
                            val tx = Transaction(
                                categoryId = currentState.selectedCategory?.id,
                                type = TransactionType.EXPENSE,
                                amountInCents = amountInCents,
                                description = currentState.description,
                                date = LocalDateTime.now(clock)
                            )
                            transactionRepository.insertTransaction(tx)
                        }
                        // Reset form
                        _userInputs.update { it.copy(amount = "", description = "") }
                    }
                }
            }
        }
    }
}

package com.example.brokeassistant.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brokeassistant.core.domain.model.Category
import com.example.brokeassistant.core.domain.model.Transaction
import com.example.brokeassistant.core.domain.model.TransactionType
import com.example.brokeassistant.core.domain.repository.CategoryRepository
import com.example.brokeassistant.core.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryBalance(
    val category: Category,
    val balanceInCents: Long
)

data class DashboardState(
    val categoryBalances: List<CategoryBalance> = emptyList(),
    val totalAvailableInCents: Long = 0L,
    val allCategories: List<Category> = emptyList(),
    val allTransactions: List<Transaction> = emptyList()
)

sealed class DashboardIntent {
    object Refresh : DashboardIntent()
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                categoryRepository.getAllCategories(),
                transactionRepository.getAllTransactions()
            ) { categories, transactions ->
                val balances = categories.map { category ->
                    val categoryTxs = transactions.filter { it.categoryId == category.id }
                    val income = categoryTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amountInCents }
                    val expense = categoryTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountInCents }
                    CategoryBalance(category, income - expense)
                }
                
                val totalAvailable = balances.sumOf { it.balanceInCents }

                DashboardState(
                    categoryBalances = balances,
                    totalAvailableInCents = totalAvailable,
                    allCategories = categories,
                    allTransactions = transactions
                )
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    fun onIntent(intent: DashboardIntent) {
    }
}

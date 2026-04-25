package com.example.brokeassistant.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brokeassistant.core.domain.model.Category
import com.example.brokeassistant.core.domain.model.Transaction
import com.example.brokeassistant.core.domain.model.TransactionType
import com.example.brokeassistant.core.domain.repository.CategoryRepository
import com.example.brokeassistant.core.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

    val state: StateFlow<DashboardState> = combine(
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
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardState()
    )

    fun onIntent(intent: DashboardIntent) {
    }
}

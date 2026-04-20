package com.example.brokeassistant.core.domain.usecase

import com.example.brokeassistant.core.domain.model.Category
import com.example.brokeassistant.core.domain.model.Transaction
import com.example.brokeassistant.core.domain.model.TransactionType
import java.time.LocalDateTime
import javax.inject.Inject

class DistributeIncomeUseCase @Inject constructor() {
    operator fun invoke(
        amountInCents: Long,
        description: String,
        categories: List<Category>,
        date: LocalDateTime = LocalDateTime.now()
    ): List<Transaction> {
        require(amountInCents > 0) { "Income amount must be greater than zero" }
        require(categories.isNotEmpty()) { "Categories list cannot be empty" }
        require(categories.sumOf { it.percentage } == 100) { "Category percentages must sum up exactly to 100" }

        val transactions = mutableListOf<Transaction>()
        var remainingCents = amountInCents

        // Calculate exact distribution per category based on percentage
        val distributions = categories.map { category ->
            val distributedAmount = (amountInCents * category.percentage) / 100
            remainingCents -= distributedAmount
            category to distributedAmount
        }.toMutableList()

        // Distribute remainder due to rounding
        if (remainingCents > 0) {
            val targetIndex = distributions.indexOfFirst { it.first.isDefaultFree }.takeIf { it != -1 }
                ?: distributions.indexOfFirst { it.first == categories.maxByOrNull { cat -> cat.percentage } }
            
            if (targetIndex != -1) {
                val (category, currentAmount) = distributions[targetIndex]
                distributions[targetIndex] = category to (currentAmount + remainingCents)
            }
        }

        // Create transaction models for the distributed amounts
        distributions.forEach { (category, amount) ->
            if (amount > 0) {
                transactions.add(
                    Transaction(
                        categoryId = category.id,
                        type = TransactionType.INCOME,
                        amountInCents = amount,
                        description = "$description - ${category.name}",
                        date = date
                    )
                )
            }
        }

        return transactions
    }
}

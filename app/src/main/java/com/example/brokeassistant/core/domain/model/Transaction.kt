package com.example.brokeassistant.core.domain.model

import java.time.LocalDateTime

enum class TransactionType { INCOME, EXPENSE }

data class Transaction(
    val id: Long = 0,
    val categoryId: Long? = null,
    val type: TransactionType,
    val amountInCents: Long,
    val description: String,
    val date: LocalDateTime
)

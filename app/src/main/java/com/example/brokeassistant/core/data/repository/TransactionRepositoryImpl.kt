package com.example.brokeassistant.core.data.repository

import com.example.brokeassistant.core.domain.model.Transaction
import com.example.brokeassistant.core.domain.model.TransactionType
import com.example.brokeassistant.core.domain.repository.TransactionRepository
import com.example.brokeassistant.feature.transactions.data.TransactionDao
import com.example.brokeassistant.feature.transactions.data.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {
    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getTransactionsByCategory(categoryId: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByCategory(categoryId.toInt()).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getTransactionById(id: Long): Transaction? {
        return transactionDao.getTransactionById(id.toInt())?.toDomain()
    }

    override suspend fun insertTransaction(transaction: Transaction): Long {
        return transactionDao.insertTransaction(transaction.toEntity())
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction.toEntity())
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction.toEntity())
    }

    private fun TransactionEntity.toDomain(): Transaction {
        return Transaction(
            id = id.toLong(),
            categoryId = categoryId?.toLong(),
            type = TransactionType.valueOf(type),
            amountInCents = amountInCents,
            description = description,
            date = Instant.ofEpochMilli(dateMillis).atZone(ZoneId.systemDefault()).toLocalDateTime()
        )
    }

    private fun Transaction.toEntity(): TransactionEntity {
        return TransactionEntity(
            id = id.toInt(),
            amountInCents = amountInCents,
            description = description,
            categoryId = categoryId?.toInt(),
            dateMillis = date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            type = type.name
        )
    }
}
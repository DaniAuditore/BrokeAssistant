package com.example.brokeassistant.core.di

import com.example.brokeassistant.core.data.repository.CategoryRepositoryImpl
import com.example.brokeassistant.core.data.repository.TransactionRepositoryImpl
import com.example.brokeassistant.core.domain.repository.CategoryRepository
import com.example.brokeassistant.core.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        categoryRepositoryImpl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        transactionRepositoryImpl: TransactionRepositoryImpl
    ): TransactionRepository
}
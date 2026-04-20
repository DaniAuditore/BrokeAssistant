package com.example.brokeassistant.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.example.brokeassistant.core.database.BrokeDatabase
import com.example.brokeassistant.core.datastore.PreferencesDataStore
import com.example.brokeassistant.feature.budget.data.CategoryDao
import com.example.brokeassistant.feature.transactions.data.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideBrokeDatabase(@ApplicationContext context: Context): BrokeDatabase {
        return Room.databaseBuilder(
            context,
            BrokeDatabase::class.java,
            "broke_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideCategoryDao(database: BrokeDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    fun provideTransactionDao(database: BrokeDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext context: Context): PreferencesDataStore {
        return PreferencesDataStore(context)
    }
}
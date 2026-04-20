package com.example.brokeassistant.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "broke_settings")

class PreferencesDataStore(private val context: Context) {
    companion object {
        val CURRENCY = stringPreferencesKey("currency")
        val THEME = stringPreferencesKey("theme")
        val BIO_AUTH = booleanPreferencesKey("bio_auth")
    }

    val currencyFlow: Flow<String> = context.dataStore.data.map { it[CURRENCY] ?: "USD" }
    val themeFlow: Flow<String> = context.dataStore.data.map { it[THEME] ?: "SYSTEM" }
    val bioAuthFlow: Flow<Boolean> = context.dataStore.data.map { it[BIO_AUTH] ?: false }

    suspend fun saveCurrency(currency: String) {
        context.dataStore.edit { it[CURRENCY] = currency }
    }

    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { it[THEME] = theme }
    }

    suspend fun saveBioAuth(enabled: Boolean) {
        context.dataStore.edit { it[BIO_AUTH] = enabled }
    }
}

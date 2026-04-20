package com.example.brokeassistant.core.util

import com.example.brokeassistant.core.datastore.PreferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

class CurrencyFormatter(
    private val preferencesDataStore: PreferencesDataStore
) {
    /**
     * Emits a function that formats a Long amount to its String representation
     * according to the currently selected currency.
     */
    val formatterFlow: Flow<(Long) -> String> = preferencesDataStore.currencyFlow.map { currencyCode ->
        val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
        try {
            format.currency = Currency.getInstance(currencyCode)
        } catch (e: Exception) {
            format.currency = Currency.getInstance("USD")
        }

        // Adjust fraction digits based on currency.
        // CLP uses 0. Most others default to 2.
        if (currencyCode == "CLP" || format.currency?.defaultFractionDigits == 0) {
            format.minimumFractionDigits = 0
            format.maximumFractionDigits = 0
        }

        return@map { amount: Long ->
            if (currencyCode == "CLP" || format.currency?.defaultFractionDigits == 0) {
                // For zero-decimal currencies, the amount is the exact value, no cents division.
                format.format(amount)
            } else {
                // For decimal currencies, we store the amount in cents (e.g. 1000 = $10.00).
                format.format(amount / 100.0)
            }
        }
    }

    /**
     * Static utility to parse user input based on current currency code
     */
    companion object {
        fun parseInputToLong(input: String, currencyCode: String): Long {
            val cleanStr = input.replace(Regex("[^0-9]"), "")
            if (cleanStr.isEmpty()) return 0L

            val parsed = cleanStr.toLongOrNull() ?: 0L
            return if (currencyCode == "CLP" || Currency.getInstance(currencyCode).defaultFractionDigits == 0) {
                // No decimals, raw value is the long
                parsed
            } else {
                // Values are in cents (12.34 -> 1234), if input is typed as whole numbers it might need * 100 depending on UX,
                // but usually user types 1234 for 12.34. We'll just return parsed.
                parsed
            }
        }
    }
}

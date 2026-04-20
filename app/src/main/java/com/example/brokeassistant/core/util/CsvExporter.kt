package com.example.brokeassistant.core.util

import android.content.Context
import android.net.Uri
import com.example.brokeassistant.core.domain.model.Category
import com.example.brokeassistant.core.domain.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

object CsvExporter {

    suspend fun exportData(
        context: Context,
        uri: Uri,
        categories: List<Category>,
        transactions: List<Transaction>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                // Write with UTF-8 to support accents and special chars
                // Use semicolon (;) as separator so Google Sheets mobile app parses columns correctly
                OutputStreamWriter(outputStream, StandardCharsets.UTF_8).use { writer ->
                    // 1. Export Categories
                    writer.write("--- CATEGORIES ---\n")
                    writer.write("ID;Name;Percentage;IsDefaultFree\n")
                    for (category in categories) {
                        writer.write("${category.id};${category.name};${category.percentage};${category.isDefaultFree}\n")
                    }
                    
                    writer.write("\n")
                    
                    // 2. Export Transactions
                    writer.write("--- TRANSACTIONS ---\n")
                    writer.write("ID;AmountInCents;Description;Date;CategoryId;Type\n")
                    for (transaction in transactions) {
                        writer.write("${transaction.id};${transaction.amountInCents};${transaction.description};${transaction.date};${transaction.categoryId};${transaction.type}\n")
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}

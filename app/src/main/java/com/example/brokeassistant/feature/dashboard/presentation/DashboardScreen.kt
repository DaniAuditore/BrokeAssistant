package com.example.brokeassistant.feature.dashboard.presentation

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.brokeassistant.core.domain.model.Category
import com.example.brokeassistant.core.domain.model.Transaction
import com.example.brokeassistant.core.util.CsvExporter
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    onNavigateToCategories: () -> Unit,
    onNavigateToTransactions: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Mock data for export test
    val categories = listOf(Category(1L, "Food", 50, false))
    val transactions = listOf(Transaction(1L, 1L, com.example.brokeassistant.core.domain.model.TransactionType.EXPENSE, 10000L, "Lunch", java.time.LocalDateTime.now()))

    // SAF Exporter Launcher (Android 13+ compliant)
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val result = CsvExporter.exportData(context, uri, categories, transactions)
                if (result.isSuccess) {
                    Toast.makeText(context, "Exported Successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Export Failed!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Dashboard Overview")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateToCategories) {
            Text("Manage Categories")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onNavigateToTransactions) {
            Text("View Transactions")
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { exportLauncher.launch("brokeassistant_export.csv") }) {
            Text("Export Data to CSV")
        }
    }
}

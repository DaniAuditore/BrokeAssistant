package com.example.brokeassistant.feature.dashboard.presentation

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.brokeassistant.core.util.CsvExporter
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    onNavigateToCategories: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val result = CsvExporter.exportData(context, uri, state.allCategories, state.allTransactions)
                if (result.isSuccess) {
                    Toast.makeText(context, "Exported Successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Export Failed!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Dashboard Overview", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Total Available")
                Text(
                    text = currencyFormat.format(state.totalAvailableInCents / 100.0),
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onNavigateToCategories) {
                Text("Categories")
            }
            Button(onClick = onNavigateToTransactions) {
                Text("Transactions")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Balances by Category", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(state.categoryBalances) { balance ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(balance.category.name)
                    Text(currencyFormat.format(balance.balanceInCents / 100.0))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { exportLauncher.launch("brokeassistant_export.csv") }) {
            Text("Export Data to CSV")
        }
    }
}

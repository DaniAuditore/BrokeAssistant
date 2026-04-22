package com.example.brokeassistant.feature.transactions.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.brokeassistant.core.domain.model.TransactionType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    onNavigateBack: () -> Unit,
    viewModel: TransactionsViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState
) {
    val state by viewModel.state.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = state.transactionType == TransactionType.EXPENSE,
                    onClick = { viewModel.onIntent(TransactionsIntent.UpdateType(TransactionType.EXPENSE)) },
                    label = { Text("Expense") }
                )
                FilterChip(
                    selected = state.transactionType == TransactionType.INCOME,
                    onClick = { viewModel.onIntent(TransactionsIntent.UpdateType(TransactionType.INCOME)) },
                    label = { Text("Income") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val isAmountInvalid = state.amount.isNotBlank() && (state.amount.toDoubleOrNull() == null || state.amount.toDouble() <= 0)
            OutlinedTextField(
                value = state.amount,
                onValueChange = { viewModel.onIntent(TransactionsIntent.UpdateAmount(it)) },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = isAmountInvalid,
                supportingText = {
                    if (isAmountInvalid) {
                        Text("Please enter a valid amount greater than 0")
                    } else if (state.amount.isBlank()) {
                        Text("Amount is required")
                    }
                },
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            val isDescriptionInvalid = state.description.isBlank() && state.amount.isNotBlank()
            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.onIntent(TransactionsIntent.UpdateDescription(it)) },
                label = { Text("Description") },
                isError = isDescriptionInvalid,
                supportingText = {
                    if (isDescriptionInvalid) {
                        Text("Description cannot be empty")
                    }
                },
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (state.transactionType == TransactionType.EXPENSE) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = state.selectedCategory?.name ?: "Select Category",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth().defaultMinSize(minHeight = 48.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        state.categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    viewModel.onIntent(TransactionsIntent.SelectCategory(category))
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val isSaveEnabled = state.amount.isNotBlank() && !isAmountInvalid && state.description.isNotBlank() && (state.transactionType == TransactionType.INCOME || state.selectedCategory != null)

            Button(
                onClick = {
                    viewModel.onIntent(TransactionsIntent.AddTransaction)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Transaction saved successfully")
                    }
                    onNavigateBack()
                },
                enabled = isSaveEnabled,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Transaction")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Cancel")
            }
        }
    }
}

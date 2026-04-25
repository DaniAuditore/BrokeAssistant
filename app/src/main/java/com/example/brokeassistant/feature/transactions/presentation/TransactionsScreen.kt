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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.brokeassistant.R
import com.example.brokeassistant.core.domain.model.TransactionType
import kotlinx.coroutines.launch

import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    onNavigateBack: () -> Unit,
    viewModel: TransactionsViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_transaction), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.string_back))
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
            // Grupo 1: Categorización y Tipo (Ley de Proximidad)
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FilterChip(
                            selected = state.transactionType == TransactionType.EXPENSE,
                            onClick = { viewModel.onIntent(TransactionsIntent.UpdateType(TransactionType.EXPENSE)) },
                            label = { Text(stringResource(R.string.string_type_expense)) }
                        )
                        FilterChip(
                            selected = state.transactionType == TransactionType.INCOME,
                            onClick = { viewModel.onIntent(TransactionsIntent.UpdateType(TransactionType.INCOME)) },
                            label = { Text(stringResource(R.string.string_type_income)) }
                        )
                    }

                    if (state.transactionType == TransactionType.EXPENSE) {
                        Spacer(modifier = Modifier.height(8.dp))
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = state.selectedCategory?.name ?: stringResource(R.string.string_select_category),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.string_category)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                )
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
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Grupo 2: Detalles de la transacción
            val isAmountInvalid = state.amount.isNotBlank() && (state.amount.toDoubleOrNull() == null || state.amount.toDouble() <= 0)
            OutlinedTextField(
                value = state.amount,
                onValueChange = { viewModel.onIntent(TransactionsIntent.UpdateAmount(it)) },
                label = { Text(stringResource(R.string.string_amount)) },
                placeholder = { Text("0.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = isAmountInvalid,
                supportingText = {
                    if (isAmountInvalid) {
                        Text(stringResource(R.string.string_amount_invalid))
                    } else if (state.amount.isBlank()) {
                        Text(stringResource(R.string.string_amount_required))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(12.dp))

            val isDescriptionInvalid = state.description.isBlank() && state.amount.isNotBlank()
            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.onIntent(TransactionsIntent.UpdateDescription(it)) },
                label = { Text(stringResource(R.string.string_description)) },
                placeholder = { Text("What was this for?") },
                isError = isDescriptionInvalid,
                supportingText = {
                    if (isDescriptionInvalid) {
                        Text(stringResource(R.string.string_description_empty))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            val isSaveEnabled = state.amount.isNotBlank() && !isAmountInvalid && state.description.isNotBlank() && (state.transactionType == TransactionType.INCOME || state.selectedCategory != null)

            val savedMsg = stringResource(R.string.string_transaction_saved)
            Button(
                onClick = {
                    viewModel.onIntent(TransactionsIntent.AddTransaction)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(savedMsg)
                    }
                    onNavigateBack()
                },
                enabled = isSaveEnabled,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.string_add))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.string_save_transaction), style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.string_cancel))
            }
        }
    }
}


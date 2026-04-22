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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.brokeassistant.R
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
                title = { Text(stringResource(R.string.add_transaction)) },
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

            Spacer(modifier = Modifier.height(16.dp))

            val isAmountInvalid = state.amount.isNotBlank() && (state.amount.toDoubleOrNull() == null || state.amount.toDouble() <= 0)
            OutlinedTextField(
                value = state.amount,
                onValueChange = { viewModel.onIntent(TransactionsIntent.UpdateAmount(it)) },
                label = { Text(stringResource(R.string.string_amount)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = isAmountInvalid,
                supportingText = {
                    if (isAmountInvalid) {
                        Text(stringResource(R.string.string_amount_invalid))
                    } else if (state.amount.isBlank()) {
                        Text(stringResource(R.string.string_amount_required))
                    }
                },
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            val isDescriptionInvalid = state.description.isBlank() && state.amount.isNotBlank()
            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.onIntent(TransactionsIntent.UpdateDescription(it)) },
                label = { Text(stringResource(R.string.string_description)) },
                isError = isDescriptionInvalid,
                supportingText = {
                    if (isDescriptionInvalid) {
                        Text(stringResource(R.string.string_description_empty))
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
                        value = state.selectedCategory?.name ?: stringResource(R.string.string_select_category),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.string_category)) },
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
                        snackbarHostState.showSnackbar(stringResource(R.string.string_transaction_saved))
                    }
                    onNavigateBack()
                },
                enabled = isSaveEnabled,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.string_add))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.string_save_transaction))
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(stringResource(R.string.string_cancel))
            }
        }
    }
}

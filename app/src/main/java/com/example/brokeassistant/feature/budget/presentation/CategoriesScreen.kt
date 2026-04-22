package com.example.brokeassistant.feature.budget.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onNavigateBack: () -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState
) {
    val state by viewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories Management") },
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
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (state.totalPercentage == 100) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.errorContainer,
                    contentColor = if (state.totalPercentage == 100) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Percentage: ${state.totalPercentage}%", style = MaterialTheme.typography.titleMedium)
                    if (state.totalPercentage != 100) {
                        Text(
                            text = "Total must be exactly 100%",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.categories) { category ->
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(category.name, style = MaterialTheme.typography.titleMedium)
                                Text("${category.percentage}%", style = MaterialTheme.typography.bodyLarge)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = category.percentage / 100f,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val isNameInvalid = state.newCategoryName.isBlank() && state.newCategoryPercentage.isNotBlank()
            OutlinedTextField(
                value = state.newCategoryName,
                onValueChange = { viewModel.onIntent(CategoriesIntent.UpdateNewCategoryName(it)) },
                label = { Text("Category Name") },
                isError = isNameInvalid,
                supportingText = {
                    if (isNameInvalid) {
                        Text("Category name cannot be empty")
                    }
                },
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            val isPercentageInvalid = state.newCategoryPercentage.isNotBlank() && (state.newCategoryPercentage.toIntOrNull() == null || state.newCategoryPercentage.toInt() < 0)
            OutlinedTextField(
                value = state.newCategoryPercentage,
                onValueChange = { viewModel.onIntent(CategoriesIntent.UpdateNewCategoryPercentage(it)) },
                label = { Text("Percentage") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = isPercentageInvalid || state.totalPercentage != 100,
                supportingText = {
                    if (isPercentageInvalid) {
                        Text("Enter a valid percentage")
                    } else if (state.totalPercentage != 100) {
                        Text("Total percentage must be 100% to save", color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { viewModel.onIntent(CategoriesIntent.AddCategory) },
                enabled = state.newCategoryName.isNotBlank() && !isPercentageInvalid && state.newCategoryPercentage.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Add Category")
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    viewModel.onIntent(CategoriesIntent.SaveCategories)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Categories saved successfully")
                    }
                    onNavigateBack()
                },
                enabled = state.isSaveEnabled,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Save & Back")
            }
        }
    }
}

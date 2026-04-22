package com.example.brokeassistant.feature.budget.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.brokeassistant.R
import com.example.brokeassistant.core.domain.model.Category
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
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.string_categories_management)) },
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
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (state.totalPercentage == 100) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.errorContainer,
                    contentColor = if (state.totalPercentage == 100) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.string_total_percentage, state.totalPercentage), style = MaterialTheme.typography.titleMedium)
                    if (state.totalPercentage != 100) {
                        Text(
                            text = stringResource(R.string.string_total_must_be_100),
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${category.percentage}%", style = MaterialTheme.typography.bodyLarge)
                                    if (state.categories.size > 1) {
                                        IconButton(onClick = { categoryToDelete = category }) {
                                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.string_delete))
                                        }
                                    }
                                }
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
                label = { Text(stringResource(R.string.string_category_name)) },
                isError = isNameInvalid,
                supportingText = {
                    if (isNameInvalid) {
                        Text(stringResource(R.string.string_category_name_empty))
                    }
                },
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            val isPercentageInvalid = state.newCategoryPercentage.isNotBlank() && (state.newCategoryPercentage.toIntOrNull() == null || state.newCategoryPercentage.toInt() < 0)
            OutlinedTextField(
                value = state.newCategoryPercentage,
                onValueChange = { viewModel.onIntent(CategoriesIntent.UpdateNewCategoryPercentage(it)) },
                label = { Text(stringResource(R.string.string_percentage)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = isPercentageInvalid || state.totalPercentage != 100,
                supportingText = {
                    if (isPercentageInvalid) {
                        Text(stringResource(R.string.string_percentage_invalid))
                    } else if (state.totalPercentage != 100) {
                        Text(stringResource(R.string.string_percentage_must_be_100), color = MaterialTheme.colorScheme.error)
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
                Text(stringResource(R.string.add_category))
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            val savedMsg = stringResource(R.string.string_categories_saved)
            Button(
                onClick = {
                    viewModel.onIntent(CategoriesIntent.SaveCategories)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(savedMsg)
                    }
                    onNavigateBack()
                },
                enabled = state.isSaveEnabled,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(stringResource(R.string.string_save_and_back))
            }
        }
    }

    if (categoryToDelete != null) {
        var isDropdownExpanded by remember { mutableStateOf(false) }
        val fallbackOptions = state.categories.filter { it.id != categoryToDelete?.id }
        var fallbackCategory by remember { mutableStateOf<Category?>(fallbackOptions.firstOrNull()) }

        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text(stringResource(R.string.delete_category)) },
            text = {
                Column {
                    Text(stringResource(R.string.string_deleting_requires_fallback, categoryToDelete?.name ?: ""))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = isDropdownExpanded,
                        onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = fallbackCategory?.name ?: stringResource(R.string.string_select_fallback),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.string_fallback_category)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false }
                        ) {
                            fallbackOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.name) },
                                    onClick = {
                                        fallbackCategory = option
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        fallbackCategory?.let { fallback ->
                            categoryToDelete?.let { deleted ->
                                viewModel.onIntent(CategoriesIntent.DeleteCategoryWithFallback(deleted.id, fallback.id))
                            }
                        }
                        categoryToDelete = null
                    },
                    enabled = fallbackCategory != null
                ) {
                    Text(stringResource(R.string.string_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text(stringResource(R.string.string_cancel))
                }
            }
        )
    }
}

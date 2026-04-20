package com.example.brokeassistant.feature.budget.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun CategoriesScreen(
    onNavigateBack: () -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Categories Management")
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Total Percentage: ${state.totalPercentage}%")
        if (state.totalPercentage != 100) {
            Text(
                text = "Total must be exactly 100%",
                color = androidx.compose.ui.graphics.Color.Red
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(state.categories) { category ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(category.name)
                    Text("${category.percentage}%")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = state.newCategoryName,
            onValueChange = { viewModel.onIntent(CategoriesIntent.UpdateNewCategoryName(it)) },
            label = { Text("Category Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = state.newCategoryPercentage,
            onValueChange = { viewModel.onIntent(CategoriesIntent.UpdateNewCategoryPercentage(it)) },
            label = { Text("Percentage") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = { viewModel.onIntent(CategoriesIntent.AddCategory) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Category")
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                viewModel.onIntent(CategoriesIntent.SaveCategories)
                onNavigateBack()
            },
            enabled = state.isSaveEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save & Back")
        }
    }
}

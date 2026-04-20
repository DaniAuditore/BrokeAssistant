package com.example.brokeassistant.feature.onboarding.presentation

import androidx.compose.foundation.layout.*
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
fun OnboardingScreen(
    onNavigateToDashboard: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to BrokeAssistant!")
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = state.selectedCurrency,
            onValueChange = { viewModel.onIntent(OnboardingIntent.SelectCurrency(it)) },
            label = { Text("Currency (e.g., USD, EUR)") }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            viewModel.onIntent(OnboardingIntent.SaveCurrency)
            onNavigateToDashboard()
        }) {
            Text("Save & Go to Dashboard")
        }
    }
}

package com.example.brokeassistant.feature.onboarding.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun OnboardingScreen(
    onNavigateToDashboard: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to BrokeAssistant!",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = state.selectedCurrency,
            onValueChange = { viewModel.onIntent(OnboardingIntent.SelectCurrency(it)) },
            label = { Text("Currency (e.g., USD, EUR)") },
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                viewModel.onIntent(OnboardingIntent.SaveCurrency)
                onNavigateToDashboard()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Get Started")
        }
    }
}

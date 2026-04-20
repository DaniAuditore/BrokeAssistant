package com.example.brokeassistant.feature.onboarding.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingScreen(onNavigateToDashboard: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to BrokeAssistant!")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateToDashboard) {
            Text("Go to Dashboard")
        }
    }
}

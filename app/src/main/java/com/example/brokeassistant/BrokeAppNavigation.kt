package com.example.brokeassistant

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.brokeassistant.feature.budget.presentation.CategoriesScreen
import com.example.brokeassistant.feature.dashboard.presentation.DashboardScreen
import com.example.brokeassistant.feature.onboarding.presentation.OnboardingScreen
import com.example.brokeassistant.feature.transactions.presentation.TransactionsScreen

@Composable
fun BrokeAppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "onboarding") {
        composable("onboarding") {
            OnboardingScreen(
                onNavigateToDashboard = {
                    navController.navigate("dashboard") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        
        composable("dashboard") {
            DashboardScreen(
                onNavigateToCategories = {
                    navController.navigate("categories")
                },
                onNavigateToTransactions = {
                    navController.navigate("transactions")
                }
            )
        }
        
        composable("categories") {
            CategoriesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("transactions") {
            TransactionsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

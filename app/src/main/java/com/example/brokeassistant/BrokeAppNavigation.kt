package com.example.brokeassistant

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.brokeassistant.feature.budget.presentation.CategoriesScreen
import com.example.brokeassistant.feature.budget.presentation.CategoriesViewModel
import com.example.brokeassistant.feature.dashboard.presentation.DashboardScreen
import com.example.brokeassistant.feature.dashboard.presentation.DashboardViewModel
import com.example.brokeassistant.feature.onboarding.presentation.OnboardingScreen
import com.example.brokeassistant.feature.onboarding.presentation.OnboardingViewModel
import com.example.brokeassistant.feature.transactions.presentation.TransactionsScreen
import com.example.brokeassistant.feature.transactions.presentation.TransactionsViewModel

@Composable
fun BrokeAppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "onboarding") {
        composable("onboarding") {
            val viewModel: OnboardingViewModel = hiltViewModel()
            OnboardingScreen(
                onNavigateToDashboard = {
                    navController.navigate("dashboard") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }
        
        composable("dashboard") {
            val viewModel: DashboardViewModel = hiltViewModel()
            DashboardScreen(
                onNavigateToCategories = {
                    navController.navigate("categories")
                },
                onNavigateToTransactions = {
                    navController.navigate("transactions")
                },
                viewModel = viewModel
            )
        }
        
        composable("categories") {
            val viewModel: CategoriesViewModel = hiltViewModel()
            CategoriesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = viewModel
            )
        }
        
        composable("transactions") {
            val viewModel: TransactionsViewModel = hiltViewModel()
            TransactionsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = viewModel
            )
        }
    }
}

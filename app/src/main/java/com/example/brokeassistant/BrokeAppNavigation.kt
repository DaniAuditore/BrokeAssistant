package com.example.brokeassistant

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.brokeassistant.feature.budget.presentation.CategoriesScreen
import com.example.brokeassistant.feature.budget.presentation.CategoriesViewModel
import com.example.brokeassistant.feature.dashboard.presentation.DashboardScreen
import com.example.brokeassistant.feature.dashboard.presentation.DashboardViewModel
import com.example.brokeassistant.feature.onboarding.presentation.OnboardingScreen
import com.example.brokeassistant.feature.onboarding.presentation.OnboardingViewModel
import com.example.brokeassistant.feature.transactions.presentation.TransactionsScreen
import com.example.brokeassistant.feature.transactions.presentation.TransactionsViewModel

sealed class Screen(val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String) {
    object Dashboard : Screen("dashboard", Icons.Rounded.Dashboard, "Dashboard")
    object Transactions : Screen("transactions", Icons.Rounded.AccountBalanceWallet, "Transactions")
    object Categories : Screen("categories", Icons.Rounded.Category, "Categories")
}

@Composable
fun BrokeAppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        Screen.Dashboard,
        Screen.Transactions,
        Screen.Categories
    )
    
    val showBottomBar = currentDestination?.route in items.map { it.route }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    items.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController, 
            startDestination = "onboarding",
            modifier = Modifier.padding(innerPadding)
        ) {
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
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState
                )
            }
            
            composable("transactions") {
                val viewModel: TransactionsViewModel = hiltViewModel()
                TransactionsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}

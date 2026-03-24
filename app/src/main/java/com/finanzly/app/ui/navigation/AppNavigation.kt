package com.finanzly.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.finanzly.app.ui.screens.budget.BudgetScreen
import com.finanzly.app.ui.screens.dashboard.DashboardScreen
import com.finanzly.app.ui.screens.history.HistoryScreen
import com.finanzly.app.ui.screens.transactions.TransactionFormScreen
import com.finanzly.app.ui.viewmodel.FinanzlyViewModel

sealed class Screen(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Dashboard : Screen(
        "dashboard", "Inicio",
        Icons.Filled.Home, Icons.Outlined.Home
    )
    object Form : Screen(
        "form", "Agregar",
        Icons.Filled.AddCircle, Icons.Outlined.AddCircle
    )
    object Budget : Screen(
        "budget", "Presupuesto",
        Icons.Filled.PieChart, Icons.Outlined.PieChart
    )
    object History : Screen(
        "history", "Historial",
        Icons.Filled.History, Icons.Outlined.History
    )
}

val bottomNavItems = listOf(
    Screen.Dashboard, Screen.Form, Screen.Budget, Screen.History
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: FinanzlyViewModel = hiltViewModel()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    val selected = currentDestination?.hierarchy
                        ?.any { it.route == screen.route } == true

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) screen.selectedIcon
                                              else screen.unselectedIcon,
                                contentDescription = screen.label
                            )
                        },
                        label = { Text(screen.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(viewModel = viewModel)
            }
            composable(Screen.Form.route) {
                TransactionFormScreen(
                    viewModel = viewModel,
                    onTransactionAdded = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Budget.route) {
                BudgetScreen(viewModel = viewModel)
            }
            composable(Screen.History.route) {
                HistoryScreen(viewModel = viewModel)
            }
        }
    }
}

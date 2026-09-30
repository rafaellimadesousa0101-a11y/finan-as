package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.TransactionEntity
import com.example.ui.components.AddEditTransactionSheet
import com.example.ui.navigation.BottomNavItems
import com.example.ui.navigation.MinFinanceBottomBar
import com.example.ui.navigation.Screen
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SavingBoxesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TimeTravelScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.FinanceViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(
    viewModel: FinanceViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    val savingBoxes by viewModel.allSavingBoxes.collectAsStateWithLifecycle()

    var showAddEditSheet by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    val isTopLevelDestination = BottomNavItems.any { it.route == currentRoute }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (isTopLevelDestination) {
                MinFinanceBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                popUpTo(Screen.Home.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToTransactions = {
                            navController.navigate(Screen.Transactions.route)
                        },
                        onNavigateToSettings = {
                            navController.navigate(Screen.Settings.route)
                        },
                        onNavigateToTimeTravel = {
                            navController.navigate(Screen.TimeTravel.route)
                        },
                        onAddTransactionClick = {
                            editingTransaction = null
                            showAddEditSheet = true
                        },
                        onAddTransactionWithAmount = { amount ->
                            editingTransaction = TransactionEntity(
                                title = "",
                                amount = amount,
                                type = "EXPENSE",
                                category = "Outros",
                                dateMillis = System.currentTimeMillis()
                            )
                            showAddEditSheet = true
                        },
                        onAddTransactionForDay = { dayMillis ->
                            editingTransaction = TransactionEntity(
                                title = "",
                                amount = 0.0,
                                type = "EXPENSE",
                                category = "Alimentação",
                                dateMillis = dayMillis
                            )
                            showAddEditSheet = true
                        }
                    )
                }

                composable(Screen.Transactions.route) {
                    TransactionsScreen(
                        viewModel = viewModel,
                        onEditTransaction = { transaction ->
                            editingTransaction = transaction
                            showAddEditSheet = true
                        },
                        onAddTransaction = {
                            editingTransaction = null
                            showAddEditSheet = true
                        }
                    )
                }

                composable(Screen.SavingBoxes.route) {
                    SavingBoxesScreen(viewModel = viewModel)
                }

                composable(Screen.Reports.route) {
                    ReportsScreen(viewModel = viewModel)
                }

                composable(Screen.AiAssistant.route) {
                    AiAssistantScreen(viewModel = viewModel)
                }

                composable(Screen.TimeTravel.route) {
                    BackHandler { navController.popBackStack() }
                    TimeTravelScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Settings.route) {
                    BackHandler { navController.popBackStack() }
                    SettingsScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            // Quick Add/Edit Transaction Bottom Sheet
            if (showAddEditSheet) {
                AddEditTransactionSheet(
                    initialTransaction = editingTransaction,
                    savingBoxes = savingBoxes,
                    onDismiss = {
                        showAddEditSheet = false
                        editingTransaction = null
                    },
                    onSave = { transaction ->
                        if (editingTransaction == null) {
                            viewModel.addTransaction(transaction)
                        } else {
                            viewModel.updateTransaction(transaction)
                        }
                        showAddEditSheet = false
                        editingTransaction = null
                    }
                )
            }
        }
    }
}

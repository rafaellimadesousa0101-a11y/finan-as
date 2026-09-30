package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val titlePtBr: String, val icon: ImageVector) {
    object Home : Screen("home", "Início", Icons.Default.AccountBalanceWallet)
    object Transactions : Screen("transactions", "Extrato", Icons.Default.ReceiptLong)
    object SavingBoxes : Screen("saving_boxes", "Caixinhas", Icons.Default.Savings)
    object Reports : Screen("reports", "Relatórios", Icons.Default.BarChart)
    object AiAssistant : Screen("ai_assistant", "Assistente IA", Icons.Default.AutoAwesome)
    object TimeTravel : Screen("time_travel", "Viagem Temporal", Icons.Default.HourglassTop)
    object Settings : Screen("settings", "Ajustes", Icons.Default.Settings)
}

val BottomNavItems = listOf(
    Screen.Home,
    Screen.Transactions,
    Screen.SavingBoxes,
    Screen.Reports,
    Screen.AiAssistant
)

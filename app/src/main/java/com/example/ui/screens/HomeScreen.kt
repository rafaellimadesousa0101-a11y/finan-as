package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.TransactionEntity
import com.example.domain.model.CurrencyUtils
import com.example.domain.model.FinancialCategories
import com.example.domain.model.MonthSummary
import com.example.ui.components.CalculatorDialog
import com.example.ui.components.MonthRolloverDialog
import com.example.ui.components.MonthSelector
import com.example.ui.components.MonthlyCalendarSheet
import com.example.ui.theme.Amber400
import com.example.ui.theme.Amber500
import com.example.ui.theme.Carmine500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.viewmodel.FinanceViewModel

@Composable
fun HomeScreen(
    viewModel: FinanceViewModel,
    onNavigateToTransactions: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToTimeTravel: () -> Unit,
    onAddTransactionClick: () -> Unit,
    onAddTransactionWithAmount: (Double) -> Unit = {},
    onAddTransactionForDay: (Long) -> Unit = {}
) {
    val monthSummary by viewModel.currentMonthSummary.collectAsStateWithLifecycle()
    val transactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val timeTravelOffset by viewModel.timeTravelOffset.collectAsStateWithLifecycle()
    val rolloverInfo by viewModel.rolloverEvent.collectAsStateWithLifecycle()
    val dailyMarkers by viewModel.dailyMarkers.collectAsStateWithLifecycle()
    val selectedCal by viewModel.selectedCalendar.collectAsStateWithLifecycle()

    var showCalendarSheet by remember { mutableStateOf(false) }
    var showCalculator by remember { mutableStateOf(false) }

    // Rollover dialog
    if (rolloverInfo != null) {
        MonthRolloverDialog(
            rolloverInfo = rolloverInfo!!,
            onConfirm = { viewModel.dismissRolloverEvent() }
        )
    }

    // Monthly Calendar Sheet
    if (showCalendarSheet) {
        MonthlyCalendarSheet(
            calendar = selectedCal,
            markers = dailyMarkers,
            transactions = transactions,
            onDismiss = { showCalendarSheet = false },
            onAddTransactionForDay = { dayMillis ->
                showCalendarSheet = false
                onAddTransactionForDay(dayMillis)
            }
        )
    }

    // Floating Calculator
    if (showCalculator) {
        CalculatorDialog(
            initialValue = 0.0,
            onDismiss = { showCalculator = false },
            onApplyResult = { calculatedAmount ->
                showCalculator = false
                onAddTransactionWithAmount(calculatedAmount)
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Olá, ${userSettings?.userName ?: "Rafael"} 👋",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (timeTravelOffset > 0) "Modo Viagem Temporal (+${timeTravelOffset} meses)" else "Controle Financeiro Minimalista",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (timeTravelOffset > 0) Indigo500 else Slate400
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { showCalculator = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Indigo500.copy(alpha = 0.15f))
                        ) {
                            Icon(imageVector = Icons.Default.Calculate, contentDescription = "Calculadora", tint = Indigo500)
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = onNavigateToTimeTravel,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(imageVector = Icons.Default.HourglassTop, contentDescription = "Simulação", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = "Ajustes", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Month Selector with Calendar quick sheet trigger
            item {
                MonthSelector(
                    monthSummary = monthSummary,
                    timeTravelOffset = timeTravelOffset,
                    onPreviousMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() },
                    onOpenCalendar = { showCalendarSheet = true },
                    onOpenTimeTravel = onNavigateToTimeTravel
                )
            }

            // Main Balance Card: Saldo Atual
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("current_balance_card"),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SALDO ATUAL DISPONÍVEL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = Slate400
                            )

                            // Savings rate pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (monthSummary.savingsRate >= 0) Emerald500.copy(alpha = 0.15f)
                                        else Carmine500.copy(alpha = 0.15f)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "Poupança: ${String.format("%.1f", monthSummary.savingsRate)}%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (monthSummary.savingsRate >= 0) Emerald500 else Carmine500
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = CurrencyUtils.formatBrl(monthSummary.currentBalance),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 34.sp
                            ),
                            color = if (monthSummary.currentBalance >= 0) Emerald500 else Carmine500
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Slate800)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Incomes and Expenses Breakdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Entradas
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Emerald500
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Entradas", style = MaterialTheme.typography.labelSmall, color = Slate400)
                                }
                                Text(
                                    text = CurrencyUtils.formatBrl(monthSummary.totalIncome),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Saídas Pagas
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Carmine500
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Saídas Pagas", style = MaterialTheme.typography.labelSmall, color = Slate400)
                                }
                                Text(
                                    text = CurrencyUtils.formatBrl(monthSummary.paidExpenses),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Gastos Previstos (A Pagar)
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Amber500
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("A Pagar", style = MaterialTheme.typography.labelSmall, color = Slate400)
                                }
                                Text(
                                    text = CurrencyUtils.formatBrl(monthSummary.pendingExpenses),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Amber500
                                )
                            }
                        }
                    }
                }
            }

            // Projected End of Month Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Saldo Projetado no Fim do Mês",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Slate400
                            )
                            Text(
                                text = "(Saldo Atual - Gastos a Pagar)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }

                        Text(
                            text = CurrencyUtils.formatBrl(monthSummary.projectedBalance),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (monthSummary.projectedBalance >= 0) Emerald500 else Carmine500
                            )
                        )
                    }
                }
            }

            // Pending Bills Alert Card (if any pending expense in month)
            val pendingTransactions = transactions.filter { it.type == "EXPENSE" && !it.isPaid }
            if (pendingTransactions.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Amber500.copy(alpha = 0.1f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Amber500.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = Amber500,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${pendingTransactions.size} Contas a Vencer no Mês",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Text(
                                    text = CurrencyUtils.formatBrl(pendingTransactions.sumOf { it.amount }),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Amber500
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Show first 2 pending items with 1-tap "Pagar" button
                            pendingTransactions.take(2).forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Vencimento: ${CurrencyUtils.formatShortDate(item.dueDateMillis ?: item.dateMillis)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Slate400
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = CurrencyUtils.formatBrl(item.amount),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Carmine500
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = { viewModel.markAsPaid(item.id) },
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Pagar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Recent Transactions Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Movimentações Recentes",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    TextButton(onClick = onNavigateToTransactions) {
                        Text("Ver Extrato", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Recent Items list (top 5)
            if (transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhum lançamento cadastrado neste mês.\nToque no botão '+' abaixo para adicionar!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate400,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(transactions.take(5)) { item ->
                    val cat = FinancialCategories.getByName(item.category)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(cat.color.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = cat.icon,
                                        contentDescription = item.category,
                                        tint = cat.color,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${cat.namePtBr} • ${CurrencyUtils.formatShortDate(item.dateMillis)}" +
                                                if (!item.isPaid) " • A Pagar" else "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (!item.isPaid) Amber500 else Slate400
                                    )
                                }
                            }

                            Text(
                                text = (if (item.type == "INCOME") "+ " else "- ") + CurrencyUtils.formatBrl(item.amount),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (item.type == "INCOME") Emerald500 else if (!item.isPaid) Amber500 else Carmine500
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Floating Action Button to Add
        FloatingActionButton(
            onClick = onAddTransactionClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_transaction_fab"),
            containerColor = Emerald500,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Adicionar Lançamento", modifier = Modifier.size(28.dp))
        }
    }
}

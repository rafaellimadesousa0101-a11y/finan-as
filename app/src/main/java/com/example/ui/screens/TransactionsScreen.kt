package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import com.example.ui.components.MonthSelector
import com.example.ui.components.MonthlyCalendarSheet
import com.example.ui.theme.Amber500
import com.example.ui.theme.Carmine500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.viewmodel.FinanceViewModel
import com.example.viewmodel.TransactionFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: FinanceViewModel,
    onEditTransaction: (TransactionEntity) -> Unit,
    onAddTransaction: () -> Unit
) {
    val transactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val monthSummary by viewModel.currentMonthSummary.collectAsStateWithLifecycle()
    val currentFilter by viewModel.currentFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val timeTravelOffset by viewModel.timeTravelOffset.collectAsStateWithLifecycle()
    val dailyMarkers by viewModel.dailyMarkers.collectAsStateWithLifecycle()
    val selectedCal by viewModel.selectedCalendar.collectAsStateWithLifecycle()

    var showCalendarSheet by remember { mutableStateOf(false) }

    if (showCalendarSheet) {
        MonthlyCalendarSheet(
            calendar = selectedCal,
            markers = dailyMarkers,
            transactions = transactions,
            onDismiss = { showCalendarSheet = false },
            onAddTransactionForDay = { dayMillis ->
                showCalendarSheet = false
                onEditTransaction(
                    TransactionEntity(
                        title = "",
                        amount = 0.0,
                        type = "EXPENSE",
                        category = "Alimentação",
                        dateMillis = dayMillis
                    )
                )
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = "Extrato Financeiro",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Month Selector
            MonthSelector(
                monthSummary = monthSummary,
                timeTravelOffset = timeTravelOffset,
                onPreviousMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() },
                onOpenCalendar = { showCalendarSheet = true },
                onOpenTimeTravel = {}
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transactions_search_input"),
                placeholder = { Text("Buscar lançamentos ou categorias...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Slate400)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpar")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TransactionFilter.values().forEach { filter ->
                    val isSelected = currentFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setFilter(filter) },
                        label = { Text(filter.labelPtBr, fontSize = 12.sp) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("filter_${filter.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Transaction Count & Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${transactions.size} lançamentos encontrados",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate400
                )
                val sumAmount = transactions.sumOf { if (it.type == "INCOME") it.amount else -it.amount }
                Text(
                    text = "Líquido: " + CurrencyUtils.formatBrl(sumAmount),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (sumAmount >= 0) Emerald500 else Carmine500
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Transactions List
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Nenhum resultado para a pesquisa." else "Nenhum lançamento neste filtro.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate400
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = transactions,
                        key = { it.id }
                    ) { item ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.deleteTransaction(item.id)
                                    true
                                } else false
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = {
                                val color by animateColorAsState(
                                    if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) Carmine500 else Color.Transparent,
                                    label = "swipe_color"
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(color)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Excluir",
                                        tint = Color.White
                                    )
                                }
                            }
                        ) {
                            val cat = FinancialCategories.getByName(item.category)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onEditTransaction(item) }
                                    .testTag("transaction_item_${item.id}"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(cat.color.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = cat.icon,
                                                contentDescription = item.category,
                                                tint = cat.color,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = item.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${cat.namePtBr} • ${CurrencyUtils.formatShortDate(item.dateMillis)}" +
                                                        if (!item.isPaid) " • Venc: ${CurrencyUtils.formatShortDate(item.dueDateMillis ?: item.dateMillis)}" else "",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (!item.isPaid) Amber500 else Slate400
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = (if (item.type == "INCOME") "+ " else "- ") + CurrencyUtils.formatBrl(item.amount),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (item.type == "INCOME") Emerald500 else if (!item.isPaid) Amber500 else Carmine500
                                        )

                                        if (item.type == "EXPENSE" && !item.isPaid) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Button(
                                                onClick = { viewModel.markAsPaid(item.id) },
                                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                                                modifier = Modifier.height(26.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Pagar", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // FAB to Add Transaction
        FloatingActionButton(
            onClick = onAddTransaction,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_transaction_fab_screen"),
            containerColor = Emerald500,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Novo Lançamento", modifier = Modifier.size(28.dp))
        }
    }
}

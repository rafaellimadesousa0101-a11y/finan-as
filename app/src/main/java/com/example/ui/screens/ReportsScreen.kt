package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.CurrencyUtils
import com.example.ui.components.CategoryPieChart
import com.example.ui.components.IncomeExpenseBarChart
import com.example.ui.components.MonthSelector
import com.example.ui.theme.Amber500
import com.example.ui.theme.Carmine500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.viewmodel.FinanceViewModel

@Composable
fun ReportsScreen(
    viewModel: FinanceViewModel
) {
    val monthSummary by viewModel.currentMonthSummary.collectAsStateWithLifecycle()
    val categoryStats by viewModel.categoryStats.collectAsStateWithLifecycle()
    val monthlyComparison by viewModel.monthlyComparison.collectAsStateWithLifecycle()
    val budgetHealth by viewModel.budgetHealth.collectAsStateWithLifecycle()
    val timeTravelOffset by viewModel.timeTravelOffset.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Title
        Text(
            text = "Relatórios & Diagnóstico",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Análise gráfica e previsibilidade do seu orçamento",
            style = MaterialTheme.typography.labelSmall,
            color = Slate400
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Month Selector
        MonthSelector(
            monthSummary = monthSummary,
            timeTravelOffset = timeTravelOffset,
            onPreviousMonth = { viewModel.previousMonth() },
            onNextMonth = { viewModel.nextMonth() },
            onOpenCalendar = {},
            onOpenTimeTravel = {}
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Card 1: Budget Health & Consistency Score
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("budget_health_card"),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        budgetHealth.score >= 80 -> Emerald500.copy(alpha = 0.2f)
                                        budgetHealth.score >= 60 -> Amber500.copy(alpha = 0.2f)
                                        else -> Carmine500.copy(alpha = 0.2f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolunteerActivism,
                                contentDescription = null,
                                tint = when {
                                    budgetHealth.score >= 80 -> Emerald500
                                    budgetHealth.score >= 60 -> Amber500
                                    else -> Carmine500
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Saúde Financeira: ${budgetHealth.statusLabel}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Score ${budgetHealth.score} de 100",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    budgetHealth.score >= 80 -> Emerald500.copy(alpha = 0.15f)
                                    budgetHealth.score >= 60 -> Amber500.copy(alpha = 0.15f)
                                    else -> Carmine500.copy(alpha = 0.15f)
                                }
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${budgetHealth.score}/100",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = when {
                                budgetHealth.score >= 80 -> Emerald500
                                budgetHealth.score >= 60 -> Amber500
                                else -> Carmine500
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = budgetHealth.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Card 2: Category Pie / Donut Chart
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("category_pie_chart_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.PieChart, contentDescription = null, tint = Indigo500)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Despesas por Categoria",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                CategoryPieChart(categoryStats = categoryStats)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Card 3: Month-over-month comparison bar chart
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("monthly_bar_chart_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.BarChart, contentDescription = null, tint = Emerald500)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Histórico: Receitas vs Despesas",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                IncomeExpenseBarChart(comparisonStats = monthlyComparison)
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

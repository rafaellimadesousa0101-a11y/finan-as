package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CurrencyUtils
import com.example.ui.theme.Carmine500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.viewmodel.MonthComparisonStat

@Composable
fun IncomeExpenseBarChart(
    comparisonStats: List<MonthComparisonStat>,
    modifier: Modifier = Modifier
) {
    if (comparisonStats.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Sem dados comparativos suficientes",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate400
            )
        }
        return
    }

    val maxVal = comparisonStats.maxOfOrNull { maxOf(it.totalIncome, it.totalExpense) }?.coerceAtLeast(100.0) ?: 100.0
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(comparisonStats) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 750)
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Emerald500)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Receitas", style = MaterialTheme.typography.labelSmall, color = Slate400)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Carmine500)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Despesas", style = MaterialTheme.typography.labelSmall, color = Slate400)
            }
        }

        // Canvas for bars
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            val chartHeight = size.height - 24.dp.toPx()
            val numGroups = comparisonStats.size
            val groupWidth = size.width / numGroups
            val barWidth = 12.dp.toPx()
            val barSpacing = 4.dp.toPx()

            // Baseline
            drawLine(
                color = Color(0xFF334155),
                start = Offset(0f, chartHeight),
                end = Offset(size.width, chartHeight),
                strokeWidth = 1.dp.toPx()
            )

            comparisonStats.forEachIndexed { index, stat ->
                val centerX = index * groupWidth + (groupWidth / 2)

                // Income bar
                val incomeHeight = ((stat.totalIncome / maxVal) * chartHeight * animatedProgress.value).toFloat()
                val incomeLeft = centerX - barWidth - (barSpacing / 2)
                val incomeTop = chartHeight - incomeHeight

                drawRoundRect(
                    color = Emerald500,
                    topLeft = Offset(incomeLeft, incomeTop),
                    size = Size(barWidth, incomeHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Expense bar
                val expenseHeight = ((stat.totalExpense / maxVal) * chartHeight * animatedProgress.value).toFloat()
                val expenseLeft = centerX + (barSpacing / 2)
                val expenseTop = chartHeight - expenseHeight

                drawRoundRect(
                    color = Carmine500,
                    topLeft = Offset(expenseLeft, expenseTop),
                    size = Size(barWidth, expenseHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }
        }

        // Labels row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            comparisonStats.forEach { stat ->
                Text(
                    text = stat.monthLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = Slate400,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

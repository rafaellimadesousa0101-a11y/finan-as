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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.CurrencyUtils
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun TimeTravelScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val currentOffset by viewModel.timeTravelOffset.collectAsStateWithLifecycle()
    val monthSummary by viewModel.currentMonthSummary.collectAsStateWithLifecycle()
    val savingBoxes by viewModel.allSavingBoxes.collectAsStateWithLifecycle()

    var sliderValue by remember { mutableFloatStateOf(currentOffset.toFloat()) }
    val monthsAhead = sliderValue.toInt()

    val targetDate = Calendar.getInstance().apply {
        add(Calendar.MONTH, monthsAhead)
    }
    val targetDateName = SimpleDateFormat("MMMM 'de' yyyy", Locale("pt", "BR"))
        .format(targetDate.time).replaceFirstChar { it.uppercase() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = "Modo Viagem no Tempo",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Simulação e projeção do seu patrimônio futuro",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate400
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Explanation Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Indigo500.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Indigo500.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(imageVector = Icons.Default.HourglassTop, contentDescription = null, tint = Indigo500, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Este modo altera o estado temporal da aplicação, projetando receitas recorrentes (salário), despesas fixas estimadas e o ritmo das suas caixinhas ao longo do tempo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Simulation Slider Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Avançar no Tempo",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (monthsAhead == 0) "Mês atual (Tempo real)" else "Simulando +$monthsAhead meses no futuro ($targetDateName)",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (monthsAhead > 0) Indigo500 else Slate400
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Slider(
                    value = sliderValue,
                    onValueChange = {
                        sliderValue = it
                        viewModel.setTimeTravelOffset(it.toInt())
                    },
                    valueRange = 0f..12f,
                    steps = 11,
                    colors = SliderDefaults.colors(
                        thumbColor = Indigo500,
                        activeTrackColor = Indigo500,
                        inactiveTrackColor = Slate800
                    ),
                    modifier = Modifier.testTag("time_travel_slider")
                )

                // Quick jump buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(0, 1, 3, 6, 12).forEach { m ->
                        Text(
                            text = if (m == 0) "Hoje" else "+${m}m",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (monthsAhead == m) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (monthsAhead == m) Indigo500 else Slate400
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Simulated Balance Card
        val monthlySavingCapacity = (monthSummary.estimatedFixedIncome - 2500.0).coerceAtLeast(500.0)
        val projectedTotalBalance = monthSummary.currentBalance + (monthsAhead * monthlySavingCapacity)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "SALDO TOTAL PROJETADO EM $targetDateName",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = Slate400
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = CurrencyUtils.formatBrl(projectedTotalBalance),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Emerald500
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Capacidade Mensal", style = MaterialTheme.typography.labelSmall, color = Slate400)
                        Text(CurrencyUtils.formatBrl(monthlySavingCapacity), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Aporte Estimado Caixinhas", style = MaterialTheme.typography.labelSmall, color = Slate400)
                        Text(CurrencyUtils.formatBrl(monthsAhead * (monthlySavingCapacity * 0.4)), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Indigo500))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Reset and back buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    sliderValue = 0f
                    viewModel.resetToCurrentMonth()
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tempo Real")
            }

            Button(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Indigo500)
            ) {
                Text("Concluído", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

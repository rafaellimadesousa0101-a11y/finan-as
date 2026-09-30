package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CurrencyUtils
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850

@Composable
fun CalculatorDialog(
    initialValue: Double = 0.0,
    onDismiss: () -> Unit,
    onApplyResult: (Double) -> Unit
) {
    var display by remember { mutableStateOf(if (initialValue > 0) String.format(java.util.Locale.US, "%.2f", initialValue) else "0") }
    var expression by remember { mutableStateOf("") }
    var operand1 by remember { mutableStateOf<Double?>(null) }
    var pendingOp by remember { mutableStateOf<String?>(null) }
    var isNewInput by remember { mutableStateOf(false) }

    fun currentDouble(): Double = display.replace(',', '.').toDoubleOrNull() ?: 0.0

    fun calculate() {
        val op1 = operand1 ?: return
        val op2 = currentDouble()
        val res = when (pendingOp) {
            "+" -> op1 + op2
            "-" -> op1 - op2
            "×" -> op1 * op2
            "÷" -> if (op2 != 0.0) op1 / op2 else 0.0
            else -> op2
        }
        val cleanRes = if (res % 1.0 == 0.0) res.toLong().toString() else String.format(java.util.Locale.US, "%.2f", res)
        display = cleanRes
        expression = ""
        operand1 = null
        pendingOp = null
        isNewInput = true
    }

    fun handleDigit(d: String) {
        if (isNewInput || display == "0") {
            display = d
            isNewInput = false
        } else {
            if (display.length < 10) {
                display += d
            }
        }
    }

    fun handleDecimal() {
        if (isNewInput) {
            display = "0."
            isNewInput = false
        } else if (!display.contains(".")) {
            display += "."
        }
    }

    fun handleOperation(op: String) {
        if (operand1 != null && pendingOp != null && !isNewInput) {
            calculate()
        }
        operand1 = currentDouble()
        pendingOp = op
        expression = "$display $op"
        isNewInput = true
    }

    fun handlePercent() {
        val curr = currentDouble()
        val res = curr / 100.0
        display = String.format(java.util.Locale.US, "%.2f", res)
        isNewInput = true
    }

    fun handleBackspace() {
        if (display.length > 1 && !isNewInput) {
            display = display.dropLast(1)
        } else {
            display = "0"
            isNewInput = false
        }
    }

    fun handleClear() {
        display = "0"
        expression = ""
        operand1 = null
        pendingOp = null
        isNewInput = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Calculate, contentDescription = null, tint = Indigo500)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Calculadora Financeira",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Display box
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    color = Slate850
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = if (expression.isNotBlank()) expression else " ",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = CurrencyUtils.formatBrl(currentDouble()),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Keypad grid (4x5)
                val buttons = listOf(
                    listOf("C", "%", "⌫", "÷"),
                    listOf("7", "8", "9", "×"),
                    listOf("4", "5", "6", "-"),
                    listOf("1", "2", "3", "+"),
                    listOf("00", "0", ".", "=")
                )

                buttons.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        row.forEach { btn ->
                            val isOp = btn in listOf("÷", "×", "-", "+", "=")
                            val isAction = btn in listOf("C", "%", "⌫")

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when {
                                            btn == "=" -> MaterialTheme.colorScheme.primary
                                            isOp -> Indigo500.copy(alpha = 0.25f)
                                            isAction -> Slate800
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    )
                                    .clickable {
                                        when (btn) {
                                            "C" -> handleClear()
                                            "%" -> handlePercent()
                                            "⌫" -> handleBackspace()
                                            "=" -> calculate()
                                            "." -> handleDecimal()
                                            "00" -> {
                                                handleDigit("0")
                                                handleDigit("0")
                                            }
                                            in listOf("+", "-", "×", "÷") -> handleOperation(btn)
                                            else -> handleDigit(btn)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (btn == "⌫") {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                                        contentDescription = "Apagar",
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                } else {
                                    Text(
                                        text = btn,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = if (isOp || btn == "=") FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (btn == "=") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Apply button
                Button(
                    onClick = {
                        calculate()
                        onApplyResult(currentDouble())
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Aplicar ${CurrencyUtils.formatBrl(currentDouble())}",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

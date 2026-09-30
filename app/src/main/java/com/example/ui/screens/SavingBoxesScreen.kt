package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.SavingBoxEntity
import com.example.domain.model.CurrencyUtils
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.viewmodel.FinanceViewModel

fun getSavingBoxIcon(iconName: String): ImageVector {
    return when (iconName) {
        "Shield" -> Icons.Default.Shield
        "Flight" -> Icons.Default.Flight
        "Laptop" -> Icons.Default.Laptop
        "Home" -> Icons.Default.Home
        "Car" -> Icons.Default.DirectionsCar
        "Gift" -> Icons.Default.CardGiftcard
        else -> Icons.Default.Savings
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingBoxesScreen(
    viewModel: FinanceViewModel
) {
    val savingBoxes by viewModel.allSavingBoxes.collectAsStateWithLifecycle()
    val monthSummary by viewModel.currentMonthSummary.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    var activeDepositBox by remember { mutableStateOf<SavingBoxEntity?>(null) }
    var activeWithdrawBox by remember { mutableStateOf<SavingBoxEntity?>(null) }
    var showArchived by remember { mutableStateOf(false) }

    val totalSaved = savingBoxes.filter { !it.isArchived }.sumOf { it.currentAmount }
    val displayedBoxes = savingBoxes.filter { if (showArchived) it.isArchived else !it.isArchived }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Caixinhas & Metas",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Separe dinheiro para seus objetivos",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400
                    )
                }

                TextButton(onClick = { showArchived = !showArchived }) {
                    Text(
                        text = if (showArchived) "Ver Ativas" else "Ver Arquivadas",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Indigo500
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Total Saved Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "TOTAL GUARDADO NAS CAIXINHAS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = Slate400
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = CurrencyUtils.formatBrl(totalSaved),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Indigo500
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Saldo livre disponível na conta corrente: ${CurrencyUtils.formatBrl(monthSummary.currentBalance)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Boxes List
            if (displayedBoxes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (showArchived) "Nenhuma caixinha arquivada." else "Nenhuma caixinha criada ainda.\nToque no botão '+' abaixo para criar sua primeira meta!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate400,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayedBoxes, key = { it.id }) { box ->
                        val boxColor = try {
                            Color(android.graphics.Color.parseColor(box.colorHex))
                        } catch (e: Exception) {
                            Indigo500
                        }
                        val progress = if (box.targetAmount > 0) {
                            (box.currentAmount / box.targetAmount).coerceIn(0.0, 1.0).toFloat()
                        } else 0f
                        val progressPercent = (progress * 100).toInt()

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("saving_box_item_${box.id}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(boxColor.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = getSavingBoxIcon(box.iconName),
                                                contentDescription = box.name,
                                                tint = boxColor,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = box.name,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = box.category + if (box.targetDateMillis != null) " • Prazo: ${CurrencyUtils.formatShortDate(box.targetDateMillis)}" else "",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Slate400
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { viewModel.toggleArchiveSavingBox(box) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (box.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                                            contentDescription = "Arquivar",
                                            tint = Slate400,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Progress row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column {
                                        Text(
                                            text = "Guardado",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Slate400
                                        )
                                        Text(
                                            text = CurrencyUtils.formatBrl(box.currentAmount),
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                            color = boxColor
                                        )
                                    }

                                    if (box.targetAmount > 0) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "Meta: ${CurrencyUtils.formatBrl(box.targetAmount)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Slate400
                                            )
                                            Text(
                                                text = "$progressPercent%",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }

                                if (box.targetAmount > 0) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = boxColor,
                                        trackColor = Slate800
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Action Buttons: Guardar and Resgatar
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { activeDepositBox = box },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                            .testTag("deposit_box_${box.id}"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = boxColor)
                                    ) {
                                        Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Guardar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }

                                    OutlinedButton(
                                        onClick = { activeWithdrawBox = box },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                            .testTag("withdraw_box_${box.id}"),
                                        shape = RoundedCornerShape(10.dp),
                                        enabled = box.currentAmount > 0
                                    ) {
                                        Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Resgatar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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

        // FAB to create Box
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("create_saving_box_fab"),
            containerColor = Indigo500,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Nova Caixinha", modifier = Modifier.size(28.dp))
        }
    }

    // Create Box Dialog
    if (showCreateDialog) {
        CreateSavingBoxDialog(
            onDismiss = { showCreateDialog = false },
            onSave = { name, cat, target, deadline, colorHex, iconName ->
                viewModel.createSavingBox(name, cat, target, deadline, colorHex, iconName)
                showCreateDialog = false
            }
        )
    }

    // Deposit Dialog
    activeDepositBox?.let { box ->
        TransferSavingBoxDialog(
            title = "Guardar na Caixinha: ${box.name}",
            subtitle = "O valor será debitado do seu saldo em conta corrente.",
            confirmLabel = "Confirmar Depósito",
            confirmColor = Emerald500,
            onDismiss = { activeDepositBox = null },
            onConfirm = { amount ->
                viewModel.depositToSavingBox(box, amount)
                activeDepositBox = null
            }
        )
    }

    // Withdraw Dialog
    activeWithdrawBox?.let { box ->
        TransferSavingBoxDialog(
            title = "Resgatar da Caixinha: ${box.name}",
            subtitle = "Saldo guardado nesta meta: ${CurrencyUtils.formatBrl(box.currentAmount)}. O valor voltará para a conta corrente.",
            confirmLabel = "Confirmar Resgate",
            confirmColor = Indigo500,
            maxAmount = box.currentAmount,
            onDismiss = { activeWithdrawBox = null },
            onConfirm = { amount ->
                viewModel.withdrawFromSavingBox(box, amount)
                activeWithdrawBox = null
            }
        )
    }
}

@Composable
fun TransferSavingBoxDialog(
    title: String,
    subtitle: String,
    confirmLabel: String,
    confirmColor: Color,
    maxAmount: Double? = null,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    val quickChips = listOf(50.0, 100.0, 200.0, 500.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.replace(',', '.').toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onConfirm(amt)
                    }
                },
                enabled = (amountText.replace(',', '.').toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = confirmColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(confirmLabel, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        title = {
            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Slate400)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Valor (R$)") },
                    placeholder = { Text("0,00") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickChips.forEach { chipVal ->
                        val canUse = maxAmount == null || chipVal <= maxAmount
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable(enabled = canUse) {
                                    amountText = String.format(java.util.Locale.US, "%.2f", chipVal)
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "R$ ${chipVal.toInt()}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (canUse) MaterialTheme.colorScheme.onSurface else Slate700
                            )
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSavingBoxDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, cat: String, target: Double, deadline: Long?, colorHex: String, iconName: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Investimentos") }
    var selectedColorHex by remember { mutableStateOf("#6366F1") }
    var selectedIcon by remember { mutableStateOf("Savings") }
    var deadlineMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis() + 86400000L * 90)

    val colors = listOf("#6366F1", "#10B981", "#F59E0B", "#EF4444", "#EC4899", "#0EA5E9")
    val icons = listOf("Savings", "Shield", "Flight", "Laptop", "Home", "Car", "Gift")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val target = targetText.replace(',', '.').toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank()) {
                        onSave(name.trim(), selectedCategory, target, deadlineMillis, selectedColorHex, selectedIcon)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Criar Caixinha", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        title = {
            Text("Nova Caixinha / Meta", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Meta") },
                    placeholder = { Text("ex: Reserva de Emergência, Viagem...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("Meta Desejada R$ (Opcional)") },
                    placeholder = { Text("0,00") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Deadline
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (deadlineMillis != null) "Prazo: ${CurrencyUtils.formatShortDate(deadlineMillis!!)}" else "Definir Prazo (Opcional)",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text("Escolher", style = MaterialTheme.typography.labelSmall, color = Indigo500)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Color picker
                Text("Cor de Destaque", style = MaterialTheme.typography.labelSmall, color = Slate400)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.forEach { hex ->
                        val c = Color(android.graphics.Color.parseColor(hex))
                        val isSel = selectedColorHex == hex
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(if (isSel) 2.dp else 0.dp, Color.White, CircleShape)
                                .clickable { selectedColorHex = hex }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Icon picker
                Text("Ícone", style = MaterialTheme.typography.labelSmall, color = Slate400)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    icons.forEach { iconName ->
                        val isSel = selectedIcon == iconName
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) Indigo500.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                                .border(if (isSel) 1.5.dp else 0.dp, Indigo500, RoundedCornerShape(8.dp))
                                .clickable { selectedIcon = iconName },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getSavingBoxIcon(iconName),
                                contentDescription = iconName,
                                modifier = Modifier.size(18.dp),
                                tint = if (isSel) Indigo500 else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    deadlineMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

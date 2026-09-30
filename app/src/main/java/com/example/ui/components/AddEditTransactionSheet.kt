package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SavingBoxEntity
import com.example.data.local.TransactionEntity
import com.example.domain.model.CurrencyUtils
import com.example.domain.model.FinancialCategories
import com.example.domain.model.TransactionType
import com.example.ui.theme.Carmine500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditTransactionSheet(
    initialTransaction: TransactionEntity? = null,
    savingBoxes: List<SavingBoxEntity> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var type by remember(initialTransaction) {
        mutableStateOf(if (initialTransaction?.type == "INCOME") TransactionType.INCOME else TransactionType.EXPENSE)
    }
    var title by remember(initialTransaction) {
        mutableStateOf(initialTransaction?.title ?: "")
    }
    var amountText by remember(initialTransaction) {
        mutableStateOf(if (initialTransaction != null) String.format(java.util.Locale.US, "%.2f", initialTransaction.amount) else "")
    }
    var selectedCategory by remember(initialTransaction) {
        mutableStateOf(
            initialTransaction?.category ?: (if (initialTransaction?.type == "INCOME") "Salário" else "Alimentação")
        )
    }
    var dateMillis by remember(initialTransaction) {
        mutableLongStateOf(initialTransaction?.dateMillis ?: System.currentTimeMillis())
    }
    var isPaid by remember(initialTransaction) {
        mutableStateOf(initialTransaction?.isPaid ?: true)
    }
    var dueDateMillis by remember(initialTransaction) {
        mutableStateOf(initialTransaction?.dueDateMillis)
    }
    var selectedSavingBoxId by remember(initialTransaction) {
        mutableStateOf(initialTransaction?.savingBoxId)
    }
    var notes by remember(initialTransaction) {
        mutableStateOf(initialTransaction?.notes ?: "")
    }

    var showCalculator by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDueDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
    val dueDatePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDateMillis ?: (System.currentTimeMillis() + 86400000L * 7))

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialTransaction == null) "Novo Lançamento" else "Editar Lançamento",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Type Toggle: Receita vs Despesa
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (type == TransactionType.EXPENSE) Carmine500 else androidx.compose.ui.graphics.Color.Transparent)
                        .clickable {
                            type = TransactionType.EXPENSE
                            if (FinancialCategories.expenseCategories.none { it.namePtBr == selectedCategory }) {
                                selectedCategory = "Alimentação"
                            }
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Despesa",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (type == TransactionType.EXPENSE) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (type == TransactionType.INCOME) Emerald500 else androidx.compose.ui.graphics.Color.Transparent)
                        .clickable {
                            type = TransactionType.INCOME
                            if (FinancialCategories.incomeCategories.none { it.namePtBr == selectedCategory }) {
                                selectedCategory = "Salário"
                            }
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Receita",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (type == TransactionType.INCOME) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Amount Field with Calculator trigger
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Valor (R$)") },
                    placeholder = { Text("0,00") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("transaction_amount_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (type == TransactionType.INCOME) Emerald500 else Carmine500
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { showCalculator = true },
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Indigo500.copy(alpha = 0.15f))
                        .testTag("calculator_shortcut_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = "Abrir Calculadora",
                        tint = Indigo500
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description / Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Descrição") },
                placeholder = { Text("ex: Supermercado, Salário, Conta de Luz") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transaction_title_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Categories
            Text(
                text = "Categoria",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Slate400
            )
            Spacer(modifier = Modifier.height(6.dp))

            val currentCategories = if (type == TransactionType.INCOME) FinancialCategories.incomeCategories else FinancialCategories.expenseCategories

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                currentCategories.forEach { cat ->
                    val isSelected = selectedCategory == cat.namePtBr
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat.namePtBr },
                        label = { Text(cat.namePtBr, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = cat.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else cat.color
                            )
                        },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Date picker button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { showDatePicker = true }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = Slate400)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Data: ${CurrencyUtils.formatDatePtBr(dateMillis)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text("Alterar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }

            // Status: Paid vs Pending (for expenses)
            if (type == TransactionType.EXPENSE) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isPaid) "Status: Já Paga" else "Status: A Pagar (Prevista)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isPaid) Emerald500 else Carmine500
                        )
                        Text(
                            text = if (isPaid) "Valor já debitado do saldo" else "Conta pendente com vencimento",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate400
                        )
                    }

                    Switch(
                        checked = isPaid,
                        onCheckedChange = {
                            isPaid = it
                            if (!it && dueDateMillis == null) {
                                dueDateMillis = dateMillis
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Emerald500,
                            checkedTrackColor = Emerald500.copy(alpha = 0.3f)
                        )
                    )
                }

                // If pending, due date selector
                if (!isPaid) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                            .clickable { showDueDatePicker = true }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Data de Vencimento: ${CurrencyUtils.formatDatePtBr(dueDateMillis ?: dateMillis)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = Carmine500
                        )
                        Text("Mudar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Link to Saving Box (Optional)
            if (savingBoxes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Vincular a uma Caixinha (Opcional)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Slate400
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedSavingBoxId == null,
                        onClick = { selectedSavingBoxId = null },
                        label = { Text("Nenhuma", fontSize = 12.sp) }
                    )
                    savingBoxes.forEach { box ->
                        FilterChip(
                            selected = selectedSavingBoxId == box.id,
                            onClick = { selectedSavingBoxId = box.id },
                            label = { Text(box.name, fontSize = 12.sp) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            val parsedAmount = CurrencyUtils.parseBrl(amountText) ?: 0.0
            Button(
                onClick = {
                    if (parsedAmount > 0 && title.isNotBlank()) {
                        val transaction = (initialTransaction ?: TransactionEntity(
                            title = title.trim(),
                            amount = parsedAmount,
                            type = if (type == TransactionType.INCOME) "INCOME" else "EXPENSE",
                            category = selectedCategory,
                            dateMillis = dateMillis
                        )).copy(
                            title = title.trim(),
                            amount = parsedAmount,
                            type = if (type == TransactionType.INCOME) "INCOME" else "EXPENSE",
                            category = selectedCategory,
                            dateMillis = dateMillis,
                            isPaid = isPaid,
                            dueDateMillis = if (!isPaid) (dueDateMillis ?: dateMillis) else null,
                            savingBoxId = selectedSavingBoxId,
                            notes = notes
                        )
                        onSave(transaction)
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_transaction_button"),
                shape = RoundedCornerShape(12.dp),
                enabled = title.isNotBlank() && parsedAmount > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (type == TransactionType.INCOME) Emerald500 else Carmine500
                )
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (initialTransaction == null) "Confirmar Lançamento" else "Salvar Alterações",
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Calculator Dialog
    if (showCalculator) {
        val currAmt = amountText.replace(',', '.').toDoubleOrNull() ?: 0.0
        CalculatorDialog(
            initialValue = currAmt,
            onDismiss = { showCalculator = false },
            onApplyResult = { calculated ->
                amountText = String.format(java.util.Locale.US, "%.2f", calculated)
            }
        )
    }

    // Native Date Pickers
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dateMillis = it }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDueDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDueDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dueDatePickerState.selectedDateMillis?.let { dueDateMillis = it }
                    showDueDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDueDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = dueDatePickerState)
        }
    }
}

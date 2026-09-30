package com.example.domain.model

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MonthSummary(
    val year: Int,
    val month: Int, // 0-indexed (0=Jan, 9=Out)
    val initialBalance: Double,
    val totalIncome: Double,
    val paidExpenses: Double,
    val pendingExpenses: Double,
    val estimatedFixedIncome: Double
) {
    // Current available balance: Initial + Incomes - Paid Expenses
    val currentBalance: Double
        get() = initialBalance + totalIncome - paidExpenses

    // Total expenses (paid + pending)
    val totalProjectedExpenses: Double
        get() = paidExpenses + pendingExpenses

    // Projected balance at end of month: Current balance - Pending Expenses
    val projectedBalance: Double
        get() = currentBalance - pendingExpenses

    // Savings rate (taxa de poupança): (Incomes - Paid Expenses) / Incomes * 100
    val savingsRate: Double
        get() = if (totalIncome > 0) {
            val net = totalIncome - (paidExpenses + pendingExpenses)
            ((net / totalIncome) * 100.0).coerceIn(-100.0, 100.0)
        } else 0.0

    val monthNamePtBr: String
        get() {
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val fmt = SimpleDateFormat("MMMM yyyy", Locale("pt", "BR"))
            return fmt.format(cal.time).replaceFirstChar { it.uppercase() }
        }
}

data class DayFinancialMarker(
    val dayOfMonth: Int,
    val dateMillis: Long,
    val incomeTotal: Double = 0.0,
    val paidExpenseTotal: Double = 0.0,
    val pendingExpenseTotal: Double = 0.0,
    val transactionCount: Int = 0
)

data class BudgetHealth(
    val score: Int, // 0 - 100
    val statusLabel: String,
    val message: String
)

object CurrencyUtils {
    private val brLocale = Locale("pt", "BR")
    private val numberFormat = NumberFormat.getCurrencyInstance(brLocale).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    fun formatBrl(amount: Double): String {
        return numberFormat.format(amount)
    }

    fun parseBrl(text: String): Double? {
        val clean = text.replace("R$", "")
            .replace(" ", "")
            .trim()
        if (clean.isEmpty()) return null
        if (clean.contains(".") && clean.contains(",")) {
            val normalized = clean.replace(".", "").replace(",", ".")
            return normalized.toDoubleOrNull()
        }
        if (clean.contains(",")) {
            val normalized = clean.replace(",", ".")
            return normalized.toDoubleOrNull()
        }
        return clean.toDoubleOrNull()
    }

    fun formatDatePtBr(millis: Long): String {
        val sdf = SimpleDateFormat("dd 'de' MMM, yyyy", brLocale)
        return sdf.format(Date(millis))
    }

    fun formatShortDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", brLocale)
        return sdf.format(Date(millis))
    }
}

package com.example

import com.example.domain.model.CurrencyUtils
import com.example.domain.model.MonthSummary
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testCurrencyFormattingBrl() {
        val formatted = CurrencyUtils.formatBrl(1250.50)
        assertTrue(formatted.contains("1.250,50") || formatted.contains("1250,50"))
    }

    @Test
    fun testMonthSummaryCalculations() {
        val summary = MonthSummary(
            year = 2026,
            month = 9,
            initialBalance = 3000.0,
            totalIncome = 5000.0,
            paidExpenses = 2000.0,
            pendingExpenses = 1000.0,
            estimatedFixedIncome = 5000.0
        )

        // Current balance = Initial (3000) + Incomes (5000) - Paid Expenses (2000) = 6000
        assertEquals(6000.0, summary.currentBalance, 0.001)

        // Projected balance = Current (6000) - Pending (1000) = 5000
        assertEquals(5000.0, summary.projectedBalance, 0.001)

        // Net savings = 5000 - 3000 = 2000; Rate = (2000 / 5000) * 100 = 40%
        assertEquals(40.0, summary.savingsRate, 0.001)
    }

    @Test
    fun testDeficitSavingsRate() {
        val summary = MonthSummary(
            year = 2026,
            month = 9,
            initialBalance = 1000.0,
            totalIncome = 2000.0,
            paidExpenses = 2500.0,
            pendingExpenses = 500.0,
            estimatedFixedIncome = 2000.0
        )
        // Net savings = 2000 - 3000 = -1000 -> -50%
        assertEquals(-50.0, summary.savingsRate, 0.001)
        assertEquals(500.0, summary.currentBalance, 0.001)
        assertEquals(0.0, summary.projectedBalance, 0.001)
    }
}


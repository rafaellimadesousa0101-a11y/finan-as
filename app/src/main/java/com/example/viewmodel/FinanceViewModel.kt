package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiFinancialService
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.local.SavingBoxEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.repository.FinanceRepository
import com.example.domain.model.BudgetHealth
import com.example.domain.model.CategoryDef
import com.example.domain.model.DayFinancialMarker
import com.example.domain.model.FinancialCategories
import com.example.domain.model.MonthSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class TransactionFilter(val labelPtBr: String) {
    ALL("Todos"),
    INCOMES("Receitas"),
    EXPENSES("Despesas"),
    PENDING("A Pagar")
}

data class CategoryExpenseStat(
    val category: CategoryDef,
    val totalAmount: Double,
    val percentage: Float
)

data class MonthComparisonStat(
    val monthLabel: String,
    val totalIncome: Double,
    val totalExpense: Double
)

data class RolloverInfo(
    val previousMonthName: String,
    val closingBalance: Double,
    val newMonthName: String
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val repository = FinanceRepository(database)
    private val geminiService = GeminiFinancialService()

    // Current selected calendar month & year
    private val _selectedCalendar = MutableStateFlow(Calendar.getInstance())
    val selectedCalendar: StateFlow<Calendar> = _selectedCalendar.asStateFlow()

    // Time travel mode offset (in months, 0 = real time)
    private val _timeTravelOffset = MutableStateFlow(0)
    val timeTravelOffset: StateFlow<Int> = _timeTravelOffset.asStateFlow()

    // Transaction filter
    private val _currentFilter = MutableStateFlow(TransactionFilter.ALL)
    val currentFilter: StateFlow<TransactionFilter> = _currentFilter.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // AI thinking state
    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    // Rollover modal trigger
    private val _rolloverEvent = MutableStateFlow<RolloverInfo?>(null)
    val rolloverEvent: StateFlow<RolloverInfo?> = _rolloverEvent.asStateFlow()

    // All data flows from repository
    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSavingBoxes: StateFlow<List<SavingBoxEntity>> = repository.allSavingBoxes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userSettings: StateFlow<UserSettingsEntity?> = repository.userSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current Month Summary calculation
    val currentMonthSummary: StateFlow<MonthSummary> = combine(
        allTransactions,
        userSettings,
        selectedCalendar,
        timeTravelOffset
    ) { transactions, settings, cal, offset ->
        val targetCal = (cal.clone() as Calendar).apply {
            add(Calendar.MONTH, offset)
        }
        val targetYear = targetCal.get(Calendar.YEAR)
        val targetMonth = targetCal.get(Calendar.MONTH)

        val monthTransactions = transactions.filter { t ->
            val tCal = Calendar.getInstance().apply { timeInMillis = t.dateMillis }
            tCal.get(Calendar.YEAR) == targetYear && tCal.get(Calendar.MONTH) == targetMonth
        }

        var totalInc = 0.0
        var paidExp = 0.0
        var pendExp = 0.0

        for (t in monthTransactions) {
            if (t.type == "INCOME") {
                totalInc += t.amount
            } else {
                if (t.isPaid) {
                    paidExp += t.amount
                } else {
                    pendExp += t.amount
                }
            }
        }

        val initialBal = settings?.initialBalance ?: 3000.0
        val fixedInc = settings?.estimatedFixedIncome ?: 5000.0

        // In time travel simulation, apply compound months simulation
        val simulatedInitial = if (offset > 0) {
            initialBal + (offset * (fixedInc - (paidExp + pendExp).coerceAtLeast(2000.0)))
        } else {
            initialBal
        }

        MonthSummary(
            year = targetYear,
            month = targetMonth,
            initialBalance = simulatedInitial,
            totalIncome = totalInc,
            paidExpenses = paidExp,
            pendingExpenses = pendExp,
            estimatedFixedIncome = fixedInc
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MonthSummary(
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            3000.0, 0.0, 0.0, 0.0, 5000.0
        )
    )

    // Filtered transactions for the current month and search query
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        allTransactions,
        selectedCalendar,
        timeTravelOffset,
        currentFilter,
        searchQuery
    ) { transactions, cal, offset, filter, query ->
        val targetCal = (cal.clone() as Calendar).apply { add(Calendar.MONTH, offset) }
        val targetYear = targetCal.get(Calendar.YEAR)
        val targetMonth = targetCal.get(Calendar.MONTH)

        transactions.filter { t ->
            val tCal = Calendar.getInstance().apply { timeInMillis = t.dateMillis }
            val inMonth = tCal.get(Calendar.YEAR) == targetYear && tCal.get(Calendar.MONTH) == targetMonth
            if (!inMonth) return@filter false

            val matchesFilter = when (filter) {
                TransactionFilter.ALL -> true
                TransactionFilter.INCOMES -> t.type == "INCOME"
                TransactionFilter.EXPENSES -> t.type == "EXPENSE"
                TransactionFilter.PENDING -> t.type == "EXPENSE" && !t.isPaid
            }
            if (!matchesFilter) return@filter false

            if (query.isNotBlank()) {
                t.title.contains(query, ignoreCase = true) ||
                        t.category.contains(query, ignoreCase = true) ||
                        t.notes.contains(query, ignoreCase = true)
            } else true
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Category stats for pie chart
    val categoryStats: StateFlow<List<CategoryExpenseStat>> = combine(
        allTransactions,
        selectedCalendar,
        timeTravelOffset
    ) { transactions, cal, offset ->
        val targetCal = (cal.clone() as Calendar).apply { add(Calendar.MONTH, offset) }
        val targetYear = targetCal.get(Calendar.YEAR)
        val targetMonth = targetCal.get(Calendar.MONTH)

        val expenses = transactions.filter { t ->
            val tCal = Calendar.getInstance().apply { timeInMillis = t.dateMillis }
            tCal.get(Calendar.YEAR) == targetYear && tCal.get(Calendar.MONTH) == targetMonth && t.type == "EXPENSE"
        }

        val totalExpenses = expenses.sumOf { it.amount }
        if (totalExpenses == 0.0) return@combine emptyList()

        val grouped = expenses.groupBy { it.category }
        grouped.map { (catName, list) ->
            val catTotal = list.sumOf { it.amount }
            CategoryExpenseStat(
                category = FinancialCategories.getByName(catName),
                totalAmount = catTotal,
                percentage = ((catTotal / totalExpenses) * 100.0).toFloat()
            )
        }.sortedByDescending { it.totalAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Month-over-month comparison (last 6 months)
    val monthlyComparison: StateFlow<List<MonthComparisonStat>> = allTransactions.combine(selectedCalendar) { transactions, currentCal ->
        val result = mutableListOf<MonthComparisonStat>()
        val fmt = SimpleDateFormat("MMM", Locale("pt", "BR"))

        for (i in 5 downTo 0) {
            val c = (currentCal.clone() as Calendar).apply { add(Calendar.MONTH, -i) }
            val y = c.get(Calendar.YEAR)
            val m = c.get(Calendar.MONTH)
            val label = fmt.format(c.time).replaceFirstChar { it.uppercase() }

            val inMonth = transactions.filter { t ->
                val tc = Calendar.getInstance().apply { timeInMillis = t.dateMillis }
                tc.get(Calendar.YEAR) == y && tc.get(Calendar.MONTH) == m
            }

            val inc = inMonth.filter { it.type == "INCOME" }.sumOf { it.amount }
            val exp = inMonth.filter { it.type == "EXPENSE" }.sumOf { it.amount }

            result.add(MonthComparisonStat(label, inc, exp))
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calendar daily markers
    val dailyMarkers: StateFlow<Map<Int, DayFinancialMarker>> = combine(
        allTransactions,
        selectedCalendar,
        timeTravelOffset
    ) { transactions, cal, offset ->
        val targetCal = (cal.clone() as Calendar).apply { add(Calendar.MONTH, offset) }
        val targetYear = targetCal.get(Calendar.YEAR)
        val targetMonth = targetCal.get(Calendar.MONTH)

        val markers = mutableMapOf<Int, DayFinancialMarker>()
        val inMonth = transactions.filter { t ->
            val tc = Calendar.getInstance().apply { timeInMillis = t.dateMillis }
            tc.get(Calendar.YEAR) == targetYear && tc.get(Calendar.MONTH) == targetMonth
        }

        for (t in inMonth) {
            val tc = Calendar.getInstance().apply { timeInMillis = t.dateMillis }
            val day = tc.get(Calendar.DAY_OF_MONTH)
            val existing = markers[day] ?: DayFinancialMarker(day, t.dateMillis)

            val newInc = if (t.type == "INCOME") existing.incomeTotal + t.amount else existing.incomeTotal
            val newPaid = if (t.type == "EXPENSE" && t.isPaid) existing.paidExpenseTotal + t.amount else existing.paidExpenseTotal
            val newPend = if (t.type == "EXPENSE" && !t.isPaid) existing.pendingExpenseTotal + t.amount else existing.pendingExpenseTotal

            markers[day] = existing.copy(
                incomeTotal = newInc,
                paidExpenseTotal = newPaid,
                pendingExpenseTotal = newPend,
                transactionCount = existing.transactionCount + 1
            )
        }
        markers
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Budget Health Score calculation
    val budgetHealth: StateFlow<BudgetHealth> = currentMonthSummary.combine(categoryStats) { summary, stats ->
        val savingsRate = summary.savingsRate
        var score = 70

        if (savingsRate >= 20) score += 20
        else if (savingsRate >= 10) score += 10
        else if (savingsRate < 0) score -= 30

        if (summary.pendingExpenses > summary.currentBalance && summary.currentBalance > 0) {
            score -= 15
        } else if (summary.pendingExpenses == 0.0) {
            score += 10
        }

        val clamped = score.coerceIn(10, 100)
        val (label, msg) = when {
            clamped >= 85 -> "Excelente" to "Suas finanças estão sob controle total e sua taxa de economia supera a meta recomendada."
            clamped >= 70 -> "Bom" to "Fluxo de caixa saudável. Fique atento às contas a pagar nos próximos dias."
            clamped >= 50 -> "Atenção" to "Gastos próximos do limite das receitas. Recomenda-se evitar novas compras a prazo."
            else -> "Crítico" to "Despesas projetadas superam a renda do mês. Ative o Assistente IA para plano de corte de gastos."
        }
        BudgetHealth(clamped, label, msg)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BudgetHealth(75, "Bom", "Fluxo de caixa estável."))

    init {
        checkMonthRollover()
    }

    private fun checkMonthRollover() {
        viewModelScope.launch {
            val settings = repository.getSettingsDirect() ?: return@launch
            val now = Calendar.getInstance()
            val currentKey = "${now.get(Calendar.YEAR)}-${now.get(Calendar.MONTH) + 1}"

            if (settings.lastRolloverMonth.isNotBlank() && settings.lastRolloverMonth != currentKey) {
                val prevMonthCal = (now.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                val fmt = SimpleDateFormat("MMMM yyyy", Locale("pt", "BR"))
                val prevName = fmt.format(prevMonthCal.time)
                val currName = fmt.format(now.time)

                _rolloverEvent.value = RolloverInfo(
                    previousMonthName = prevName.replaceFirstChar { it.uppercase() },
                    closingBalance = settings.initialBalance,
                    newMonthName = currName.replaceFirstChar { it.uppercase() }
                )
            }
            repository.updateLastRolloverMonth(currentKey)
        }
    }

    fun dismissRolloverEvent() {
        _rolloverEvent.value = null
    }

    // Month Navigation
    fun previousMonth() {
        val c = (_selectedCalendar.value.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
        _selectedCalendar.value = c
    }

    fun nextMonth() {
        val c = (_selectedCalendar.value.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
        _selectedCalendar.value = c
    }

    fun setCalendar(calendar: Calendar) {
        _selectedCalendar.value = calendar
    }

    fun resetToCurrentMonth() {
        _selectedCalendar.value = Calendar.getInstance()
        _timeTravelOffset.value = 0
    }

    // Time Travel Mode
    fun setTimeTravelOffset(months: Int) {
        _timeTravelOffset.value = months
    }

    // Filters & Search
    fun setFilter(filter: TransactionFilter) {
        _currentFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Transaction actions
    fun addTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.addTransaction(transaction)
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    fun markAsPaid(id: Long) {
        viewModelScope.launch {
            repository.markAsPaid(id)
        }
    }

    // Saving box operations
    fun createSavingBox(
        name: String,
        category: String,
        targetAmount: Double,
        targetDateMillis: Long?,
        colorHex: String,
        iconName: String
    ) {
        viewModelScope.launch {
            repository.createSavingBox(
                SavingBoxEntity(
                    name = name,
                    category = category,
                    targetAmount = targetAmount,
                    targetDateMillis = targetDateMillis,
                    colorHex = colorHex,
                    iconName = iconName
                )
            )
        }
    }

    fun depositToSavingBox(box: SavingBoxEntity, amount: Double) {
        viewModelScope.launch {
            repository.depositToSavingBox(box.id, box.name, amount)
        }
    }

    fun withdrawFromSavingBox(box: SavingBoxEntity, amount: Double) {
        viewModelScope.launch {
            repository.withdrawFromSavingBox(box.id, box.name, amount)
        }
    }

    fun toggleArchiveSavingBox(box: SavingBoxEntity) {
        viewModelScope.launch {
            repository.toggleArchiveSavingBox(box.id, box.isArchived)
        }
    }

    fun deleteSavingBox(box: SavingBoxEntity) {
        viewModelScope.launch {
            repository.deleteSavingBox(box)
        }
    }

    // AI Assistant actions
    fun sendChatMessage(prompt: String) {
        viewModelScope.launch {
            _isAiThinking.value = true
            val summary = currentMonthSummary.value
            val topCats = categoryStats.value.map { it.category.namePtBr to it.totalAmount }
            val pendCount = filteredTransactions.value.count { it.type == "EXPENSE" && !it.isPaid }

            val response = geminiService.generateFinancialAdvice(
                userPrompt = prompt,
                monthSummary = summary,
                topCategories = topCats,
                pendingBillsCount = pendCount
            )
            repository.sendChatMessage(prompt, response)
            _isAiThinking.value = false
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }

    // Settings actions
    fun updateInitialBalance(amount: Double) {
        viewModelScope.launch {
            repository.updateInitialBalance(amount)
        }
    }

    fun updateEstimatedFixedIncome(amount: Double) {
        viewModelScope.launch {
            repository.updateEstimatedFixedIncome(amount)
        }
    }

    fun updateProfile(name: String, avatarUri: String?) {
        viewModelScope.launch {
            repository.updateProfile(name, avatarUri)
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetAllData()
        }
    }
}

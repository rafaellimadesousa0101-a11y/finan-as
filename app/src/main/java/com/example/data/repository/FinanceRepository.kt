package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.local.SavingBoxEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserSettingsEntity
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class FinanceRepository(private val database: AppDatabase) {
    private val transactionDao = database.transactionDao()
    private val savingBoxDao = database.savingBoxDao()
    private val chatDao = database.chatDao()
    private val userSettingsDao = database.userSettingsDao()

    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allSavingBoxes: Flow<List<SavingBoxEntity>> = savingBoxDao.getAllSavingBoxes()
    val chatMessages: Flow<List<ChatMessageEntity>> = chatDao.getAllMessages()
    val userSettings: Flow<UserSettingsEntity?> = userSettingsDao.getSettings()

    fun getTransactionsForRange(startMillis: Long, endMillis: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsForRange(startMillis, endMillis)
    }

    suspend fun addTransaction(transaction: TransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(id: Long) {
        transactionDao.deleteById(id)
    }

    suspend fun markAsPaid(id: Long) {
        transactionDao.markAsPaid(id)
    }

    // Saving box operations
    suspend fun createSavingBox(box: SavingBoxEntity): Long {
        return savingBoxDao.insertSavingBox(box)
    }

    suspend fun updateSavingBox(box: SavingBoxEntity) {
        savingBoxDao.updateSavingBox(box)
    }

    suspend fun deleteSavingBox(box: SavingBoxEntity) {
        savingBoxDao.deleteSavingBox(box)
    }

    suspend fun toggleArchiveSavingBox(boxId: Long, currentArchived: Boolean) {
        savingBoxDao.setArchived(boxId, !currentArchived)
    }

    // Guardar Dinheiro: Deducts from balance and adds to box
    suspend fun depositToSavingBox(boxId: Long, boxName: String, amount: Double) {
        savingBoxDao.updateAmount(boxId, amount)
        // Record as an expense/transfer
        transactionDao.insertTransaction(
            TransactionEntity(
                title = "Aplicação Caixinha: $boxName",
                amount = amount,
                type = "EXPENSE",
                category = "Investimentos",
                dateMillis = System.currentTimeMillis(),
                isPaid = true,
                savingBoxId = boxId,
                notes = "Depósito na meta $boxName"
            )
        )
    }

    // Resgatar Dinheiro: Deducts from box and adds to balance
    suspend fun withdrawFromSavingBox(boxId: Long, boxName: String, amount: Double) {
        savingBoxDao.updateAmount(boxId, -amount)
        // Record as an income/transfer
        transactionDao.insertTransaction(
            TransactionEntity(
                title = "Resgate Caixinha: $boxName",
                amount = amount,
                type = "INCOME",
                category = "Investimentos",
                dateMillis = System.currentTimeMillis(),
                isPaid = true,
                savingBoxId = boxId,
                notes = "Resgate da meta $boxName"
            )
        )
    }

    // Chat operations
    suspend fun sendChatMessage(userText: String, aiResponse: String) {
        chatDao.insertMessage(
            ChatMessageEntity(
                isUser = true,
                text = userText,
                timestampMillis = System.currentTimeMillis()
            )
        )
        chatDao.insertMessage(
            ChatMessageEntity(
                isUser = false,
                text = aiResponse,
                timestampMillis = System.currentTimeMillis() + 100
            )
        )
    }

    suspend fun clearChat() {
        chatDao.clearChat()
    }

    // User settings operations
    suspend fun updateInitialBalance(balance: Double) {
        userSettingsDao.updateInitialBalance(balance)
    }

    suspend fun updateEstimatedFixedIncome(income: Double) {
        userSettingsDao.updateEstimatedFixedIncome(income)
    }

    suspend fun updateProfile(name: String, avatarUri: String?) {
        userSettingsDao.updateProfile(name, avatarUri)
    }

    suspend fun updateLastRolloverMonth(monthKey: String) {
        userSettingsDao.updateLastRolloverMonth(monthKey)
    }

    suspend fun getSettingsDirect(): UserSettingsEntity? {
        return userSettingsDao.getSettingsDirect()
    }

    // Reset database with fresh initial data
    suspend fun resetAllData() {
        transactionDao.clearAll()
        savingBoxDao.clearAll()
        chatDao.clearChat()
        AppDatabase.populateInitialData(database)
    }

    // Backup Export to JSON
    suspend fun exportDataToJson(): String {
        val root = JSONObject()
        val settings = userSettingsDao.getSettingsDirect() ?: UserSettingsEntity()
        val sJson = JSONObject().apply {
            put("userName", settings.userName)
            put("initialBalance", settings.initialBalance)
            put("estimatedFixedIncome", settings.estimatedFixedIncome)
        }
        root.put("settings", sJson)

        // Transactions
        val tArray = JSONArray()
        // Collect current transactions once
        // For backup export, query all
        // (Room allows getAllTransactions returning Flow, or direct query)
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())
        return root.toString(2)
    }

    // Backup Import from JSON
    suspend fun importDataFromJson(jsonStr: String): Boolean {
        return try {
            val root = JSONObject(jsonStr)
            if (root.has("settings")) {
                val s = root.getJSONObject("settings")
                val name = s.optString("userName", "Rafael")
                val initBal = s.optDouble("initialBalance", 3000.0)
                val fixedInc = s.optDouble("estimatedFixedIncome", 6000.0)
                userSettingsDao.insertOrUpdate(
                    UserSettingsEntity(
                        id = 1,
                        userName = name,
                        initialBalance = initBal,
                        estimatedFixedIncome = fixedInc
                    )
                )
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}

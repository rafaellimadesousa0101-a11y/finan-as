package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [
        TransactionEntity::class,
        SavingBoxEntity::class,
        ChatMessageEntity::class,
        UserSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun savingBoxDao(): SavingBoxDao
    abstract fun chatDao(): ChatDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "min_financas_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                scope.launch(Dispatchers.IO) {
                    try {
                        if (instance.userSettingsDao().getSettingsDirect() == null) {
                            populateInitialData(instance)
                        }
                    } catch (e: Exception) {
                        // Handled
                    }
                }
                instance
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val userSettingsDao = database.userSettingsDao()
            val transactionDao = database.transactionDao()
            val savingBoxDao = database.savingBoxDao()
            val chatDao = database.chatDao()

            userSettingsDao.insertOrUpdate(
                UserSettingsEntity(
                    id = 1,
                    userName = "Rafael",
                    initialBalance = 3200.0,
                    estimatedFixedIncome = 6500.0,
                    lastRolloverMonth = ""
                )
            )

            // Initial Saving Boxes (Caixinhas)
            val cal = Calendar.getInstance()
            val travelCal = Calendar.getInstance().apply { add(Calendar.MONTH, 6) }
            val emergencyBoxId = savingBoxDao.insertSavingBox(
                SavingBoxEntity(
                    name = "Reserva de Emergência",
                    category = "Investimentos",
                    targetAmount = 15000.0,
                    currentAmount = 5200.0,
                    targetDateMillis = travelCal.timeInMillis,
                    colorHex = "#10B981",
                    iconName = "Shield"
                )
            )

            val travelBoxId = savingBoxDao.insertSavingBox(
                SavingBoxEntity(
                    name = "Viagem de Férias",
                    category = "Lazer",
                    targetAmount = 4000.0,
                    currentAmount = 1800.0,
                    targetDateMillis = travelCal.timeInMillis,
                    colorHex = "#6366F1",
                    iconName = "Flight"
                )
            )

            val techBoxId = savingBoxDao.insertSavingBox(
                SavingBoxEntity(
                    name = "Novo Notebook",
                    category = "Educação",
                    targetAmount = 5000.0,
                    currentAmount = 2500.0,
                    targetDateMillis = travelCal.timeInMillis,
                    colorHex = "#F59E0B",
                    iconName = "Laptop"
                )
            )

            // Initial Transactions for the current month
            val now = Calendar.getInstance()
            
            // Salary
            val cal1 = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 5) }
            transactionDao.insertTransaction(
                TransactionEntity(
                    title = "Salário Mensal",
                    amount = 6500.0,
                    type = "INCOME",
                    category = "Salário",
                    dateMillis = cal1.timeInMillis,
                    isPaid = true
                )
            )

            // Freelance
            val cal2 = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 12) }
            transactionDao.insertTransaction(
                TransactionEntity(
                    title = "Consultoria de Design",
                    amount = 1200.0,
                    type = "INCOME",
                    category = "Freelance",
                    dateMillis = cal2.timeInMillis,
                    isPaid = true
                )
            )

            // Rent (Aluguel)
            val cal3 = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 10) }
            transactionDao.insertTransaction(
                TransactionEntity(
                    title = "Aluguel & Condomínio",
                    amount = 1850.0,
                    type = "EXPENSE",
                    category = "Moradia",
                    dateMillis = cal3.timeInMillis,
                    isPaid = true
                )
            )

            // Supermercado
            val cal4 = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 14) }
            transactionDao.insertTransaction(
                TransactionEntity(
                    title = "Supermercado Mensal",
                    amount = 680.40,
                    type = "EXPENSE",
                    category = "Alimentação",
                    dateMillis = cal4.timeInMillis,
                    isPaid = true
                )
            )

            // Internet & Energia
            val cal5 = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 18) }
            transactionDao.insertTransaction(
                TransactionEntity(
                    title = "Internet Fibra e Luz",
                    amount = 245.0,
                    type = "EXPENSE",
                    category = "Serviços",
                    dateMillis = cal5.timeInMillis,
                    isPaid = true
                )
            )

            // Pending bill (Conta de luz futura ou cartão)
            val cal6 = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 28) }
            transactionDao.insertTransaction(
                TransactionEntity(
                    title = "Fatura do Cartão de Crédito",
                    amount = 890.0,
                    type = "EXPENSE",
                    category = "Compras",
                    dateMillis = cal6.timeInMillis,
                    isPaid = false,
                    dueDateMillis = cal6.timeInMillis,
                    notes = "Vencimento dia 28"
                )
            )

            // Pending gym
            val cal7 = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 26) }
            transactionDao.insertTransaction(
                TransactionEntity(
                    title = "Mensalidade Academia",
                    amount = 120.0,
                    type = "EXPENSE",
                    category = "Saúde",
                    dateMillis = cal7.timeInMillis,
                    isPaid = false,
                    dueDateMillis = cal7.timeInMillis
                )
            )

            // Transport
            val cal8 = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 8) }
            transactionDao.insertTransaction(
                TransactionEntity(
                    title = "Combustível / Metrô",
                    amount = 210.0,
                    type = "EXPENSE",
                    category = "Transporte",
                    dateMillis = cal8.timeInMillis,
                    isPaid = true
                )
            )

            // Leisure
            val cal9 = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 15) }
            transactionDao.insertTransaction(
                TransactionEntity(
                    title = "Jantar com amigos",
                    amount = 165.0,
                    type = "EXPENSE",
                    category = "Lazer",
                    dateMillis = cal9.timeInMillis,
                    isPaid = true
                )
            )

            // Initial AI greeting
            chatDao.insertMessage(
                ChatMessageEntity(
                    isUser = false,
                    text = "Olá, Rafael! Sou seu assistente financeiro inteligente do Min Finanças. Analisei seu saldo atual e gastos do mês: sua taxa de poupança está saudável e você tem 2 contas pendentes até o fim do mês. Como posso te ajudar hoje?"
                )
            )
        }
    }
}

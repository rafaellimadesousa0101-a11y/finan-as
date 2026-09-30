package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // "INCOME" or "EXPENSE"
    val category: String,
    val dateMillis: Long,
    val isPaid: Boolean = true,
    val dueDateMillis: Long? = null,
    val savingBoxId: Long? = null,
    val notes: String = ""
)

@Entity(tableName = "saving_boxes")
data class SavingBoxEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    val targetAmount: Double = 0.0,
    val currentAmount: Double = 0.0,
    val targetDateMillis: Long? = null,
    val colorHex: String = "#6366F1",
    val iconName: String = "Savings",
    val isArchived: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val isUser: Boolean,
    val text: String,
    val timestampMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val userName: String = "Rafael",
    val avatarUri: String? = null,
    val initialBalance: Double = 2500.0,
    val estimatedFixedIncome: Double = 5000.0,
    val lastRolloverMonth: String = "" // "YYYY-MM"
)

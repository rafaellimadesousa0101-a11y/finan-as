package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<UserSettingsEntity?>

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): UserSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: UserSettingsEntity)

    @Query("UPDATE user_settings SET initialBalance = :balance WHERE id = 1")
    suspend fun updateInitialBalance(balance: Double)

    @Query("UPDATE user_settings SET estimatedFixedIncome = :income WHERE id = 1")
    suspend fun updateEstimatedFixedIncome(income: Double)

    @Query("UPDATE user_settings SET userName = :name, avatarUri = :avatarUri WHERE id = 1")
    suspend fun updateProfile(name: String, avatarUri: String?)

    @Query("UPDATE user_settings SET lastRolloverMonth = :month WHERE id = 1")
    suspend fun updateLastRolloverMonth(month: String)
}

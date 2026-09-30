package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingBoxDao {
    @Query("SELECT * FROM saving_boxes ORDER BY isArchived ASC, createdAtMillis DESC")
    fun getAllSavingBoxes(): Flow<List<SavingBoxEntity>>

    @Query("SELECT * FROM saving_boxes WHERE id = :id LIMIT 1")
    suspend fun getSavingBoxById(id: Long): SavingBoxEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingBox(box: SavingBoxEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(boxes: List<SavingBoxEntity>)

    @Update
    suspend fun updateSavingBox(box: SavingBoxEntity)

    @Delete
    suspend fun deleteSavingBox(box: SavingBoxEntity)

    @Query("UPDATE saving_boxes SET currentAmount = currentAmount + :delta WHERE id = :id")
    suspend fun updateAmount(id: Long, delta: Double)

    @Query("UPDATE saving_boxes SET isArchived = :isArchived WHERE id = :id")
    suspend fun setArchived(id: Long, isArchived: Boolean)

    @Query("DELETE FROM saving_boxes")
    suspend fun clearAll()
}

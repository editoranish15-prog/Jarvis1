package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.MemoryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memory_items ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<MemoryItemEntity>>

    @Query("SELECT * FROM memory_items")
    suspend fun getAllMemoriesList(): List<MemoryItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryItemEntity): Long

    @Update
    suspend fun updateMemory(memory: MemoryItemEntity)

    @Delete
    suspend fun deleteMemory(memory: MemoryItemEntity)

    @Query("DELETE FROM memory_items")
    suspend fun clearAllMemories()
}

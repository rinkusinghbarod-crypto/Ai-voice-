package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.NarrationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NarrationDao {
    @Query("SELECT * FROM narrations ORDER BY timestamp DESC")
    fun getAllNarrations(): Flow<List<NarrationEntity>>

    @Query("SELECT * FROM narrations WHERE id = :id LIMIT 1")
    fun getNarrationById(id: Long): Flow<NarrationEntity?>

    @Query("SELECT * FROM narrations WHERE id = :id LIMIT 1")
    suspend fun getNarrationByIdOnce(id: Long): NarrationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNarration(narration: NarrationEntity): Long

    @Update
    suspend fun updateNarration(narration: NarrationEntity)

    @Query("DELETE FROM narrations WHERE id = :id")
    suspend fun deleteNarrationById(id: Long)

    @Query("DELETE FROM narrations WHERE id IN (:ids)")
    suspend fun deleteNarrationsByIds(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM narrations")
    fun getCount(): Flow<Int>
}

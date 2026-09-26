package com.example.data.repository

import com.example.data.dao.NarrationDao
import com.example.data.model.NarrationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class NarrationRepository(private val dao: NarrationDao) {
    val allNarrations: Flow<List<NarrationEntity>> = dao.getAllNarrations()
    val totalCount: Flow<Int> = dao.getCount()

    fun getNarration(id: Long): Flow<NarrationEntity?> = dao.getNarrationById(id)

    suspend fun getNarrationOnce(id: Long): NarrationEntity? = withContext(Dispatchers.IO) {
        dao.getNarrationByIdOnce(id)
    }

    suspend fun saveNarration(narration: NarrationEntity): Long = withContext(Dispatchers.IO) {
        dao.insertNarration(narration)
    }

    suspend fun updateNarration(narration: NarrationEntity) = withContext(Dispatchers.IO) {
        dao.updateNarration(narration)
    }

    suspend fun deleteNarration(narration: NarrationEntity) = withContext(Dispatchers.IO) {
        // Also remove cached audio file if it exists
        try {
            val file = File(narration.audioFilePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (_: Exception) {
        }
        dao.deleteNarrationById(narration.id)
    }

    suspend fun deleteNarrations(narrations: List<NarrationEntity>) = withContext(Dispatchers.IO) {
        if (narrations.isEmpty()) return@withContext
        for (narration in narrations) {
            try {
                val file = File(narration.audioFilePath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (_: Exception) {
            }
        }
        dao.deleteNarrationsByIds(narrations.map { it.id })
    }
}

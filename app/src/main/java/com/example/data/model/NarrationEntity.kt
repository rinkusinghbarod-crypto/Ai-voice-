package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "narrations")
data class NarrationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val script: String,
    val audioFilePath: String,
    val durationMs: Long = 0,
    val voiceName: String = "Old American Man",
    val voiceStyle: String = "Frontier Storyteller",
    val language: String = "English (US)",
    val speed: Float = 1.0f,
    val pitch: String = "Normal",
    val emotion: String = "Calm",
    val pauseControl: String = "Natural",
    val intensity: String = "Balanced",
    val timestamp: Long = System.currentTimeMillis(),
    val fileSizeBytes: Long = 0
)

package com.example

import android.app.Application
import com.example.audio.AudioPlayer
import com.example.data.db.AppDatabase
import com.example.data.repository.NarrationRepository
import com.example.data.settings.AppSettings
import com.example.tts.TtsManager

class OldStoryVoiceApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: NarrationRepository
        private set

    lateinit var settings: AppSettings
        private set

    lateinit var ttsManager: TtsManager
        private set

    lateinit var audioPlayer: AudioPlayer
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        repository = NarrationRepository(database.narrationDao())
        settings = AppSettings(this)
        ttsManager = TtsManager(this, settings)
        audioPlayer = AudioPlayer(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        audioPlayer.release()
    }
}

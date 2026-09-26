package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.util.Log
import com.example.tts.util.WavHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream

class AudioPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var currentFis: FileInputStream? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _currentFilePath = MutableStateFlow<String?>(null)
    val currentFilePath: StateFlow<String?> = _currentFilePath.asStateFlow()

    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted.asStateFlow()

    fun loadAudio(filePath: String, autoPlay: Boolean = true) {
        val file = File(filePath)
        if (!file.exists() || file.length() == 0L) {
            Log.e("AudioPlayer", "File does not exist or is empty: $filePath")
            return
        }

        // Verify and ensure audio container validity (WAV header on raw PCM)
        WavHelper.ensureValidAudioFile(file)

        stopAndReset()
        _currentFilePath.value = filePath
        _isCompleted.value = false

        try {
            val fis = FileInputStream(file)
            currentFis = fis

            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                // Use FileDescriptor to avoid permission issues with private sandbox files in native mediaserver
                setDataSource(fis.fd, 0, file.length())

                setOnPreparedListener { mp ->
                    _durationMs.value = mp.duration.toLong().coerceAtLeast(0L)
                    applyPlaybackSpeed(_playbackSpeed.value)
                    if (autoPlay) {
                        mp.start()
                        _isPlaying.value = true
                        startProgressTracker()
                    }
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    _isCompleted.value = true
                    _currentPositionMs.value = _durationMs.value
                    stopProgressTracker()
                }
                setOnErrorListener { mp, what, extra ->
                    Log.e("AudioPlayer", "MediaPlayer error: what=$what, extra=$extra")
                    _isPlaying.value = false
                    stopProgressTracker()
                    try {
                        mp.reset()
                    } catch (_: Exception) {
                    }
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Failed to load audio for $filePath", e)
            try {
                currentFis?.close()
            } catch (_: Exception) {
            }
            currentFis = null
            _isPlaying.value = false
        }
    }

    fun play() {
        val player = mediaPlayer ?: return
        if (!player.isPlaying) {
            if (_isCompleted.value) {
                player.seekTo(0)
                _isCompleted.value = false
            }
            player.start()
            _isPlaying.value = true
            startProgressTracker()
        }
    }

    fun pause() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _isPlaying.value = false
            stopProgressTracker()
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    fun seekTo(positionMs: Long) {
        val player = mediaPlayer ?: return
        val target = positionMs.coerceIn(0L, _durationMs.value)
        player.seekTo(target.toInt())
        _currentPositionMs.value = target
        if (_isCompleted.value && target < _durationMs.value) {
            _isCompleted.value = false
        }
    }

    fun replay() {
        seekTo(0)
        play()
    }

    fun skipForward(seconds: Int = 10) {
        val target = _currentPositionMs.value + (seconds * 1000L)
        seekTo(target)
    }

    fun skipBackward(seconds: Int = 10) {
        val target = _currentPositionMs.value - (seconds * 1000L)
        seekTo(target)
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        applyPlaybackSpeed(speed)
    }

    private fun applyPlaybackSpeed(speed: Float) {
        val player = mediaPlayer ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val params = player.playbackParams ?: PlaybackParams()
                params.speed = speed
                player.playbackParams = params
            }
        } catch (_: Exception) {
        }
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressJob = scope.launch {
            while (isActive) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        _currentPositionMs.value = player.currentPosition.toLong()
                    }
                }
                delay(100)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun stopAndReset() {
        stopProgressTracker()
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.reset()
                it.release()
            }
        } catch (_: Exception) {
        }
        try {
            currentFis?.close()
        } catch (_: Exception) {
        }
        currentFis = null
        mediaPlayer = null
        _isPlaying.value = false
        _currentPositionMs.value = 0L
    }

    fun release() {
        stopAndReset()
    }
}

package com.example.tts

import android.util.Base64
import com.example.tts.model.GeneratedAudioResult
import com.example.tts.model.TtsException
import com.example.tts.model.VoiceGenerationParams
import com.example.tts.util.AudioChunker
import com.example.tts.util.WavHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.UUID
import java.util.concurrent.TimeUnit

class CustomBackendTtsEngine(
    private val backendUrlProvider: () -> String,
    private val authHeaderProvider: () -> String
) : TtsEngine {

    override val id: String = "custom_backend"
    override val displayName: String = "Secure Backend Proxy"
    override val description: String = "Delegates generation to your private server backend to protect API keys"

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    override suspend fun generate(
        params: VoiceGenerationParams,
        outputDir: File,
        onProgress: (progress: Float, status: String) -> Unit
    ): GeneratedAudioResult = withContext(Dispatchers.IO) {
        val url = backendUrlProvider().trim()
        if (url.isEmpty()) {
            throw TtsException.ProviderNotConfiguredException(
                "Secure Backend URL is not configured. Please enter your backend endpoint in Settings."
            )
        }

        if (params.script.isBlank()) {
            throw TtsException.EmptyScriptException()
        }

        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        onProgress(0.1f, "Connecting to secure voice backend...")

        val requestPayload = JSONObject().apply {
            put("script", params.script)
            put("title", params.title)
            put("voice", params.voiceProfile)
            put("subStyle", params.subStyle)
            put("speed", params.speed)
            put("pitch", params.pitch)
            put("emotion", params.emotion)
            put("pauseControl", params.pauseControl)
            put("intensity", params.intensity)
            put("language", params.language)
        }

        val requestBody = requestPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val reqBuilder = Request.Builder().url(url).post(requestBody)

        val auth = authHeaderProvider().trim()
        if (auth.isNotEmpty()) {
            reqBuilder.addHeader("Authorization", if (auth.startsWith("Bearer ")) auth else "Bearer $auth")
        }

        val finalFileName = "narration_backend_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.wav"
        val outputFile = File(outputDir, finalFileName)

        try {
            onProgress(0.4f, "Backend generating Old American Man narration...")
            val response = httpClient.newCall(reqBuilder.build()).execute()

            if (!response.isSuccessful) {
                val errorBody = response.body?.string().orEmpty()
                throw TtsException.SynthesisException("Backend returned error ${response.code}: $errorBody")
            }

            val contentType = response.header("Content-Type", "") ?: ""
            val body = response.body ?: throw TtsException.SynthesisException("Backend returned empty response")

            if (contentType.contains("application/json")) {
                val jsonString = body.string()
                val json = JSONObject(jsonString)
                val base64 = json.optString("audio")
                    .ifEmpty { json.optString("audioData") }
                    .ifEmpty { json.optString("data") }

                if (base64.isBlank()) {
                    throw TtsException.SynthesisException("No audio field found in backend JSON response.")
                }
                val bytes = Base64.decode(base64, Base64.DEFAULT)
                FileOutputStream(outputFile).use { it.write(bytes) }
            } else {
                // Direct binary audio stream (WAV or MP3)
                FileOutputStream(outputFile).use { out ->
                    body.byteStream().use { input ->
                        input.copyTo(out)
                    }
                }
            }

            onProgress(1.0f, "Narration received and cached successfully!")
            val durationMs = WavHelper.getAudioDurationMs(outputFile)
            GeneratedAudioResult(
                audioFile = outputFile,
                durationMs = durationMs,
                mimeType = if (contentType.contains("mp3")) "audio/mp3" else "audio/wav",
                chunkCount = 1
            )
        } catch (e: Exception) {
            outputFile.delete()
            when (e) {
                is TtsException -> throw e
                is UnknownHostException -> throw TtsException.NetworkException(e)
                is SocketTimeoutException -> throw TtsException.TimeoutException()
                else -> throw TtsException.SynthesisException("Backend error: ${e.localizedMessage}")
            }
        }
    }
}

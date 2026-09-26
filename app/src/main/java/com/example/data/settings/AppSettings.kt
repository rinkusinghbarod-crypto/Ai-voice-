package com.example.data.settings

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig

class AppSettings(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("old_story_voice_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_PROVIDER = "tts_provider"
        const val KEY_CUSTOM_GEMINI_KEY = "custom_gemini_api_key"
        const val KEY_BACKEND_URL = "custom_backend_url"
        const val KEY_BACKEND_AUTH = "custom_backend_auth"

        const val PROVIDER_GEMINI = "gemini_tts"
        const val PROVIDER_BACKEND = "custom_backend"
        const val PROVIDER_SYSTEM = "android_system"
    }

    var selectedProvider: String
        get() = prefs.getString(KEY_PROVIDER, PROVIDER_GEMINI) ?: PROVIDER_GEMINI
        set(value) = prefs.edit().putString(KEY_PROVIDER, value).apply()

    var customGeminiApiKey: String
        get() = prefs.getString(KEY_CUSTOM_GEMINI_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_GEMINI_KEY, value.trim()).apply()

    var customBackendUrl: String
        get() = prefs.getString(KEY_BACKEND_URL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_BACKEND_URL, value.trim()).apply()

    var customBackendAuth: String
        get() = prefs.getString(KEY_BACKEND_AUTH, "") ?: ""
        set(value) = prefs.edit().putString(KEY_BACKEND_AUTH, value.trim()).apply()

    /**
     * Resolves the active Gemini API key:
     * 1. User entered key in Settings
     * 2. BuildConfig key injected via Secrets Gradle Plugin (.env)
     */
    fun getEffectiveGeminiApiKey(): String {
        val userKey = customGeminiApiKey.trim()
        if (userKey.isNotEmpty()) return userKey

        return try {
            val buildKey = BuildConfig.GEMINI_API_KEY.trim()
            if (buildKey.isNotEmpty() && buildKey != "MY_GEMINI_API_KEY") {
                buildKey
            } else ""
        } catch (_: Exception) {
            ""
        }
    }

    fun isGeminiKeyConfigured(): Boolean {
        return getEffectiveGeminiApiKey().isNotBlank()
    }
}

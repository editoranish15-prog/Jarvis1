package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class JarvisSettings(
    val userHonorific: String = "Sir",
    val userName: String = "Chief",
    val voicePitch: Float = 0.90f,
    val voiceRate: Float = 0.95f,
    val autoSpeakResponses: Boolean = true,
    val soundEffectsEnabled: Boolean = true,
    val aiModel: String = "gemini-3.8-flash",
    val responseStyle: String = "BALANCED", // "QUICK", "BALANCED", "DETAILED"
    val enableMemoryContext: Boolean = true,
    val eyeTheme: String = "CYAN", // "CYAN", "BLUE", "AMBER", "GREEN", "RED"
    val scanlinesOverlay: Boolean = true,
    val hudGridEnabled: Boolean = true,
    val deviceRole: String = "PRIMARY", // "PRIMARY", "SECONDARY"
    val deviceName: String = "JARVIS-MK85"
)

class JarvisPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("jarvis_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<JarvisSettings> = _settings.asStateFlow()

    private fun loadSettings(): JarvisSettings {
        return JarvisSettings(
            userHonorific = prefs.getString("user_honorific", "Sir") ?: "Sir",
            userName = prefs.getString("user_name", "Chief") ?: "Chief",
            voicePitch = prefs.getFloat("voice_pitch", 0.90f),
            voiceRate = prefs.getFloat("voice_rate", 0.95f),
            autoSpeakResponses = prefs.getBoolean("auto_speak", true),
            soundEffectsEnabled = prefs.getBoolean("sound_fx", true),
            aiModel = prefs.getString("ai_model", "gemini-3.8-flash") ?: "gemini-3.8-flash",
            responseStyle = prefs.getString("response_style", "BALANCED") ?: "BALANCED",
            enableMemoryContext = prefs.getBoolean("memory_context", true),
            eyeTheme = prefs.getString("eye_theme", "CYAN") ?: "CYAN",
            scanlinesOverlay = prefs.getBoolean("scanlines", true),
            hudGridEnabled = prefs.getBoolean("hud_grid", true),
            deviceRole = prefs.getString("device_role", "PRIMARY") ?: "PRIMARY",
            deviceName = prefs.getString("device_name", "JARVIS-MK85") ?: "JARVIS-MK85"
        )
    }

    fun updateSettings(newSettings: JarvisSettings) {
        prefs.edit()
            .putString("user_honorific", newSettings.userHonorific)
            .putString("user_name", newSettings.userName)
            .putFloat("voice_pitch", newSettings.voicePitch)
            .putFloat("voice_rate", newSettings.voiceRate)
            .putBoolean("auto_speak", newSettings.autoSpeakResponses)
            .putBoolean("sound_fx", newSettings.soundEffectsEnabled)
            .putString("ai_model", newSettings.aiModel)
            .putString("response_style", newSettings.responseStyle)
            .putBoolean("memory_context", newSettings.enableMemoryContext)
            .putString("eye_theme", newSettings.eyeTheme)
            .putBoolean("scanlines", newSettings.scanlinesOverlay)
            .putBoolean("hud_grid", newSettings.hudGridEnabled)
            .putString("device_role", newSettings.deviceRole)
            .putString("device_name", newSettings.deviceName)
            .apply()
        _settings.value = newSettings
    }
}

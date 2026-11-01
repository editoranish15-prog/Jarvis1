package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class VoiceState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    MUTED
}

enum class AiStatus {
    ONLINE,
    PROCESSING,
    STANDBY,
    OFFLINE
}

data class SystemTelemetry(
    val batteryPercent: Int = 100,
    val isCharging: Boolean = false,
    val networkStatus: String = "ONLINE (WiFi)",
    val availableRamMb: Long = 2048,
    val totalRamMb: Long = 4096,
    val systemUptime: String = "00:00:00"
)

data class DeviceLinkMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val senderName: String,
    val text: String,
    val type: String = "MSG", // "MSG", "CMD", "STATUS", "ALERT"
    val timestamp: Long = System.currentTimeMillis(),
    val isIncoming: Boolean = true
)

enum class ConnectionStatus {
    DISCONNECTED,
    ADVERTISING,
    SCANNING,
    CONNECTING,
    CONNECTED
}

data class GreetingInfo(
    val salutation: String,
    val fullGreeting: String,
    val timeRangeDescription: String,
    val currentTimeFormatted: String
)

object JarvisGreetings {
    /**
     * Determines the appropriate greeting based on the current local hour:
     * - 05:00 to 11:59: "Good morning"
     * - 12:00 to 16:59: "Good afternoon"
     * - 17:00 to 04:59: "Good evening"
     */
    fun getGreetingInfo(honorific: String = "Sir", calendar: Calendar = Calendar.getInstance()): GreetingInfo {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val currentTimeFormatted = timeFormat.format(calendar.time)

        val (salutation, timeRange) = when (hour) {
            in 5..11 -> Pair("Good morning", "Morning Protocol [05:00 - 11:59]")
            in 12..16 -> Pair("Good afternoon", "Afternoon Protocol [12:00 - 16:59]")
            else -> Pair("Good evening", "Evening Protocol [17:00 - 04:59]")
        }

        val fullGreeting = "$salutation, $honorific. I am JARVIS, your personal AI assistant. How may I assist you today?"

        return GreetingInfo(
            salutation = salutation,
            fullGreeting = fullGreeting,
            timeRangeDescription = timeRange,
            currentTimeFormatted = currentTimeFormatted
        )
    }

    fun generateGreeting(honorific: String = "Sir"): String {
        return getGreetingInfo(honorific).fullGreeting
    }
}

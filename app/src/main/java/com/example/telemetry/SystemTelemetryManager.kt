package com.example.telemetry

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.SystemClock
import com.example.data.model.SystemTelemetry
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.TimeUnit

class SystemTelemetryManager(private val context: Context) {

    fun getTelemetrySnapshot(): SystemTelemetry {
        // 1. Battery Telemetry
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct: Int = if (level >= 0 && scale > 0) ((level / scale.toFloat()) * 100).toInt() else 100
        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        // 2. Network Telemetry
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetwork = connectivityManager?.activeNetwork
        val caps = connectivityManager?.getNetworkCapabilities(activeNetwork)
        val networkStatus = when {
            caps == null -> "OFFLINE"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "ONLINE (Wi-Fi 5GHz)"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "ONLINE (Cellular 5G)"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ONLINE (Ethernet)"
            else -> "CONNECTED"
        }

        // 3. Memory Telemetry
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)
        val availMb = memInfo.availMem / (1024 * 1024)
        val totalMb = memInfo.totalMem / (1024 * 1024)

        // 4. Uptime Telemetry
        val uptimeMillis = SystemClock.elapsedRealtime()
        val hours = TimeUnit.MILLISECONDS.toHours(uptimeMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(uptimeMillis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(uptimeMillis) % 60
        val uptimeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)

        return SystemTelemetry(
            batteryPercent = batteryPct,
            isCharging = isCharging,
            networkStatus = networkStatus,
            availableRamMb = availMb,
            totalRamMb = totalMb,
            systemUptime = uptimeFormatted
        )
    }

    fun pollTelemetryFlow(intervalMs: Long = 2000L): Flow<SystemTelemetry> = flow {
        while (true) {
            emit(getTelemetrySnapshot())
            delay(intervalMs)
        }
    }
}

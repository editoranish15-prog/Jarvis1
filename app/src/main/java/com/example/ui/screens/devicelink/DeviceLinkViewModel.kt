package com.example.ui.screens.devicelink

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.JarvisDeviceLinkManager
import com.example.data.model.ConnectionStatus
import com.example.data.model.DeviceLinkMessage
import com.example.data.preferences.JarvisPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DeviceLinkViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = JarvisPreferences(application)
    val settings = preferences.settings

    private val linkManager = JarvisDeviceLinkManager(application, viewModelScope)

    val connectionStatus: StateFlow<ConnectionStatus> = linkManager.connectionStatus
    val connectedDeviceName: StateFlow<String?> = linkManager.connectedDeviceName
    val messages: StateFlow<List<DeviceLinkMessage>> = linkManager.messages

    private val _pairedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val pairedDevices: StateFlow<List<BluetoothDevice>> = _pairedDevices.asStateFlow()

    private val _isBluetoothSupported = MutableStateFlow(linkManager.bluetoothAdapter != null)
    val isBluetoothSupported: StateFlow<Boolean> = _isBluetoothSupported.asStateFlow()

    private val _isBluetoothEnabled = MutableStateFlow(linkManager.bluetoothAdapter?.isEnabled == true)
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    init {
        refreshDevices()
    }

    fun setDeviceRole(newRole: String) {
        val current = settings.value
        preferences.updateSettings(current.copy(deviceRole = newRole))
    }

    @SuppressLint("MissingPermission")
    fun refreshDevices() {
        _isBluetoothEnabled.value = linkManager.bluetoothAdapter?.isEnabled == true
        if (linkManager.hasPermissions()) {
            _pairedDevices.value = linkManager.getPairedDevices()
        }
    }

    fun startAdvertisingHost() {
        val myName = settings.value.deviceName
        linkManager.startAsPrimary(myName)
    }

    fun connectToDevice(device: BluetoothDevice) {
        linkManager.connectToSecondary(device)
    }

    fun sendLinkMessage(text: String, type: String = "MSG"): Boolean {
        if (text.isBlank()) return false
        val myName = settings.value.deviceName
        return linkManager.sendMessage(text, myName, type)
    }

    fun sendTacticalCommand(command: String) {
        val myName = settings.value.deviceName
        val payload = when (command) {
            "PING" -> "PING_REQUEST // ACK REQUIRED"
            "STATUS" -> "STATUS_REQUEST // UPTIME & POWER REPORT"
            "ALERT" -> "DEFENSE_ALERT // HIGH PRIORITY WARNING"
            else -> command
        }
        linkManager.sendMessage(payload, myName, "CMD")
    }

    fun disconnect() {
        linkManager.disconnect()
    }

    fun clearHistory() {
        linkManager.clearMessages()
    }

    // Diagnostic loopback link simulation for single-device verification
    fun runLoopbackTest() {
        viewModelScope.launch {
            val myName = settings.value.deviceName
            val testMsg = DeviceLinkMessage(
                senderName = "JARVIS-LINK-DIAGNOSTIC",
                text = "Diagnostic Packet: Application-level protocol validated. Local loopback OK.",
                type = "STATUS",
                isIncoming = true
            )
            linkManager.sendMessage("DIAGNOSTIC_ECHO", myName, "STATUS")
        }
    }
}

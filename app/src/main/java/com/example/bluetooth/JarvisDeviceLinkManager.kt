package com.example.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.model.ConnectionStatus
import com.example.data.model.DeviceLinkMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.util.UUID

private const val TAG = "JarvisDeviceLink"
private val JARVIS_LINK_UUID: UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")
private const val SERVICE_NAME = "JARVIS_SECURE_LINK"

class JarvisDeviceLinkManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _connectedDeviceName = MutableStateFlow<String?>(null)
    val connectedDeviceName: StateFlow<String?> = _connectedDeviceName.asStateFlow()

    private val _messages = MutableStateFlow<List<DeviceLinkMessage>>(emptyList())
    val messages: StateFlow<List<DeviceLinkMessage>> = _messages.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothDevice>> = _discoveredDevices.asStateFlow()

    private var serverSocket: BluetoothServerSocket? = null
    private var activeSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var listenerJob: Job? = null

    fun hasPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val connect = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
            val scan = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
            return connect && scan
        }
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.BLUETOOTH
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun startAsPrimary(deviceName: String) {
        if (!hasPermissions() || bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Log.w(TAG, "Bluetooth not ready or missing permissions")
            return
        }

        disconnect()
        _connectionStatus.value = ConnectionStatus.ADVERTISING

        scope.launch(Dispatchers.IO) {
            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(SERVICE_NAME, JARVIS_LINK_UUID)
                Log.d(TAG, "Primary listening for incoming JARVIS links...")
                val socket = serverSocket?.accept()
                if (socket != null) {
                    serverSocket?.close()
                    serverSocket = null
                    handleConnectedSocket(socket, socket.remoteDevice.name ?: "Secondary Device")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in primary server listener", e)
                if (_connectionStatus.value == ConnectionStatus.ADVERTISING) {
                    _connectionStatus.value = ConnectionStatus.DISCONNECTED
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDevice> {
        if (!hasPermissions() || bluetoothAdapter == null) return emptyList()
        return bluetoothAdapter.bondedDevices?.toList().orEmpty()
    }

    @SuppressLint("MissingPermission")
    fun connectToSecondary(device: BluetoothDevice) {
        if (!hasPermissions() || bluetoothAdapter == null) return

        disconnect()
        _connectionStatus.value = ConnectionStatus.CONNECTING
        val name = device.name ?: device.address

        scope.launch(Dispatchers.IO) {
            try {
                bluetoothAdapter.cancelDiscovery()
                val socket = device.createRfcommSocketToServiceRecord(JARVIS_LINK_UUID)
                socket.connect()
                handleConnectedSocket(socket, name)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to connect to device $name", e)
                _connectionStatus.value = ConnectionStatus.DISCONNECTED
                addSystemNotice("Connection to $name failed: ${e.localizedMessage}")
            }
        }
    }

    private fun handleConnectedSocket(socket: BluetoothSocket, deviceName: String) {
        activeSocket = socket
        outputStream = socket.outputStream
        _connectedDeviceName.value = deviceName
        _connectionStatus.value = ConnectionStatus.CONNECTED

        addSystemNotice("Secure quantum link established with $deviceName.")

        // Start reading loop
        listenerJob = scope.launch(Dispatchers.IO) {
            val reader = BufferedReader(InputStreamReader(socket.inputStream))
            try {
                while (isActive) {
                    val line = reader.readLine() ?: break
                    parseIncomingMessage(line)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Socket read error", e)
            } finally {
                disconnect()
            }
        }
    }

    fun sendMessage(text: String, myName: String, type: String = "MSG"): Boolean {
        if (_connectionStatus.value != ConnectionStatus.CONNECTED || outputStream == null) {
            return false
        }

        val json = JSONObject().apply {
            put("id", UUID.randomUUID().toString())
            put("sender", myName)
            put("text", text)
            put("type", type)
            put("timestamp", System.currentTimeMillis())
        }

        return try {
            scope.launch(Dispatchers.IO) {
                outputStream?.write((json.toString() + "\n").toByteArray(Charsets.UTF_8))
                outputStream?.flush()
            }

            val msg = DeviceLinkMessage(
                senderName = myName,
                text = text,
                type = type,
                isIncoming = false
            )
            _messages.value = _messages.value + msg
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error writing message to socket", e)
            disconnect()
            false
        }
    }

    private fun parseIncomingMessage(jsonString: String) {
        try {
            val obj = JSONObject(jsonString)
            val id = obj.optString("id", UUID.randomUUID().toString())
            val sender = obj.optString("sender", "Unknown JARVIS Unit")
            val text = obj.optString("text", "")
            val type = obj.optString("type", "MSG")
            val ts = obj.optLong("timestamp", System.currentTimeMillis())

            val message = DeviceLinkMessage(
                id = id,
                senderName = sender,
                text = text,
                type = type,
                timestamp = ts,
                isIncoming = true
            )
            _messages.value = _messages.value + message
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing incoming protocol packet: $jsonString", e)
        }
    }

    private fun addSystemNotice(notice: String) {
        val msg = DeviceLinkMessage(
            senderName = "JARVIS SYSTEM",
            text = notice,
            type = "STATUS",
            isIncoming = true
        )
        _messages.value = _messages.value + msg
    }

    fun disconnect() {
        listenerJob?.cancel()
        listenerJob = null

        try {
            outputStream?.close()
        } catch (_: Exception) {}
        outputStream = null

        try {
            activeSocket?.close()
        } catch (_: Exception) {}
        activeSocket = null

        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serverSocket = null

        _connectionStatus.value = ConnectionStatus.DISCONNECTED
        _connectedDeviceName.value = null
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }
}

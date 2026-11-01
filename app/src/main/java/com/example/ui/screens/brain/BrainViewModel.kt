package com.example.ui.screens.brain

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.JarvisBrainService
import com.example.data.local.JarvisDatabase
import com.example.data.local.entity.MemoryItemEntity
import com.example.data.preferences.JarvisPreferences
import com.example.data.preferences.JarvisSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DiagnosticResult(
    val latencyMs: Long = 0,
    val isSuccess: Boolean = false,
    val message: String = "No diagnostic test performed yet."
)

class BrainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = JarvisDatabase.getInstance(application)
    private val memoryDao = db.memoryDao()
    private val preferences = JarvisPreferences(application)
    private val brainService = JarvisBrainService()

    val settings: StateFlow<JarvisSettings> = preferences.settings

    val memories: StateFlow<List<MemoryItemEntity>> = memoryDao.getAllMemories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _diagnostic = MutableStateFlow(DiagnosticResult())
    val diagnostic: StateFlow<DiagnosticResult> = _diagnostic.asStateFlow()

    private val _isTestingConnection = MutableStateFlow(false)
    val isTestingConnection: StateFlow<Boolean> = _isTestingConnection.asStateFlow()

    fun updateModel(newModel: String) {
        val current = settings.value
        preferences.updateSettings(current.copy(aiModel = newModel))
    }

    fun updateResponseStyle(newStyle: String) {
        val current = settings.value
        preferences.updateSettings(current.copy(responseStyle = newStyle))
    }

    fun toggleMemoryContext(enabled: Boolean) {
        val current = settings.value
        preferences.updateSettings(current.copy(enableMemoryContext = enabled))
    }

    fun addMemory(key: String, value: String, category: String = "Personal") {
        if (key.isBlank() || value.isBlank()) return
        viewModelScope.launch {
            memoryDao.insertMemory(
                MemoryItemEntity(
                    key = key.trim(),
                    value = value.trim(),
                    category = category.trim()
                )
            )
        }
    }

    fun deleteMemory(item: MemoryItemEntity) {
        viewModelScope.launch {
            memoryDao.deleteMemory(item)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            memoryDao.clearAllMemories()
        }
    }

    fun runDiagnosticPing() {
        if (_isTestingConnection.value) return
        _isTestingConnection.value = true

        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            try {
                val current = settings.value
                val testPrompt = "Ping check. State system operational status in one sentence."
                val answer = brainService.generateContent(
                    prompt = testPrompt,
                    modelName = current.aiModel,
                    honorific = current.userHonorific,
                    responseStyle = "QUICK"
                )
                val duration = System.currentTimeMillis() - startTime
                _diagnostic.value = DiagnosticResult(
                    latencyMs = duration,
                    isSuccess = true,
                    message = "Diagnostic link confirmed ($duration ms): \"$answer\""
                )
            } catch (e: Exception) {
                val duration = System.currentTimeMillis() - startTime
                _diagnostic.value = DiagnosticResult(
                    latencyMs = duration,
                    isSuccess = false,
                    message = "Diagnostic test failed: ${e.localizedMessage}"
                )
            } finally {
                _isTestingConnection.value = false
            }
        }
    }
}

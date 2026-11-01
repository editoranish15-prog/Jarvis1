package com.example.ui.screens.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.JarvisBrainService
import com.example.data.local.JarvisDatabase
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ChatSessionEntity
import com.example.data.preferences.JarvisPreferences
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val db = JarvisDatabase.getInstance(application)
    private val chatDao = db.chatDao()
    private val memoryDao = db.memoryDao()
    private val preferences = JarvisPreferences(application)
    private val brainService = JarvisBrainService()

    private val _currentSessionId = MutableStateFlow(UUID.randomUUID().toString())
    val currentSessionId: StateFlow<String> = _currentSessionId.asStateFlow()

    val messages: StateFlow<List<ChatMessageEntity>> = _currentSessionId.flatMapLatest { sessionId ->
        chatDao.getMessagesForSession(sessionId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _streamingText = MutableStateFlow("")
    val streamingText: StateFlow<String> = _streamingText.asStateFlow()

    private val _lastUserPrompt = MutableStateFlow("")
    val lastUserPrompt: StateFlow<String> = _lastUserPrompt.asStateFlow()

    private var activeJob: Job? = null

    init {
        ensureCurrentSessionExists()
    }

    private fun ensureCurrentSessionExists() {
        viewModelScope.launch {
            val sessionId = _currentSessionId.value
            val existing = chatDao.getSessionById(sessionId)
            if (existing == null) {
                chatDao.insertSession(
                    ChatSessionEntity(
                        id = sessionId,
                        title = "New Neural Conversation"
                    )
                )
            }
        }
    }

    fun loadSession(sessionId: String) {
        _currentSessionId.value = sessionId
        ensureCurrentSessionExists()
    }

    fun sendMessage(
        content: String,
        onGenerated: (String) -> Unit = {}
    ) {
        if (content.isBlank() || _isGenerating.value) return

        val sessionId = _currentSessionId.value
        _lastUserPrompt.value = content
        _isGenerating.value = true
        _streamingText.value = ""

        viewModelScope.launch {
            // Insert user message into local database
            chatDao.insertMessage(
                ChatMessageEntity(
                    sessionId = sessionId,
                    sender = "user",
                    content = content
                )
            )

            // Update session title if this is the first message
            val currentList = messages.value
            if (currentList.isEmpty() || currentList.size == 1) {
                val autoTitle = if (content.length > 30) content.take(30) + "..." else content
                chatDao.updateSession(sessionId, autoTitle, System.currentTimeMillis())
            } else {
                chatDao.updateSession(sessionId, currentList.first().content.take(30), System.currentTimeMillis())
            }

            // Gather memory items if enabled
            val settings = preferences.settings.value
            val memoryContext = if (settings.enableMemoryContext) {
                val memories = memoryDao.getAllMemoriesList()
                memories.joinToString(separator = "\n") { "- ${it.key}: ${it.value}" }
            } else ""

            activeJob = launch {
                val fullResponse = StringBuilder()
                try {
                    brainService.generateStream(
                        prompt = content,
                        modelName = settings.aiModel,
                        honorific = settings.userHonorific,
                        responseStyle = settings.responseStyle,
                        memoryContext = memoryContext
                    ).collect { chunk ->
                        fullResponse.append(chunk)
                        _streamingText.value = fullResponse.toString()
                    }

                    val finalAnswer = fullResponse.toString().trim()
                    if (finalAnswer.isNotEmpty()) {
                        chatDao.insertMessage(
                            ChatMessageEntity(
                                sessionId = sessionId,
                                sender = "jarvis",
                                content = finalAnswer
                            )
                        )
                        onGenerated(finalAnswer)
                    }
                } catch (e: Exception) {
                    val fallback = "Diagnostic notification: Communication link interrupted (${e.localizedMessage})."
                    chatDao.insertMessage(
                        ChatMessageEntity(
                            sessionId = sessionId,
                            sender = "jarvis",
                            content = fallback
                        )
                    )
                    onGenerated(fallback)
                } finally {
                    _isGenerating.value = false
                    _streamingText.value = ""
                }
            }
        }
    }

    fun stopGeneration() {
        activeJob?.cancel()
        activeJob = null
        if (_streamingText.value.isNotBlank()) {
            val partial = _streamingText.value
            viewModelScope.launch {
                chatDao.insertMessage(
                    ChatMessageEntity(
                        sessionId = _currentSessionId.value,
                        sender = "jarvis",
                        content = "$partial\n[Transmission halted by user command]"
                    )
                )
                _streamingText.value = ""
                _isGenerating.value = false
            }
        } else {
            _isGenerating.value = false
        }
    }

    fun regenerateLast() {
        val lastPrompt = _lastUserPrompt.value
        if (lastPrompt.isNotBlank()) {
            sendMessage(lastPrompt)
        }
    }

    fun startNewChat() {
        stopGeneration()
        val newId = UUID.randomUUID().toString()
        _currentSessionId.value = newId
        ensureCurrentSessionExists()
    }

    fun clearCurrentChat() {
        stopGeneration()
        viewModelScope.launch {
            chatDao.deleteMessagesForSession(_currentSessionId.value)
        }
    }
}

package com.example.ui.screens.tools

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.local.JarvisDatabase
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

enum class SmartToolTab {
    CALCULATOR,
    NOTES,
    TASKS,
    REMINDERS,
    LAUNCHER
}

class SmartToolsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = JarvisDatabase.getInstance(application)
    private val noteDao = db.noteDao()
    private val taskDao = db.taskDao()

    private val _activeTab = MutableStateFlow(SmartToolTab.CALCULATOR)
    val activeTab: StateFlow<SmartToolTab> = _activeTab.asStateFlow()

    // 1. Calculator State
    private val _calcExpression = MutableStateFlow("")
    val calcExpression: StateFlow<String> = _calcExpression.asStateFlow()

    private val _calcResult = MutableStateFlow("0")
    val calcResult: StateFlow<String> = _calcResult.asStateFlow()

    // 2. Notes State
    val notes: StateFlow<List<NoteEntity>> = noteDao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 3. Tasks State
    val tasks: StateFlow<List<TaskEntity>> = taskDao.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        createNotificationChannel()
    }

    fun setTab(tab: SmartToolTab) {
        _activeTab.value = tab
    }

    // --- Calculator Engine ---
    fun onCalcInput(input: String) {
        when (input) {
            "C" -> {
                _calcExpression.value = ""
                _calcResult.value = "0"
            }
            "DEL" -> {
                val current = _calcExpression.value
                if (current.isNotEmpty()) {
                    _calcExpression.value = current.dropLast(1)
                }
            }
            "=" -> evaluateExpression()
            "sqrt" -> {
                applySingleOp { sqrt(it) }
            }
            "sin" -> {
                applySingleOp { sin(Math.toRadians(it)) }
            }
            "cos" -> {
                applySingleOp { cos(Math.toRadians(it)) }
            }
            "tan" -> {
                applySingleOp { tan(Math.toRadians(it)) }
            }
            "log" -> {
                applySingleOp { log10(it) }
            }
            "ln" -> {
                applySingleOp { ln(it) }
            }
            else -> {
                _calcExpression.value += input
            }
        }
    }

    private fun applySingleOp(op: (Double) -> Double) {
        val expr = _calcExpression.value
        val number = expr.toDoubleOrNull() ?: _calcResult.value.toDoubleOrNull()
        if (number != null) {
            try {
                val res = op(number)
                _calcResult.value = String.format("%.4f", res).trimEnd('0').trimEnd('.')
                _calcExpression.value = ""
            } catch (e: Exception) {
                _calcResult.value = "Error"
            }
        }
    }

    private fun evaluateExpression() {
        val expr = _calcExpression.value.replace("×", "*").replace("÷", "/")
        if (expr.isBlank()) return

        try {
            // Simple arithmetic evaluator
            val result = simpleEvaluate(expr)
            _calcResult.value = String.format("%.4f", result).trimEnd('0').trimEnd('.')
        } catch (e: Exception) {
            _calcResult.value = "Syntax Error"
        }
    }

    private fun simpleEvaluate(expression: String): Double {
        // Handle basic chain addition, subtraction, multiplication, division
        var current = 0.0
        var currentOp = '+'
        var currentToken = StringBuilder()

        val tokens = mutableListOf<String>()
        val ops = mutableListOf<Char>()

        var i = 0
        while (i < expression.length) {
            val c = expression[i]
            if (c in listOf('+', '-', '*', '/')) {
                if (currentToken.isNotEmpty()) {
                    tokens.add(currentToken.toString())
                    currentToken = StringBuilder()
                }
                ops.add(c)
            } else if (c.isDigit() || c == '.') {
                currentToken.append(c)
            }
            i++
        }
        if (currentToken.isNotEmpty()) {
            tokens.add(currentToken.toString())
        }

        if (tokens.isEmpty()) return 0.0

        // Perform * and /
        val simplifiedTokens = mutableListOf<Double>()
        val simplifiedOps = mutableListOf<Char>()

        var index = 0
        while (index < tokens.size) {
            var valAt = tokens[index].toDoubleOrNull() ?: 0.0
            while (index < ops.size && (ops[index] == '*' || ops[index] == '/')) {
                val nextVal = tokens[index + 1].toDoubleOrNull() ?: 1.0
                if (ops[index] == '*') {
                    valAt *= nextVal
                } else {
                    valAt = if (nextVal != 0.0) valAt / nextVal else 0.0
                }
                index++
            }
            simplifiedTokens.add(valAt)
            if (index < ops.size) {
                simplifiedOps.add(ops[index])
            }
            index++
        }

        // Perform + and -
        var finalResult = simplifiedTokens.firstOrNull() ?: 0.0
        for (j in simplifiedOps.indices) {
            val op = simplifiedOps[j]
            val next = simplifiedTokens.getOrNull(j + 1) ?: 0.0
            if (op == '+') {
                finalResult += next
            } else if (op == '-') {
                finalResult -= next
            }
        }
        return finalResult
    }

    // --- Notes Manager ---
    fun addNote(title: String, content: String, category: String = "Tactical") {
        if (title.isBlank() && content.isBlank()) return
        viewModelScope.launch {
            noteDao.insertNote(
                NoteEntity(
                    title = title.ifBlank { "Untitled Note" },
                    content = content,
                    category = category
                )
            )
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            noteDao.deleteNote(note)
        }
    }

    // --- Tasks Manager ---
    fun addTask(title: String, priority: String = "Normal", dueDate: String = "") {
        if (title.isBlank()) return
        viewModelScope.launch {
            taskDao.insertTask(
                TaskEntity(
                    title = title,
                    priority = priority,
                    dueDate = dueDate
                )
            )
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            taskDao.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            taskDao.deleteTask(task)
        }
    }

    // --- Reminders & Notifications ---
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "JARVIS Tactical Reminders"
            val descriptionText = "Notifications and operational reminders from JARVIS"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("JARVIS_REMINDERS", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getApplication<Application>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun triggerNotificationReminder(title: String, body: String): Boolean {
        val context = getApplication<Application>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) return false
        }

        val builder = NotificationCompat.Builder(context, "JARVIS_REMINDERS")
            .setSmallIcon(R.drawable.jarvis_core)
            .setContentTitle("JARVIS: $title")
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), builder.build())
        return true
    }

    // --- App Launcher ---
    fun launchApp(action: String) {
        val context = getApplication<Application>()
        val intent = when (action) {
            "CAMERA" -> Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
            "SETTINGS" -> Intent(Settings.ACTION_SETTINGS)
            "BROWSER" -> Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com"))
            "MAPS" -> Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=location"))
            "DIALER" -> Intent(Intent.ACTION_DIAL)
            else -> null
        }

        intent?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            try {
                context.startActivity(this)
            } catch (e: Exception) {
                // If not resolvable
            }
        }
    }
}

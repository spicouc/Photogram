package com.akslabs.cloudgallery.utils

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Centralized debug log manager that captures logs in memory
 * for display in the Debug Logs screen.
 */
object DebugLogManager {
    private const val TAG = "DebugLogManager"
    private const val MAX_LOG_ENTRIES = 500

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    enum class Level {
        VERBOSE, DEBUG, INFO, WARNING, ERROR
    }

    data class LogEntry(
        val timestamp: Long,
        val timeFormatted: String,
        val level: Level,
        val tag: String,
        val message: String
    )

    fun log(level: Level, tag: String, message: String) {
        val timestamp = System.currentTimeMillis()
        val entry = LogEntry(
            timestamp = timestamp,
            timeFormatted = dateFormat.format(Date(timestamp)),
            level = level,
            tag = tag,
            message = message
        )

        val currentLogs = _logs.value.toMutableList()
        currentLogs.add(entry)

        // Keep only the last MAX_LOG_ENTRIES
        if (currentLogs.size > MAX_LOG_ENTRIES) {
            currentLogs.removeAt(0)
        }

        _logs.value = currentLogs

        // Also log to Android system log
        when (level) {
            Level.VERBOSE -> Log.v(tag, message)
            Level.DEBUG -> Log.d(tag, message)
            Level.INFO -> Log.i(tag, message)
            Level.WARNING -> Log.w(tag, message)
            Level.ERROR -> Log.e(tag, message)
        }
    }

    fun clear() {
        _logs.value = emptyList()
        Log.i(TAG, "Logs cleared")
    }

    fun getLogsAsText(): String {
        return _logs.value.joinToString("\n") { entry ->
            val levelChar = when (entry.level) {
                Level.VERBOSE -> "V"
                Level.DEBUG -> "D"
                Level.INFO -> "I"
                Level.WARNING -> "W"
                Level.ERROR -> "E"
            }
            "${entry.timeFormatted} $levelChar/${entry.tag}: ${entry.message}"
        }
    }

    fun v(tag: String, message: String) = log(Level.VERBOSE, tag, message)
    fun d(tag: String, message: String) = log(Level.DEBUG, tag, message)
    fun i(tag: String, message: String) = log(Level.INFO, tag, message)
    fun w(tag: String, message: String) = log(Level.WARNING, tag, message)
    fun e(tag: String, message: String) = log(Level.ERROR, tag, message)
}

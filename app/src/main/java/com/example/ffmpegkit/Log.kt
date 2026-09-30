package com.example.ffmpegkit

enum class Level {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
    FATAL
}

/**
 * Log message emitted during an FFmpegKit execution session.
 */
data class Log(
    private val sessionId: Long,
    private val level: Level,
    private val message: String
) {
    fun getSessionId(): Long = sessionId
    fun getLevel(): Level = level
    fun getMessage(): String = message

    override fun toString(): String = "[$level] $message"
}

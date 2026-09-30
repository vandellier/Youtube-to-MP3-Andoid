package com.example.ffmpegkit

import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Represents an active or completed FFmpeg execution session.
 */
class FFmpegSession(
    private val sessionId: Long,
    private val command: String
) {
    private val startTime = Date()
    private var endTime: Date? = null
    private var returnCode: ReturnCode? = null
    private val logs = mutableListOf<Log>()
    private var failStackTrace: String? = null
    private val isCancelled = AtomicBoolean(false)
    private var statistics: Statistics? = null

    fun getSessionId(): Long = sessionId

    fun getCommand(): String = command

    fun getStartTime(): Date = startTime

    fun getEndTime(): Date? = endTime

    fun getDuration(): Long {
        val end = endTime?.time ?: System.currentTimeMillis()
        return end - startTime.time
    }

    fun getReturnCode(): ReturnCode? = returnCode

    fun getFailStackTrace(): String? = failStackTrace

    fun getLogs(): List<Log> = synchronized(logs) { logs.toList() }

    fun getAllLogsAsString(): String = synchronized(logs) {
        logs.joinToString("\n") { it.getMessage() }
    }

    fun getStatistics(): Statistics? = statistics

    fun cancel() {
        isCancelled.set(true)
    }

    fun isCancelled(): Boolean = isCancelled.get()

    internal fun addLog(log: Log) {
        synchronized(logs) {
            logs.add(log)
        }
    }

    internal fun setStatistics(stats: Statistics) {
        this.statistics = stats
    }

    internal fun complete(code: ReturnCode, stackTrace: String? = null) {
        this.endTime = Date()
        this.returnCode = code
        this.failStackTrace = stackTrace
    }
}

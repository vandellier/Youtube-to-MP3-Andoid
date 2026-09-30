package com.example.ffmpegkit

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Main FFmpegKit entry point for on-device media processing and audio encoding.
 * Implements standard FFmpegKit execution methods for synchronous and asynchronous execution.
 */
object FFmpegKit {

    private val sessionCounter = AtomicLong(1000)
    private val activeSessions = ConcurrentHashMap<Long, FFmpegSession>()
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Executes an FFmpeg command synchronously.
     */
    fun execute(command: String): FFmpegSession {
        val sessionId = sessionCounter.incrementAndGet()
        val session = FFmpegSession(sessionId, command)
        activeSessions[sessionId] = session

        try {
            val returnCode = FFmpegAudioEncoder.process(
                session = session,
                logCallback = null,
                statisticsCallback = null
            )
            session.complete(returnCode)
        } catch (e: Exception) {
            session.complete(ReturnCode(1), e.stackTraceToString())
        } finally {
            activeSessions.remove(sessionId)
        }

        return session
    }

    /**
     * Executes an FFmpeg command asynchronously with callbacks for session completion, logs, and statistics.
     */
    fun executeAsync(
        command: String,
        executeCallback: (FFmpegSession) -> Unit,
        logCallback: ((Log) -> Unit)? = null,
        statisticsCallback: ((Statistics) -> Unit)? = null
    ): FFmpegSession {
        val sessionId = sessionCounter.incrementAndGet()
        val session = FFmpegSession(sessionId, command)
        activeSessions[sessionId] = session

        scope.launch {
            try {
                val returnCode = FFmpegAudioEncoder.process(
                    session = session,
                    logCallback = logCallback,
                    statisticsCallback = statisticsCallback
                )
                session.complete(returnCode)
            } catch (e: Exception) {
                session.complete(ReturnCode(1), e.stackTraceToString())
            } finally {
                activeSessions.remove(sessionId)
                executeCallback(session)
            }
        }

        return session
    }

    /**
     * Cancels an ongoing session by ID.
     */
    fun cancel(sessionId: Long) {
        activeSessions[sessionId]?.cancel()
    }

    /**
     * Cancels all active sessions.
     */
    fun cancel() {
        activeSessions.values.forEach { it.cancel() }
    }

    /**
     * Lists all current sessions.
     */
    fun listSessions(): List<FFmpegSession> = activeSessions.values.toList()
}

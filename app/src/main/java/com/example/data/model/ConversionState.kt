package com.example.data.model

enum class LogLevel {
    INFO,
    STREAM,
    SUCCESS,
    WARN,
    ERROR
}

data class TerminalLogEntry(
    val id: Long = System.nanoTime(),
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String,
    val message: String,
    val level: LogLevel = LogLevel.INFO
)

sealed interface ConversionState {
    data object Idle : ConversionState
    data class FetchingMetadata(val url: String) : ConversionState
    data class Ready(val videoInfo: YouTubeVideoInfo, val quality: AudioQuality) : ConversionState
    data class Converting(
        val videoInfo: YouTubeVideoInfo,
        val progress: Float,
        val currentBytes: Long,
        val totalBytes: Long,
        val speedKbps: Int,
        val stage: String
    ) : ConversionState
    data class Success(val track: DownloadedTrack) : ConversionState
    data class Error(val message: String, val rawUrl: String? = null) : ConversionState
}

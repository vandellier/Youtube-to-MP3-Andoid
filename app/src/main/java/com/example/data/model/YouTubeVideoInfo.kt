package com.example.data.model

data class YouTubeVideoInfo(
    val videoId: String,
    val originalUrl: String,
    val title: String,
    val authorName: String,
    val thumbnailUrl: String,
    val durationSeconds: Int = 214,
    val streamBitrate: Int = 320
) {
    val estimatedSizeBytes: Long
        get() {
            // Approximation: bitrate (kbps) * 1000 / 8 * duration
            return (streamBitrate * 1000L / 8L) * durationSeconds
        }

    val formattedEstimatedSize: String
        get() {
            val mb = estimatedSizeBytes.toDouble() / (1024.0 * 1024.0)
            return "%.2f MB".format(mb)
        }

    val formattedDuration: String
        get() {
            val mins = durationSeconds / 60
            val secs = durationSeconds % 60
            return "%d:%02d".format(mins, secs)
        }
}

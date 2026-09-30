package com.example.ffmpegkit

/**
 * Statistics emitted by FFmpegKit during audio/video processing.
 */
data class Statistics(
    private val sessionId: Long,
    private val videoFrameNumber: Int,
    private val fps: Float,
    private val quality: Float,
    private val size: Long,
    private val time: Long, // in milliseconds
    private val bitrate: Double, // in kbps
    private val speed: Double // e.g. 1.5x
) {
    fun getSessionId(): Long = sessionId
    fun getVideoFrameNumber(): Int = videoFrameNumber
    fun getFps(): Float = fps
    fun getQuality(): Float = quality
    fun getSize(): Long = size
    fun getTime(): Long = time
    fun getBitrate(): Double = bitrate
    fun getSpeed(): Double = speed

    override fun toString(): String {
        return "size=${size / 1024}kB time=${time / 1000}s bitrate=${bitrate}kbits/s speed=${speed}x"
    }
}

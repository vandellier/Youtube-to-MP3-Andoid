package com.example.data.model

enum class AudioQuality(
    val bitrateKbps: Int,
    val label: String,
    val badge: String,
    val description: String
) {
    KBPS_320(320, "320 KBPS", "HQ AUDIO", "Extreme Studio Quality (CBR)"),
    KBPS_256(256, "256 KBPS", "HI-FI", "High Cyber Clarity"),
    KBPS_192(192, "192 KBPS", "STD", "Balanced Standard Fidelity"),
    KBPS_128(128, "128 KBPS", "ECO", "Compact Matrix Stream")
}

enum class AudioContainer(
    val extension: String,
    val mimeType: String,
    val label: String
) {
    MP3("mp3", "audio/mpeg", "MP3 (MPEG-Audio Layer III)"),
    FLAC("flac", "audio/flac", "FLAC (Lossless)"),
    WAV("wav", "audio/wav", "WAV (Raw PCM)")
}

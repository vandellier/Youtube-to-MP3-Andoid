package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_tracks")
data class DownloadedTrack(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val youtubeId: String,
    val title: String,
    val artist: String,
    val durationSeconds: Int = 180,
    val format: String = "MP3",
    val bitrateKbps: Int = 320,
    val fileSizeBytes: Long = 0L,
    val localFilePath: String,
    val thumbnailUrl: String = "",
    val downloadTimestamp: Long = System.currentTimeMillis(),
    val playCount: Int = 0,
    val isFavorite: Boolean = false
) {
    val formattedDuration: String
        get() {
            val mins = durationSeconds / 60
            val secs = durationSeconds % 60
            return "%d:%02d".format(mins, secs)
        }

    val formattedSize: String
        get() {
            val mb = fileSizeBytes.toDouble() / (1024.0 * 1024.0)
            return "%.2f MB".format(mb)
        }
}

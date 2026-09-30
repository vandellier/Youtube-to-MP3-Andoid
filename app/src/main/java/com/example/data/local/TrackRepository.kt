package com.example.data.local

import com.example.data.model.DownloadedTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class TrackRepository(private val trackDao: TrackDao) {

    val allTracks: Flow<List<DownloadedTrack>> = trackDao.getAllTracks()
    val favoriteTracks: Flow<List<DownloadedTrack>> = trackDao.getFavoriteTracks()
    val trackCount: Flow<Int> = trackDao.getTrackCount()
    val totalSizeBytes: Flow<Long> = trackDao.getTotalSizeBytes()

    fun searchTracks(query: String): Flow<List<DownloadedTrack>> {
        return if (query.isBlank()) {
            trackDao.getAllTracks()
        } else {
            trackDao.searchTracks(query.trim())
        }
    }

    suspend fun findByYoutubeId(youtubeId: String): DownloadedTrack? = withContext(Dispatchers.IO) {
        trackDao.findByYoutubeId(youtubeId)
    }

    suspend fun saveTrack(track: DownloadedTrack): Long = withContext(Dispatchers.IO) {
        trackDao.insertTrack(track)
    }

    suspend fun updateTrack(track: DownloadedTrack) = withContext(Dispatchers.IO) {
        trackDao.updateTrack(track)
    }

    suspend fun toggleFavorite(id: Long) = withContext(Dispatchers.IO) {
        trackDao.toggleFavorite(id)
    }

    suspend fun incrementPlayCount(id: Long) = withContext(Dispatchers.IO) {
        trackDao.incrementPlayCount(id)
    }

    suspend fun deleteTrack(track: DownloadedTrack) = withContext(Dispatchers.IO) {
        // Remove physical audio file
        try {
            val file = File(track.localFilePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (_: Exception) {
            // Best effort file deletion
        }
        trackDao.deleteTrack(track)
    }

    suspend fun deleteById(id: Long, filePath: String?) = withContext(Dispatchers.IO) {
        if (!filePath.isNullOrBlank()) {
            try {
                val file = File(filePath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (_: Exception) {
            }
        }
        trackDao.deleteById(id)
    }
}

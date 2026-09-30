package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DownloadedTrack
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM downloaded_tracks ORDER BY downloadTimestamp DESC")
    fun getAllTracks(): Flow<List<DownloadedTrack>>

    @Query("SELECT * FROM downloaded_tracks WHERE isFavorite = 1 ORDER BY downloadTimestamp DESC")
    fun getFavoriteTracks(): Flow<List<DownloadedTrack>>

    @Query("SELECT * FROM downloaded_tracks WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' ORDER BY downloadTimestamp DESC")
    fun searchTracks(query: String): Flow<List<DownloadedTrack>>

    @Query("SELECT * FROM downloaded_tracks WHERE id = :id LIMIT 1")
    fun getTrackById(id: Long): Flow<DownloadedTrack?>

    @Query("SELECT * FROM downloaded_tracks WHERE youtubeId = :youtubeId LIMIT 1")
    suspend fun findByYoutubeId(youtubeId: String): DownloadedTrack?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: DownloadedTrack): Long

    @Update
    suspend fun updateTrack(track: DownloadedTrack)

    @Delete
    suspend fun deleteTrack(track: DownloadedTrack)

    @Query("DELETE FROM downloaded_tracks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE downloaded_tracks SET playCount = playCount + 1 WHERE id = :id")
    suspend fun incrementPlayCount(id: Long)

    @Query("UPDATE downloaded_tracks SET isFavorite = CASE WHEN isFavorite = 1 THEN 0 ELSE 1 END WHERE id = :id")
    suspend fun toggleFavorite(id: Long)

    @Query("SELECT COUNT(*) FROM downloaded_tracks")
    fun getTrackCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(fileSizeBytes), 0) FROM downloaded_tracks")
    fun getTotalSizeBytes(): Flow<Long>
}

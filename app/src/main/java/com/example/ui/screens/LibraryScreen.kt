package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.DownloadedTrack
import com.example.ui.components.CyberCard
import com.example.ui.components.NeonButton
import com.example.ui.theme.LocalThemePalette
import com.example.ui.theme.MatrixAmber
import com.example.ui.theme.MatrixCyan
import com.example.ui.theme.MatrixRed

enum class LibraryFilter {
    ALL,
    FAVORITES,
    HQ_320K
}

@Composable
fun LibraryScreen(
    tracks: List<DownloadedTrack>,
    totalSizeBytes: Long,
    currentPlayingTrack: DownloadedTrack?,
    isPlaying: Boolean,
    onPlayTrack: (DownloadedTrack) -> Unit,
    onTogglePlayPause: () -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onDeleteTrack: (DownloadedTrack) -> Unit,
    onShareTrack: (DownloadedTrack) -> Unit,
    onNavigateToConvert: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalThemePalette.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(LibraryFilter.ALL) }
    var trackToDelete by remember { mutableStateOf<DownloadedTrack?>(null) }

    val filteredTracks = tracks.filter { track ->
        val matchesQuery = searchQuery.isBlank() ||
                track.title.contains(searchQuery, ignoreCase = true) ||
                track.artist.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            LibraryFilter.ALL -> true
            LibraryFilter.FAVORITES -> track.isFavorite
            LibraryFilter.HQ_320K -> track.bitrateKbps >= 320
        }

        matchesQuery && matchesFilter
    }

    val totalMb = totalSizeBytes.toDouble() / (1024.0 * 1024.0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Top Header
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            cornerSize = 8.dp,
            showBrackets = true
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = palette.primaryBright,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "MUSIC LIBRARY",
                            color = palette.primaryBright,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Unencrypted /Music Root Storage",
                        color = palette.textSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${tracks.size} TRACKS",
                        color = palette.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "%.2f MB TOTAL".format(totalMb),
                        color = MatrixCyan,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("library_search_input"),
            placeholder = {
                Text(
                    text = "Search tracks by title, artist, or tags...",
                    color = palette.textMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = palette.primary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = palette.textSecondary
                        )
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = palette.primaryBright,
                unfocusedBorderColor = palette.primaryDark,
                focusedTextColor = palette.textPrimary,
                unfocusedTextColor = palette.textPrimary,
                cursorColor = palette.primaryBright,
                focusedContainerColor = palette.background,
                unfocusedContainerColor = palette.background
            ),
            shape = RoundedCornerShape(6.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LibraryFilter.entries.forEach { filter ->
                val isSelected = filter == selectedFilter
                val label = when (filter) {
                    LibraryFilter.ALL -> "ALL (${tracks.size})"
                    LibraryFilter.FAVORITES -> "FAVORITES (${tracks.count { it.isFavorite }})"
                    LibraryFilter.HQ_320K -> "320 KBPS (${tracks.count { it.bitrateKbps >= 320 }})"
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .border(
                            1.dp,
                            if (isSelected) palette.primaryBright else palette.primaryDark,
                            RoundedCornerShape(6.dp)
                        )
                        .background(
                            if (isSelected) palette.primary.copy(alpha = 0.2f) else palette.background
                        )
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) palette.primaryBright else palette.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Track List / Empty State
        if (filteredTracks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CyberCard(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(16.dp),
                    cornerSize = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = palette.primary.copy(alpha = 0.7f),
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (tracks.isEmpty()) "NO CONVERTED TRACKS IN /MUSIC" else "NO MATCHING TRACKS FOUND",
                            color = palette.primaryBright,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (tracks.isEmpty()) {
                                "Paste any YouTube link in the Converter to drop unencrypted MP3s into your device Music folder."
                            } else {
                                "Try adjusting your search query or filter tags."
                            },
                            color = palette.textSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        if (tracks.isEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            NeonButton(
                                text = "OPEN CONVERTER",
                                icon = Icons.Default.Download,
                                onClick = onNavigateToConvert,
                                testTag = "empty_open_converter_button"
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredTracks, key = { it.id }) { track ->
                    val isCurrent = currentPlayingTrack?.id == track.id
                    TrackItemCard(
                        track = track,
                        isCurrentlyPlaying = isCurrent && isPlaying,
                        onPlayClick = {
                            if (isCurrent) {
                                onTogglePlayPause()
                            } else {
                                onPlayTrack(track)
                            }
                        },
                        onFavoriteClick = { onToggleFavorite(track.id) },
                        onShareClick = { onShareTrack(track) },
                        onDeleteClick = { trackToDelete = track }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    trackToDelete?.let { track ->
        AlertDialog(
            onDismissRequest = { trackToDelete = null },
            title = {
                Text(
                    text = "DELETE MP3 FILE?",
                    color = MatrixRed,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Permanently remove \"${track.title}\" and delete the unencrypted MP3 file from your Music folder?",
                    color = palette.textPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteTrack(track)
                        trackToDelete = null
                    },
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text(
                        text = "DELETE FILE",
                        color = MatrixRed,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { trackToDelete = null }) {
                    Text(
                        text = "CANCEL",
                        color = palette.textSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            containerColor = palette.surfaceElevated,
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
private fun TrackItemCard(
    track: DownloadedTrack,
    isCurrentlyPlaying: Boolean,
    onPlayClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val palette = LocalThemePalette.current
    CyberCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (isCurrentlyPlaying) palette.primaryBright else palette.primaryDark.copy(alpha = 0.6f),
        backgroundColor = if (isCurrentlyPlaying) palette.surfaceElevated else palette.surface.copy(alpha = 0.85f),
        cornerSize = 8.dp,
        showBrackets = isCurrentlyPlaying
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail / Cyber Icon
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(palette.background)
                    .border(1.dp, palette.primaryDark, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (track.thumbnailUrl.isNotBlank()) {
                    AsyncImage(
                        model = track.thumbnailUrl,
                        contentDescription = "Track Art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = palette.primaryBright,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Track details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    color = palette.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = track.artist,
                    color = palette.textSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(palette.primaryDark.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
                            .border(0.5.dp, palette.primary, RoundedCornerShape(3.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "${track.bitrateKbps}K",
                            color = MatrixCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = "${track.formattedDuration} • ${track.formattedSize}",
                        color = palette.textMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Quick Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Favorite
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (track.isFavorite) MatrixAmber else palette.textMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Share
                IconButton(
                    onClick = onShareClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = palette.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Delete
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = palette.textMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Play / Pause
                IconButton(
                    onClick = onPlayClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(palette.primary.copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, palette.primaryBright, CircleShape)
                        .testTag("track_play_${track.id}")
                ) {
                    Icon(
                        imageVector = if (isCurrentlyPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isCurrentlyPlaying) "Pause" else "Play",
                        tint = palette.primaryBright,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

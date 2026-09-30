package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.ui.theme.LocalThemePalette

@Composable
fun MiniPlayer(
    track: DownloadedTrack?,
    isPlaying: Boolean,
    progressFraction: Float,
    visualizerBars: List<Float>,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onExpandPlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (track == null) return
    val palette = LocalThemePalette.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .border(1.dp, palette.primary.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .background(palette.surfaceElevated.copy(alpha = 0.95f), RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = palette.primaryBright),
                onClick = onExpandPlayer
            )
            .testTag("mini_player_container")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Track playback progress strip
            LinearProgressIndicator(
                progress = { progressFraction.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp),
                color = palette.primaryBright,
                trackColor = palette.primaryDark.copy(alpha = 0.5f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail or fallback
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(palette.background)
                        .border(1.dp, palette.primaryDark, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (track.thumbnailUrl.isNotBlank()) {
                        AsyncImage(
                            model = track.thumbnailUrl,
                            contentDescription = "Cover",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = palette.primaryBright,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Track Info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = track.title,
                        color = palette.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${track.artist} • ${track.bitrateKbps}kbps",
                        color = palette.textSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Mini Waveform
                AudioWaveVisualizer(
                    bars = visualizerBars.take(8),
                    modifier = Modifier.height(20.dp),
                    barWidth = 2.5.dp,
                    spacing = 1.5.dp
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Play / Pause
                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier
                        .size(38.dp)
                        .background(palette.primary.copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, palette.primary.copy(alpha = 0.6f), CircleShape)
                        .testTag("mini_player_play_pause")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = palette.primaryBright,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Skip Next
                IconButton(
                    onClick = onSkipNext,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("mini_player_skip_next")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Track",
                        tint = palette.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

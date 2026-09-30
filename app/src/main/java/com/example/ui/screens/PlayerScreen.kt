package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.DownloadedTrack
import com.example.player.PlayerRepeatMode
import com.example.ui.components.AudioWaveVisualizer
import com.example.ui.components.CyberCard
import com.example.ui.theme.LocalThemePalette
import com.example.ui.theme.MatrixAmber
import com.example.ui.theme.MatrixCyan

@Composable
fun PlayerScreen(
    currentTrack: DownloadedTrack?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    playbackSpeed: Float,
    repeatMode: PlayerRepeatMode,
    isShuffle: Boolean,
    visualizerBars: List<Float>,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit,
    onSetPlaybackSpeed: (Float) -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onShareTrack: (DownloadedTrack) -> Unit,
    onNavigateToLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalThemePalette.current
    val scrollState = rememberScrollState()
    var isSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableStateOf(0f) }

    // Vinyl rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "disc_rotate")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val effectiveRotation = if (isPlaying) rotationAngle else 0f

    val positionSec = (if (isSeeking) seekPosition.toLong() else currentPositionMs) / 1000
    val durationSec = durationMs / 1000

    val posFormatted = "%02d:%02d".format(positionSec / 60, positionSec % 60)
    val durFormatted = "%02d:%02d".format(durationSec / 60, durationSec % 60)

    if (currentTrack == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                cornerSize = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = palette.primary,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "DECK IDLE // NO TRACK LOADED",
                        color = palette.primaryBright,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Select any audio track from the Library to initiate playback.",
                        color = palette.textSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Deck Top Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = palette.primaryBright,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AUDIO DECK",
                    color = palette.primaryBright,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            Box(
                modifier = Modifier
                    .background(palette.primaryDark.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .border(1.dp, palette.primary.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${currentTrack.bitrateKbps} KBPS CBR",
                    color = MatrixCyan,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Cyber Rotating Disc / Album Cover
        Box(
            modifier = Modifier
                .size(220.dp)
                .rotate(effectiveRotation),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.width / 2

                drawCircle(
                    color = palette.background,
                    radius = radius
                )
                drawCircle(
                    color = palette.primaryBright,
                    radius = radius,
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = palette.primaryDark,
                    radius = radius * 0.85f,
                    style = Stroke(width = 1.dp.toPx())
                )
                drawCircle(
                    color = palette.primary.copy(alpha = 0.35f),
                    radius = radius * 0.65f,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // Center Track Thumbnail
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, palette.primaryBright, CircleShape)
                    .background(palette.background)
            ) {
                if (currentTrack.thumbnailUrl.isNotBlank()) {
                    AsyncImage(
                        model = currentTrack.thumbnailUrl,
                        contentDescription = "Cover",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = palette.primaryBright,
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.Center)
                    )
                }
            }

            // Center Spindle
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(palette.primaryBright)
                    .border(2.dp, palette.background, CircleShape)
            )
        }

        // Dynamic Audio Spectrum Waveform
        AudioWaveVisualizer(
            bars = visualizerBars,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(36.dp),
            barColor = palette.primaryBright,
            barWidth = 5.dp,
            spacing = 3.dp
        )

        // Track Information
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentTrack.title,
                color = palette.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = currentTrack.artist,
                color = MatrixCyan,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Scrubbable Progress Slider
        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = if (isSeeking) seekPosition else currentPositionMs.toFloat(),
                onValueChange = {
                    isSeeking = true
                    seekPosition = it
                },
                onValueChangeFinished = {
                    onSeekTo(seekPosition.toLong())
                    isSeeking = false
                },
                valueRange = 0f..(durationMs.toFloat().coerceAtLeast(1f)),
                colors = SliderDefaults.colors(
                    thumbColor = palette.primaryBright,
                    activeTrackColor = palette.primaryBright,
                    inactiveTrackColor = palette.primaryDark
                ),
                modifier = Modifier.testTag("player_seek_slider")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = posFormatted,
                    color = palette.primaryBright,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = durFormatted,
                    color = palette.textSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Playback Transport Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Shuffle
            IconButton(
                onClick = onToggleShuffle,
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (isShuffle) palette.primaryBright else palette.textMuted,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Previous
            IconButton(
                onClick = onPreviousTrack,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous Track",
                    tint = palette.textPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Play / Pause (Large glowing CTA)
            IconButton(
                onClick = onTogglePlayPause,
                modifier = Modifier
                    .size(64.dp)
                    .background(palette.primary.copy(alpha = 0.2f), CircleShape)
                    .border(2.dp, palette.primaryBright, CircleShape)
                    .testTag("player_play_pause_button")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = palette.primaryBright,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Next
            IconButton(
                onClick = onNextTrack,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next Track",
                    tint = palette.textPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Repeat Mode
            IconButton(
                onClick = onToggleRepeat,
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    imageVector = when (repeatMode) {
                        PlayerRepeatMode.ONE -> Icons.Default.RepeatOne
                        else -> Icons.Default.Repeat
                    },
                    contentDescription = "Repeat",
                    tint = if (repeatMode != PlayerRepeatMode.OFF) palette.primaryBright else palette.textMuted,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Speed & Secondary Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Speed Chips
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(0.75f, 1.0f, 1.25f, 1.5f).forEach { speed ->
                    val isSelected = playbackSpeed == speed
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .border(
                                1.dp,
                                if (isSelected) palette.primaryBright else palette.primaryDark,
                                RoundedCornerShape(4.dp)
                            )
                            .background(if (isSelected) palette.primary.copy(alpha = 0.2f) else palette.background)
                            .clickable { onSetPlaybackSpeed(speed) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${speed}x",
                            color = if (isSelected) palette.primaryBright else palette.textSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Favorite & Share
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = { onToggleFavorite(currentTrack.id) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (currentTrack.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (currentTrack.isFavorite) MatrixAmber else palette.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { onShareTrack(currentTrack) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = palette.primaryBright,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

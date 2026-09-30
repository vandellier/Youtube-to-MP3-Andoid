package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AudioQuality
import com.example.data.model.ConversionState
import com.example.data.model.DownloadedTrack
import com.example.data.model.TerminalLogEntry
import com.example.data.model.YouTubeVideoInfo
import com.example.ui.components.CyberCard
import com.example.ui.components.NeonButton
import com.example.ui.components.TerminalLogView
import com.example.ui.theme.ColorTheme
import com.example.ui.theme.LocalThemePalette
import com.example.ui.theme.MatrixAmber
import com.example.ui.theme.MatrixCyan
import com.example.ui.theme.MatrixRed

private data class QuickPreset(
    val title: String,
    val artist: String,
    val url: String
)

private val QUICK_PRESETS = listOf(
    QuickPreset("Cyberpunk 2077 Theme", "Rebel Path [Synth]", "https://youtu.be/9ayYeLL385Y"),
    QuickPreset("Matrix Resurrections", "White Rabbit [Remix]", "https://youtu.be/9ix7TUGVYIo"),
    QuickPreset("Synthwave Radio", "Lofi Hacking Beats 24/7", "https://youtu.be/5qap5aO4i9A"),
    QuickPreset("Underground Cyber Bass", "80s Retrowave Drive", "https://youtu.be/kJQP7kiw5Fk")
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConvertScreen(
    urlInput: String,
    onUrlChange: (String) -> Unit,
    conversionState: ConversionState,
    selectedQuality: AudioQuality,
    onQualitySelected: (AudioQuality) -> Unit,
    selectedTheme: ColorTheme,
    onThemeSelected: (ColorTheme) -> Unit,
    logs: List<TerminalLogEntry>,
    onClearLogs: () -> Unit,
    onStartConversion: () -> Unit,
    onPlayTrack: (DownloadedTrack) -> Unit,
    onShareTrack: (DownloadedTrack) -> Unit,
    onNavigateToLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val palette = LocalThemePalette.current

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_online")
    val onlineGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Theme Quick Switcher Row
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            cornerSize = 6.dp,
            showBrackets = false
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = palette.primaryBright,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "COLOR SCHEME",
                            color = palette.primaryBright,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Text(
                        text = selectedTheme.displayName,
                        color = palette.textSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ColorTheme.entries.forEach { theme ->
                        val isSelected = theme == selectedTheme
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) theme.palette.primaryBright else theme.palette.primaryDark,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .background(
                                    if (isSelected) theme.palette.primary.copy(alpha = 0.25f) else theme.palette.background
                                )
                                .clickable { onThemeSelected(theme) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(theme.palette.primaryBright, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = when (theme) {
                                        ColorTheme.MATRIX_GREEN -> "GREEN"
                                        ColorTheme.HOT_PINK -> "PINK"
                                        ColorTheme.CRIMSON_RED -> "RED"
                                        ColorTheme.CYBER_ORANGE -> "ORANGE"
                                    },
                                    color = if (isSelected) theme.palette.primaryBright else theme.palette.textSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // Underground Status Header
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            cornerSize = 8.dp,
            showBrackets = true
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .background(
                                    palette.primaryBright.copy(alpha = onlineGlow),
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "UNDERGROUND // PROTOCOL",
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
                            text = "AUTO-DROP /Music",
                            color = MatrixCyan,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "YouTube to MP3 Converter",
                    color = palette.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Text(
                    text = "Transcode YouTube audio to unencrypted MP3s and automatically drop them directly into your device root Music folder.",
                    color = palette.textSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Quick Preset Targets
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.HighQuality,
                    contentDescription = null,
                    tint = palette.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "PRESET TARGETS // QUICK TEST",
                    color = palette.textSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QUICK_PRESETS.forEach { preset ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, palette.primaryDark, RoundedCornerShape(6.dp))
                            .background(palette.surfaceElevated)
                            .clickable {
                                onUrlChange(preset.url)
                                focusManager.clearFocus()
                            }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "▶ ",
                                color = palette.primaryBright,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = preset.title,
                                color = palette.textPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // URL Input Field Box
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            cornerSize = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Text(
                    text = "TARGET YOUTUBE URL / VIDEO ID",
                    color = palette.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = urlInput,
                    onValueChange = onUrlChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("youtube_url_input"),
                    placeholder = {
                        Text(
                            text = "https://youtube.com/watch?v=... or youtu.be/...",
                            color = palette.textMuted,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Audiotrack,
                            contentDescription = null,
                            tint = palette.primaryBright
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (urlInput.isNotBlank()) {
                                IconButton(
                                    onClick = { onUrlChange("") },
                                    modifier = Modifier.testTag("clear_url_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = palette.textSecondary
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    if (clipboard?.hasPrimaryClip() == true) {
                                        val clipData = clipboard.primaryClip
                                        if (clipData != null && clipData.itemCount > 0) {
                                            val text = clipData.getItemAt(0).coerceToText(context).toString()
                                            onUrlChange(text)
                                        }
                                    }
                                },
                                modifier = Modifier.testTag("paste_url_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = "Paste from clipboard",
                                    tint = palette.primaryBright
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
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    shape = RoundedCornerShape(6.dp)
                )

                // Quality Selector
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "TARGET AUDIO BITRATE",
                    color = palette.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AudioQuality.entries.forEach { quality ->
                        val isSelected = quality == selectedQuality
                        val borderCol = if (isSelected) palette.primaryBright else palette.primaryDark
                        val bgCol = if (isSelected) palette.primary.copy(alpha = 0.2f) else palette.background

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, borderCol, RoundedCornerShape(6.dp))
                                .background(bgCol)
                                .clickable { onQualitySelected(quality) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${quality.bitrateKbps}K",
                                    color = if (isSelected) palette.primaryBright else palette.textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = quality.badge,
                                    color = if (isSelected) MatrixCyan else palette.textMuted,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // Preview metadata card (when video info is available or fetching)
        AnimatedVisibility(
            visible = conversionState is ConversionState.FetchingMetadata ||
                    conversionState is ConversionState.Ready ||
                    conversionState is ConversionState.Converting,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut()
        ) {
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                cornerSize = 8.dp
            ) {
                when (conversionState) {
                    is ConversionState.FetchingMetadata -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = palette.primaryBright,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "CONNECTING TO STREAM...",
                                    color = palette.primaryBright,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Extracting video details & audio stream parameters",
                                    color = palette.textSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    is ConversionState.Ready -> {
                        VideoPreviewContent(info = conversionState.videoInfo)
                    }

                    is ConversionState.Converting -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            VideoPreviewContent(info = conversionState.videoInfo)

                            Spacer(modifier = Modifier.height(14.dp))

                            // Conversion Progress Metrics
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = conversionState.stage,
                                    color = palette.primaryBright,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${(conversionState.progress * 100).toInt()}%",
                                    color = MatrixCyan,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LinearProgressIndicator(
                                progress = { conversionState.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .testTag("conversion_progress_bar"),
                                color = palette.primaryBright,
                                trackColor = palette.background
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${"%.2f".format(conversionState.currentBytes / (1024.0 * 1024.0))} MB / ${"%.2f".format(conversionState.totalBytes / (1024.0 * 1024.0))} MB",
                                    color = palette.textSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${conversionState.speedKbps} KB/S",
                                    color = MatrixAmber,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    else -> Unit
                }
            }
        }

        // Success State Card
        AnimatedVisibility(
            visible = conversionState is ConversionState.Success,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut()
        ) {
            if (conversionState is ConversionState.Success) {
                val track = conversionState.track
                CyberCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = palette.primaryBright,
                    cornerSize = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = palette.primaryBright,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "CONVERTED & SAVED TO /MUSIC",
                                    color = palette.primaryBright,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Dropped unencrypted in root Music folder (${track.formattedSize})",
                                    color = palette.textSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = track.title,
                            color = palette.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${track.artist} • ${track.bitrateKbps}kbps MP3 • ${track.formattedDuration}",
                            color = MatrixCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            NeonButton(
                                text = "PLAY NOW",
                                icon = Icons.Default.PlayArrow,
                                onClick = { onPlayTrack(track) },
                                modifier = Modifier.weight(1f),
                                testTag = "success_play_button"
                            )

                            NeonButton(
                                text = "MUSIC LIST",
                                icon = Icons.Default.Folder,
                                onClick = onNavigateToLibrary,
                                isPrimary = false,
                                modifier = Modifier.weight(0.9f),
                                testTag = "success_vault_button"
                            )

                            IconButton(
                                onClick = { onShareTrack(track) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .border(1.dp, palette.primaryDark, RoundedCornerShape(8.dp))
                                    .background(palette.surfaceElevated)
                                    .testTag("success_share_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share MP3",
                                    tint = palette.primaryBright,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Error message card
        AnimatedVisibility(
            visible = conversionState is ConversionState.Error,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut()
        ) {
            if (conversionState is ConversionState.Error) {
                CyberCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = MatrixRed,
                    backgroundColor = palette.surface.copy(alpha = 0.95f),
                    cornerSize = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "SYSTEM FAULT // ERROR",
                            color = MatrixRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = conversionState.message,
                            color = palette.textPrimary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Action CTA: Convert & Download
        val isConverting = conversionState is ConversionState.Converting
        val isFetching = conversionState is ConversionState.FetchingMetadata

        NeonButton(
            text = if (isConverting) "DOWNLOADING & TRANSCODING..." else if (isFetching) "ANALYZING TARGET..." else "CONVERT & DROP INTO /MUSIC",
            icon = if (isConverting) Icons.Default.Sync else Icons.Default.Download,
            onClick = {
                focusManager.clearFocus()
                onStartConversion()
            },
            enabled = urlInput.isNotBlank() && !isConverting && !isFetching,
            modifier = Modifier.fillMaxWidth(),
            testTag = "execute_conversion_button"
        )

        // Terminal Log Console View
        TerminalLogView(
            logs = logs,
            onClearLogs = onClearLogs,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun VideoPreviewContent(info: YouTubeVideoInfo) {
    val palette = LocalThemePalette.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(6.dp))
                .border(1.dp, palette.primaryDark, RoundedCornerShape(6.dp))
                .background(palette.background)
        ) {
            AsyncImage(
                model = info.thumbnailUrl,
                contentDescription = "Video Thumbnail",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = info.title,
                color = palette.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = info.authorName,
                color = MatrixCyan,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "EST: ${info.formattedEstimatedSize}",
                    color = MatrixAmber,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "DUR: ${info.formattedDuration}",
                    color = palette.textSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

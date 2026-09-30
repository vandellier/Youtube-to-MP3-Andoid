package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CyberCard
import com.example.ui.components.NeonButton
import com.example.ui.theme.ColorTheme
import com.example.ui.theme.LocalThemePalette
import com.example.ui.theme.MatrixCyan

@Composable
fun SystemScreen(
    selectedTheme: ColorTheme,
    onThemeSelected: (ColorTheme) -> Unit,
    matrixRainEnabled: Boolean,
    onToggleMatrixRain: (Boolean) -> Unit,
    matrixRainSpeed: Float,
    onMatrixRainSpeedChange: (Float) -> Unit,
    scanlinesEnabled: Boolean,
    onToggleScanlines: (Boolean) -> Unit,
    totalTrackCount: Int,
    totalSizeBytes: Long,
    ffmpegEngineVersion: String = "v6.0.2-kit",
    ffmpegEngineStatus: String = "READY & OPERATIONAL",
    lastUpdateTimestamp: String = "Just now",
    isAutoUpdateEnabled: Boolean = true,
    isTestingFFmpeg: Boolean = false,
    isUpdatingFFmpeg: Boolean = false,
    onTestFFmpegEngine: () -> Unit = {},
    onTriggerFFmpegUpdate: () -> Unit = {},
    onToggleAutoUpdateTrigger: (Boolean) -> Unit = {},
    onTestAudioDriver: () -> Unit,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalThemePalette.current
    val scrollState = rememberScrollState()
    val totalMb = totalSizeBytes.toDouble() / (1024.0 * 1024.0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = palette.primaryBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TERMINAL CONFIG // SYSTEM",
                        color = palette.primaryBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Matrix color themes, audio pipeline, and device Music folder configuration.",
                    color = palette.textSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Color Scheme Choices Card
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            cornerSize = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = palette.primaryBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "COLOR SCHEME CHOICES",
                        color = palette.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = "Choose your cyberpunk color theme. Persists across app launches.",
                    color = palette.textSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                // Theme Options List
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ColorTheme.entries.forEach { theme ->
                        val isSelected = theme == selectedTheme
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) theme.palette.primaryBright else theme.palette.primaryDark,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(
                                    if (isSelected) theme.palette.primary.copy(alpha = 0.2f) else theme.palette.background
                                )
                                .clickable { onThemeSelected(theme) }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Color swatch pill
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(theme.palette.primaryBright)
                                            .border(1.5.dp, theme.palette.background, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = theme.displayName,
                                            color = if (isSelected) theme.palette.primaryBright else theme.palette.textPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = when (theme) {
                                                ColorTheme.MATRIX_GREEN -> "Classic Cyberpunk Matrix Terminal"
                                                ColorTheme.HOT_PINK -> "Neon Hot Pink & Deep Obsidian Black"
                                                ColorTheme.CRIMSON_RED -> "Crimson Hacker Red & Black"
                                                ColorTheme.CYBER_ORANGE -> "Retro Amber Terminal & Black"
                                            },
                                            color = theme.palette.textSecondary,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Active",
                                        tint = theme.palette.primaryBright,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Matrix Graphics Settings
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            cornerSize = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "VISUAL MATRIX INTERFACE",
                    color = palette.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                // Matrix Rain Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Digital Rain Stream",
                            color = palette.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Falling cipher glyphs matching active theme color",
                            color = palette.textSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Switch(
                        checked = matrixRainEnabled,
                        onCheckedChange = onToggleMatrixRain,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = palette.primaryBright,
                            checkedTrackColor = palette.primaryDark,
                            uncheckedThumbColor = palette.textSecondary,
                            uncheckedTrackColor = palette.background
                        ),
                        modifier = Modifier.testTag("toggle_matrix_rain")
                    )
                }

                if (matrixRainEnabled) {
                    // Rain Speed Slider
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Stream Velocity",
                                color = palette.textSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "%.1fx".format(matrixRainSpeed),
                                color = MatrixCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = matrixRainSpeed,
                            onValueChange = onMatrixRainSpeedChange,
                            valueRange = 0.5f..2.5f,
                            colors = SliderDefaults.colors(
                                thumbColor = palette.primaryBright,
                                activeTrackColor = palette.primaryBright,
                                inactiveTrackColor = palette.primaryDark
                            )
                        )
                    }
                }

                // CRT Scanlines Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CRT Terminal Scanlines",
                            color = palette.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Simulate retro cathode ray tube terminal phosphor lines",
                            color = palette.textSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Switch(
                        checked = scanlinesEnabled,
                        onCheckedChange = onToggleScanlines,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = palette.primaryBright,
                            checkedTrackColor = palette.primaryDark,
                            uncheckedThumbColor = palette.textSecondary,
                            uncheckedTrackColor = palette.background
                        ),
                        modifier = Modifier.testTag("toggle_scanlines")
                    )
                }
            }
        }

        // Storage & Telemetry
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            cornerSize = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "STORAGE & FILE SYSTEM (UNENCRYPTED)",
                    color = palette.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Storage Drop Location",
                        color = palette.textSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "/storage/emulated/0/Music",
                        color = palette.primaryBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Encryption Status",
                        color = palette.textSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "DISABLED (Raw Standard MP3)",
                        color = MatrixCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Converted Tracks Count",
                        color = palette.textSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "$totalTrackCount MP3 Files",
                        color = palette.textPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Music Footprint",
                        color = palette.textSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "%.2f MB".format(totalMb),
                        color = MatrixCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Android System Indexer",
                        color = palette.textSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "ACTIVE (Visible to All Music Apps)",
                        color = palette.primaryBright,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // FFmpegKit On-Device Transcoding Specs
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            cornerSize = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FFMPEGKIT AUDIO ENCODING ENGINE",
                        color = palette.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = ffmpegEngineStatus,
                        color = palette.primaryBright,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Engine Version",
                        color = palette.textSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = ffmpegEngineVersion,
                        color = palette.primaryBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Encoder Architecture",
                        color = palette.textSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "FFmpegKit (libmp3lame)",
                        color = palette.primaryBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Hardware Demuxer",
                        color = palette.textSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "MediaCodec + MediaExtractor",
                        color = MatrixCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Container Support",
                        color = palette.textSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "WebM, MP4, MKV, M4A, Opus",
                        color = palette.textPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Target Standard",
                        color = palette.textSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "MPEG-1 Layer III / ID3v2.3",
                        color = MatrixCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Last Synchronized",
                        color = palette.textSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = lastUpdateTimestamp,
                        color = palette.textPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Automated Update Trigger Card
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            cornerSize = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "FFMPEG & CODEC AUTO-UPDATE TRIGGER",
                    color = palette.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "24-Hour Automated Trigger",
                            color = palette.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Schedules background AlarmManager to verify codecs & decipher rules",
                            color = palette.textSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Switch(
                        checked = isAutoUpdateEnabled,
                        onCheckedChange = onToggleAutoUpdateTrigger,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = palette.primaryBright,
                            checkedTrackColor = palette.primaryDark,
                            uncheckedThumbColor = palette.textSecondary,
                            uncheckedTrackColor = palette.background
                        ),
                        modifier = Modifier.testTag("toggle_auto_update")
                    )
                }

                NeonButton(
                    text = if (isUpdatingFFmpeg) "CHECKING & UPDATING..." else "TRIGGER FFMPEG UPDATE NOW",
                    icon = Icons.Default.Sync,
                    onClick = onTriggerFFmpegUpdate,
                    isPrimary = false,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "trigger_update_button"
                )
            }
        }

        // Diagnostics Actions
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            cornerSize = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "AUDIO DRIVER DIAGNOSTICS",
                    color = palette.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                NeonButton(
                    text = if (isTestingFFmpeg) "BENCHMARKING FFMPEG..." else "TEST & BENCHMARK FFMPEG ENCODER",
                    icon = Icons.Default.Bolt,
                    onClick = onTestFFmpegEngine,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "test_ffmpeg_button"
                )

                NeonButton(
                    text = "TEST AUDIO SYNTH DRIVER",
                    icon = Icons.Default.VolumeUp,
                    onClick = onTestAudioDriver,
                    isPrimary = false,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "test_audio_driver_button"
                )

                NeonButton(
                    text = "PURGE LOG HISTORY",
                    icon = Icons.Default.Refresh,
                    onClick = onClearLogs,
                    isPrimary = false,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "purge_logs_button"
                )
            }
        }

        // About / Specs
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            cornerSize = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Underground YouTube to MP3 Converter",
                    color = palette.primaryBright,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Active Theme: ${selectedTheme.displayName}",
                    color = palette.textSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Direct Drop: /storage/emulated/0/Music",
                    color = palette.textMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

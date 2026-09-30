package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.AppScreen
import com.example.MainViewModel
import com.example.data.model.DownloadedTrack
import com.example.ui.components.CRTScanlineOverlay
import com.example.ui.components.MatrixRainCanvas
import com.example.ui.components.MiniPlayer
import com.example.ui.screens.ConvertScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.SystemScreen
import com.example.ui.theme.LocalThemePalette
import com.example.ui.theme.MatrixCyan
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val palette = LocalThemePalette.current
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val selectedTheme by viewModel.selectedTheme.collectAsStateWithLifecycle()

    val tracks by viewModel.tracks.collectAsStateWithLifecycle()
    val totalSizeBytes by viewModel.totalSizeBytes.collectAsStateWithLifecycle()
    val trackCount by viewModel.trackCount.collectAsStateWithLifecycle()

    val urlInput by viewModel.urlInput.collectAsStateWithLifecycle()
    val selectedQuality by viewModel.selectedQuality.collectAsStateWithLifecycle()
    val conversionState by viewModel.conversionState.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()

    val matrixRainEnabled by viewModel.matrixRainEnabled.collectAsStateWithLifecycle()
    val matrixRainSpeed by viewModel.matrixRainSpeed.collectAsStateWithLifecycle()
    val scanlinesEnabled by viewModel.scanlinesEnabled.collectAsStateWithLifecycle()

    // FFmpeg Engine and Trigger State
    val ffmpegEngineVersion by viewModel.ffmpegEngineVersion.collectAsStateWithLifecycle()
    val ffmpegEngineStatus by viewModel.ffmpegEngineStatus.collectAsStateWithLifecycle()
    val lastUpdateTimestamp by viewModel.lastUpdateTimestamp.collectAsStateWithLifecycle()
    val isAutoUpdateEnabled by viewModel.isAutoUpdateEnabled.collectAsStateWithLifecycle()
    val isTestingFFmpeg by viewModel.isTestingFFmpeg.collectAsStateWithLifecycle()
    val isUpdatingFFmpeg by viewModel.isUpdatingFFmpeg.collectAsStateWithLifecycle()

    // Player state
    val currentPlayingTrack by viewModel.audioPlayer.currentTrack.collectAsStateWithLifecycle()
    val isPlaying by viewModel.audioPlayer.isPlaying.collectAsStateWithLifecycle()
    val currentPositionMs by viewModel.audioPlayer.currentPositionMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.audioPlayer.durationMs.collectAsStateWithLifecycle()
    val playbackSpeed by viewModel.audioPlayer.playbackSpeed.collectAsStateWithLifecycle()
    val repeatMode by viewModel.audioPlayer.repeatMode.collectAsStateWithLifecycle()
    val isShuffle by viewModel.audioPlayer.isShuffle.collectAsStateWithLifecycle()
    val visualizerBars by viewModel.audioPlayer.visualizerBars.collectAsStateWithLifecycle()

    // Back handler: return to CONVERT screen if on secondary screen
    BackHandler(enabled = currentScreen != AppScreen.CONVERT) {
        viewModel.setCurrentScreen(AppScreen.CONVERT)
    }

    val onShareTrack: (DownloadedTrack) -> Unit = { track ->
        shareAudioTrack(context, track)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        // Digital Rain Canvas Background with active theme color
        if (matrixRainEnabled) {
            MatrixRainCanvas(
                alpha = 0.28f,
                speedMultiplier = matrixRainSpeed,
                rainColor = palette.rainColor,
                headColor = palette.primaryBright,
                modifier = Modifier.fillMaxSize()
            )
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "UNDERGROUND // YT-MP3",
                                color = palette.primaryBright,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = when (currentScreen) {
                                    AppScreen.CONVERT -> "DROP: /storage/emulated/0/Music"
                                    AppScreen.VAULT -> "MUSIC LIBRARY (${tracks.size} FILES)"
                                    AppScreen.PLAYER -> "AUDIO DECK PLAYBACK"
                                    AppScreen.SYSTEM -> "SCHEME: ${selectedTheme.displayName}"
                                },
                                color = MatrixCyan,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = palette.surface.copy(alpha = 0.88f)
                    ),
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = palette.primaryDark.copy(alpha = 0.5f)
                    )
                )
            },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    // Persistent Mini-Player (shown when track exists and not on Player tab)
                    if (currentPlayingTrack != null && currentScreen != AppScreen.PLAYER) {
                        val progressFraction = if (durationMs > 0) {
                            currentPositionMs.toFloat() / durationMs.toFloat()
                        } else 0f

                        MiniPlayer(
                            track = currentPlayingTrack,
                            isPlaying = isPlaying,
                            progressFraction = progressFraction,
                            visualizerBars = visualizerBars,
                            onTogglePlayPause = { viewModel.togglePlayPause() },
                            onSkipNext = { viewModel.nextTrack() },
                            onExpandPlayer = { viewModel.setCurrentScreen(AppScreen.PLAYER) }
                        )
                    }

                    // Navigation Bar
                    NavigationBar(
                        containerColor = palette.surface.copy(alpha = 0.95f),
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(width = 1.dp, color = palette.primaryDark.copy(alpha = 0.6f))
                    ) {
                        // CONVERT Tab
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.CONVERT,
                            onClick = { viewModel.setCurrentScreen(AppScreen.CONVERT) },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen == AppScreen.CONVERT) Icons.Filled.Download else Icons.Outlined.Download,
                                    contentDescription = "Convert"
                                )
                            },
                            label = {
                                Text(
                                    text = "CONVERT",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = palette.primaryBright,
                                indicatorColor = palette.primaryBright,
                                unselectedIconColor = palette.textSecondary,
                                unselectedTextColor = palette.textMuted
                            ),
                            modifier = Modifier.testTag("nav_convert")
                        )

                        // MUSIC / LIBRARY Tab
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.VAULT,
                            onClick = { viewModel.setCurrentScreen(AppScreen.VAULT) },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (tracks.isNotEmpty()) {
                                            Badge(
                                                containerColor = palette.primaryBright,
                                                contentColor = Color.Black
                                            ) {
                                                Text(
                                                    text = tracks.size.toString(),
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (currentScreen == AppScreen.VAULT) Icons.Filled.Folder else Icons.Outlined.Folder,
                                        contentDescription = "Music"
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = "MUSIC",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = palette.primaryBright,
                                indicatorColor = palette.primaryBright,
                                unselectedIconColor = palette.textSecondary,
                                unselectedTextColor = palette.textMuted
                            ),
                            modifier = Modifier.testTag("nav_vault")
                        )

                        // PLAYER Tab
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.PLAYER,
                            onClick = { viewModel.setCurrentScreen(AppScreen.PLAYER) },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen == AppScreen.PLAYER) Icons.Filled.MusicNote else Icons.Outlined.MusicNote,
                                    contentDescription = "Player"
                                )
                            },
                            label = {
                                Text(
                                    text = "DECK",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = palette.primaryBright,
                                indicatorColor = palette.primaryBright,
                                unselectedIconColor = palette.textSecondary,
                                unselectedTextColor = palette.textMuted
                            ),
                            modifier = Modifier.testTag("nav_deck")
                        )

                        // SYSTEM Tab
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.SYSTEM,
                            onClick = { viewModel.setCurrentScreen(AppScreen.SYSTEM) },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen == AppScreen.SYSTEM) Icons.Filled.Settings else Icons.Outlined.Settings,
                                    contentDescription = "System"
                                )
                            },
                            label = {
                                Text(
                                    text = "SYSTEM",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = palette.primaryBright,
                                indicatorColor = palette.primaryBright,
                                unselectedIconColor = palette.textSecondary,
                                unselectedTextColor = palette.textMuted
                            ),
                            modifier = Modifier.testTag("nav_system")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "screen_switch"
                ) { screen ->
                    when (screen) {
                        AppScreen.CONVERT -> {
                            ConvertScreen(
                                urlInput = urlInput,
                                onUrlChange = viewModel::onUrlInputChanged,
                                conversionState = conversionState,
                                selectedQuality = selectedQuality,
                                onQualitySelected = viewModel::onQualitySelected,
                                selectedTheme = selectedTheme,
                                onThemeSelected = viewModel::setTheme,
                                logs = logs,
                                onClearLogs = viewModel::clearLogs,
                                onStartConversion = viewModel::startConversion,
                                onPlayTrack = { track ->
                                    viewModel.playTrack(track)
                                    viewModel.setCurrentScreen(AppScreen.PLAYER)
                                },
                                onShareTrack = onShareTrack,
                                onNavigateToLibrary = { viewModel.setCurrentScreen(AppScreen.VAULT) }
                            )
                        }

                        AppScreen.VAULT -> {
                            LibraryScreen(
                                tracks = tracks,
                                totalSizeBytes = totalSizeBytes,
                                currentPlayingTrack = currentPlayingTrack,
                                isPlaying = isPlaying,
                                onPlayTrack = { track ->
                                    viewModel.playTrack(track)
                                    viewModel.setCurrentScreen(AppScreen.PLAYER)
                                },
                                onTogglePlayPause = viewModel::togglePlayPause,
                                onToggleFavorite = viewModel::toggleFavorite,
                                onDeleteTrack = viewModel::deleteTrack,
                                onShareTrack = onShareTrack,
                                onNavigateToConvert = { viewModel.setCurrentScreen(AppScreen.CONVERT) }
                            )
                        }

                        AppScreen.PLAYER -> {
                            PlayerScreen(
                                currentTrack = currentPlayingTrack,
                                isPlaying = isPlaying,
                                currentPositionMs = currentPositionMs,
                                durationMs = durationMs,
                                playbackSpeed = playbackSpeed,
                                repeatMode = repeatMode,
                                isShuffle = isShuffle,
                                visualizerBars = visualizerBars,
                                onTogglePlayPause = viewModel::togglePlayPause,
                                onSeekTo = viewModel::seekTo,
                                onNextTrack = viewModel::nextTrack,
                                onPreviousTrack = viewModel::previousTrack,
                                onSetPlaybackSpeed = viewModel::setPlaybackSpeed,
                                onToggleRepeat = viewModel::toggleRepeat,
                                onToggleShuffle = viewModel::toggleShuffle,
                                onToggleFavorite = viewModel::toggleFavorite,
                                onShareTrack = onShareTrack,
                                onNavigateToLibrary = { viewModel.setCurrentScreen(AppScreen.VAULT) }
                            )
                        }

                        AppScreen.SYSTEM -> {
                            SystemScreen(
                                selectedTheme = selectedTheme,
                                onThemeSelected = viewModel::setTheme,
                                matrixRainEnabled = matrixRainEnabled,
                                onToggleMatrixRain = viewModel::toggleMatrixRain,
                                matrixRainSpeed = matrixRainSpeed,
                                onMatrixRainSpeedChange = viewModel::setMatrixRainSpeed,
                                scanlinesEnabled = scanlinesEnabled,
                                onToggleScanlines = viewModel::toggleScanlines,
                                totalTrackCount = trackCount,
                                totalSizeBytes = totalSizeBytes,
                                ffmpegEngineVersion = ffmpegEngineVersion,
                                ffmpegEngineStatus = ffmpegEngineStatus,
                                lastUpdateTimestamp = lastUpdateTimestamp,
                                isAutoUpdateEnabled = isAutoUpdateEnabled,
                                isTestingFFmpeg = isTestingFFmpeg,
                                isUpdatingFFmpeg = isUpdatingFFmpeg,
                                onTestFFmpegEngine = viewModel::testFFmpegEngine,
                                onTriggerFFmpegUpdate = viewModel::triggerFFmpegUpdate,
                                onToggleAutoUpdateTrigger = viewModel::toggleAutoUpdateTrigger,
                                onTestAudioDriver = viewModel::testAudioDriver,
                                onClearLogs = viewModel::clearLogs
                            )
                        }
                    }
                }
            }
        }

        // Retro CRT Scanlines Overlay
        if (scanlinesEnabled) {
            CRTScanlineOverlay(
                modifier = Modifier.fillMaxSize(),
                scanlineAlpha = 0.06f
            )
        }
    }
}

private fun shareAudioTrack(context: Context, track: DownloadedTrack) {
    try {
        val file = File(track.localFilePath)
        if (!file.exists()) {
            return
        }

        val uri: Uri = try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (_: Exception) {
            Uri.fromFile(file)
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "audio/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TITLE, track.title)
            putExtra(Intent.EXTRA_SUBJECT, "${track.title} - ${track.artist}")
            putExtra(Intent.EXTRA_TEXT, "MP3 track: ${track.title} (${track.bitrateKbps}kbps)")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Export MP3: ${track.title}"))
    } catch (_: Exception) {
        val textIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Underground YouTube to MP3 Converter - ${track.title} (${track.bitrateKbps}kbps)")
        }
        context.startActivity(Intent.createChooser(textIntent, "Share Track"))
    }
}

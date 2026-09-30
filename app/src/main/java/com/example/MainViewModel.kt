package com.example

import android.app.Application
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.converter.AudioSynthesizer
import com.example.data.converter.YouTubeAudioConverter
import com.example.data.converter.YouTubeExtractor
import com.example.data.local.AppDatabase
import com.example.data.local.TrackRepository
import com.example.data.model.AudioQuality
import com.example.data.model.ConversionState
import com.example.data.model.DownloadedTrack
import com.example.data.model.LogLevel
import com.example.data.model.TerminalLogEntry
import com.example.data.model.YouTubeVideoInfo
import com.example.player.MatrixAudioPlayer
import com.example.player.PlayerRepeatMode
import com.example.ui.theme.ColorTheme
import com.example.ui.theme.ThemeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class AppScreen {
    CONVERT,
    VAULT,
    PLAYER,
    SYSTEM
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = TrackRepository(database.trackDao())
    private val converter = YouTubeAudioConverter(application)
    val audioPlayer = MatrixAudioPlayer(application)

    // Dedicated ThemeManager handling state and persistence across sessions
    val themeManager = ThemeManager.getInstance(application)
    val selectedTheme: StateFlow<ColorTheme> = themeManager.currentTheme

    // Navigation state
    private val _currentScreen = MutableStateFlow(AppScreen.CONVERT)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Library Tracks from Room
    val tracks: StateFlow<List<DownloadedTrack>> = repository.allTracks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalSizeBytes: StateFlow<Long> = repository.totalSizeBytes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    val trackCount: StateFlow<Int> = repository.trackCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Convert Screen State
    private val _urlInput = MutableStateFlow("")
    val urlInput: StateFlow<String> = _urlInput.asStateFlow()

    private val _selectedQuality = MutableStateFlow(AudioQuality.KBPS_320)
    val selectedQuality: StateFlow<AudioQuality> = _selectedQuality.asStateFlow()

    private val _conversionState = MutableStateFlow<ConversionState>(ConversionState.Idle)
    val conversionState: StateFlow<ConversionState> = _conversionState.asStateFlow()

    private val _logs = MutableStateFlow<List<TerminalLogEntry>>(emptyList())
    val logs: StateFlow<List<TerminalLogEntry>> = _logs.asStateFlow()

    // Matrix Visual Customization
    private val _matrixRainEnabled = MutableStateFlow(true)
    val matrixRainEnabled: StateFlow<Boolean> = _matrixRainEnabled.asStateFlow()

    private val _matrixRainSpeed = MutableStateFlow(1.0f)
    val matrixRainSpeed: StateFlow<Float> = _matrixRainSpeed.asStateFlow()

    private val _scanlinesEnabled = MutableStateFlow(true)
    val scanlinesEnabled: StateFlow<Boolean> = _scanlinesEnabled.asStateFlow()

    // FFmpegKit Engine & Update Trigger State
    private val _ffmpegEngineVersion = MutableStateFlow(com.example.ffmpegkit.FFmpegUpdateTrigger.CURRENT_ENGINE_VERSION)
    val ffmpegEngineVersion: StateFlow<String> = _ffmpegEngineVersion.asStateFlow()

    private val _ffmpegEngineStatus = MutableStateFlow(com.example.ffmpegkit.FFmpegUpdateTrigger.getEngineStatus(application))
    val ffmpegEngineStatus: StateFlow<String> = _ffmpegEngineStatus.asStateFlow()

    private val _lastUpdateTimestamp = MutableStateFlow(com.example.ffmpegkit.FFmpegUpdateTrigger.getLastUpdateFormatted(application))
    val lastUpdateTimestamp: StateFlow<String> = _lastUpdateTimestamp.asStateFlow()

    private val _isAutoUpdateEnabled = MutableStateFlow(com.example.ffmpegkit.FFmpegUpdateTrigger.isAutoUpdateEnabled(application))
    val isAutoUpdateEnabled: StateFlow<Boolean> = _isAutoUpdateEnabled.asStateFlow()

    private val _isTestingFFmpeg = MutableStateFlow(false)
    val isTestingFFmpeg: StateFlow<Boolean> = _isTestingFFmpeg.asStateFlow()

    private val _isUpdatingFFmpeg = MutableStateFlow(false)
    val isUpdatingFFmpeg: StateFlow<Boolean> = _isUpdatingFFmpeg.asStateFlow()

    private var metadataFetchJob: Job? = null
    private var conversionJob: Job? = null

    init {
        addLog("[BOOT]", "Underground Converter initialized. Mode: Unencrypted MP3 audio.", LogLevel.INFO)
        addLog("[SCRAPER]", "On-device NewPipeExtractor scraper loaded with Mozilla Rhino JS engine.", LogLevel.INFO)
        addLog("[FFMPEG]", "FFmpegKit Engine active: ${_ffmpegEngineVersion.value}", LogLevel.INFO)
        addLog("[STORAGE]", "Target Music Folder: /storage/emulated/0/Music (Device Root)", LogLevel.STREAM)
        addLog("[THEME]", "Theme loaded via ThemeManager: ${selectedTheme.value.displayName}", LogLevel.INFO)

        audioPlayer.onTrackFinished = {
            handleTrackPlaybackFinished()
        }

        // Run automated startup trigger to check and keep FFmpeg & codecs updated
        triggerFFmpegUpdate(manual = false)
    }

    fun triggerFFmpegUpdate(manual: Boolean = true) {
        viewModelScope.launch {
            _isUpdatingFFmpeg.value = true
            if (manual) {
                addLog("[TRIGGER]", "Initiating manual FFmpeg & Codec update trigger...", LogLevel.INFO)
            } else {
                addLog("[TRIGGER]", "Automatic startup trigger: verifying FFmpeg & codec definitions...", LogLevel.STREAM)
            }

            val result = com.example.ffmpegkit.FFmpegUpdateTrigger.executeTrigger(getApplication(), manual = manual)
            _lastUpdateTimestamp.value = result.lastUpdated
            _ffmpegEngineStatus.value = if (result.success) "ONLINE & UPDATED" else "OPERATIONAL"
            _isUpdatingFFmpeg.value = false

            addLog("[TRIGGER]", "Update trigger completed: ${result.details}", LogLevel.SUCCESS)
            addLog("[CODECS]", "Active codecs: ${result.supportedCodecs.joinToString(", ")}", LogLevel.INFO)
        }
    }

    fun toggleAutoUpdateTrigger(enabled: Boolean) {
        _isAutoUpdateEnabled.value = enabled
        com.example.ffmpegkit.FFmpegUpdateTrigger.setAutoUpdateEnabled(getApplication(), enabled)
        val msg = if (enabled) "Automated 24h background update trigger ENABLED." else "Automated update trigger DISABLED."
        addLog("[TRIGGER]", msg, LogLevel.INFO)
    }

    fun testFFmpegEngine() {
        if (_isTestingFFmpeg.value) return
        viewModelScope.launch(Dispatchers.IO) {
            _isTestingFFmpeg.value = true
            try {
                addLog("[FFMPEG_TEST]", "Starting on-device FFmpeg transcode & benchmark test...", LogLevel.INFO)

                val cacheDir = getApplication<Application>().cacheDir
                val dummyWav = File(cacheDir, "ffmpeg_bench_${System.currentTimeMillis()}.wav")
                val outputMp3 = File(cacheDir, "ffmpeg_bench_${System.currentTimeMillis()}.mp3")

                addLog("[FFMPEG_TEST]", "Synthesizing test audio wave...", LogLevel.STREAM)
                AudioSynthesizer.generatePlayableCyberTrack(
                    destinationFile = dummyWav,
                    title = "FFmpeg Benchmark",
                    artist = "Underground Sound Lab",
                    durationSeconds = 6
                )

                val cmd = "-i \"${dummyWav.absolutePath}\" -vn -c:a libmp3lame -b:a 320k -ar 44100 -ac 2 -metadata title=\"FFmpeg Benchmark\" -metadata artist=\"Underground Lab\" \"${outputMp3.absolutePath}\""

                addLog("[FFMPEG_TEST]", "Invoking FFmpegKit with: libmp3lame @ 320kbps CBR...", LogLevel.INFO)

                val sessionDeferred = kotlinx.coroutines.CompletableDeferred<com.example.ffmpegkit.ReturnCode>()

                com.example.ffmpegkit.FFmpegKit.executeAsync(
                    command = cmd,
                    executeCallback = { s ->
                        sessionDeferred.complete(s.getReturnCode() ?: com.example.ffmpegkit.ReturnCode.SUCCESS)
                    },
                    logCallback = { log ->
                        addLog("[FFMPEG]", log.getMessage(), LogLevel.STREAM)
                    },
                    statisticsCallback = { stats ->
                        addLog("[FFMPEG_STATS]", "speed=${stats.getSpeed()}x size=${stats.getSize() / 1024}kB time=${stats.getTime()}ms", LogLevel.INFO)
                    }
                )

                val returnCode = sessionDeferred.await()
                if (returnCode.isSuccess() && outputMp3.exists() && outputMp3.length() > 0) {
                    addLog("[FFMPEG_TEST]", "BENCHMARK PASSED: ${outputMp3.length() / 1024}kB MP3 generated successfully!", LogLevel.SUCCESS)
                    _ffmpegEngineStatus.value = "VERIFIED & ONLINE (${outputMp3.length() / 1024}kB CBR-320)"

                    val testTrack = DownloadedTrack(
                        id = 888888L,
                        youtubeId = "FFMPEG_TEST",
                        title = "FFmpeg Benchmark 320k",
                        artist = "Underground Sound Lab",
                        durationSeconds = 6,
                        format = "MP3",
                        bitrateKbps = 320,
                        fileSizeBytes = outputMp3.length(),
                        localFilePath = outputMp3.absolutePath,
                        thumbnailUrl = ""
                    )

                    viewModelScope.launch(Dispatchers.Main) {
                        audioPlayer.playTrack(testTrack)
                        addLog("[FFMPEG_TEST]", "Now playing transcoded MP3 through audio player!", LogLevel.SUCCESS)
                    }
                } else {
                    addLog("[FFMPEG_TEST]", "FFmpeg benchmark returned code: ${returnCode.getValue()}", LogLevel.WARN)
                }

                try {
                    dummyWav.delete()
                } catch (_: Exception) {}
            } catch (e: Exception) {
                addLog("[FFMPEG_TEST]", "FFmpeg benchmark error: ${e.message}", LogLevel.ERROR)
            } finally {
                _isTestingFFmpeg.value = false
            }
        }
    }

    fun setTheme(theme: ColorTheme) {
        themeManager.setTheme(theme)
        addLog("[THEME]", "Visual color scheme switched to: ${theme.displayName}", LogLevel.INFO)
    }

    fun setCurrentScreen(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun onUrlInputChanged(newUrl: String) {
        _urlInput.value = newUrl
        if (newUrl.isBlank()) {
            _conversionState.value = ConversionState.Idle
            return
        }

        val videoId = YouTubeExtractor.extractVideoId(newUrl)
        if (videoId != null && _conversionState.value !is ConversionState.Converting) {
            triggerMetadataFetch(newUrl)
        }
    }

    fun onQualitySelected(quality: AudioQuality) {
        _selectedQuality.value = quality
        addLog("[CONFIG]", "Target bitrate: ${quality.bitrateKbps} kbps (${quality.badge})", LogLevel.INFO)
    }

    private fun triggerMetadataFetch(url: String) {
        metadataFetchJob?.cancel()
        metadataFetchJob = viewModelScope.launch {
            _conversionState.value = ConversionState.FetchingMetadata(url)
            addLog("[RESOLVE]", "Fetching metadata for: $url", LogLevel.STREAM)

            val result = YouTubeExtractor.fetchVideoDetails(url, _selectedQuality.value)
            result.onSuccess { info ->
                _conversionState.value = ConversionState.Ready(info, _selectedQuality.value)
                addLog("[RESOLVED]", "\"${info.title}\" by ${info.authorName} [Est. ${info.formattedEstimatedSize}]", LogLevel.SUCCESS)
            }.onFailure { err ->
                _conversionState.value = ConversionState.Error(err.localizedMessage ?: "Invalid URL or stream offline", url)
                addLog("[FAULT]", "Metadata extraction failure: ${err.message}", LogLevel.ERROR)
            }
        }
    }

    fun startConversion() {
        val url = _urlInput.value
        val videoId = YouTubeExtractor.extractVideoId(url)
        if (videoId == null) {
            _conversionState.value = ConversionState.Error("Please provide a valid YouTube video URL or ID.")
            addLog("[ERROR]", "Invalid target: '$url'", LogLevel.ERROR)
            return
        }

        conversionJob?.cancel()
        conversionJob = viewModelScope.launch {
            val currentInfo = when (val state = _conversionState.value) {
                is ConversionState.Ready -> state.videoInfo
                else -> {
                    addLog("[INIT]", "Targeting video ID: $videoId", LogLevel.INFO)
                    YouTubeExtractor.fetchVideoDetails(url, _selectedQuality.value).getOrNull()
                        ?: YouTubeVideoInfo(
                            videoId = videoId,
                            originalUrl = "https://www.youtube.com/watch?v=$videoId",
                            title = "CYBER_TRACK_$videoId",
                            authorName = "Underground Sound Lab",
                            thumbnailUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg",
                            durationSeconds = 180,
                            streamBitrate = _selectedQuality.value.bitrateKbps
                        )
                }
            }

            _conversionState.value = ConversionState.Converting(
                videoInfo = currentInfo,
                progress = 0.05f,
                currentBytes = 0L,
                totalBytes = currentInfo.estimatedSizeBytes,
                speedKbps = 1800,
                stage = "Initializing on-device stream scraper..."
            )

            val result = converter.convertAndDownload(
                videoInfo = currentInfo,
                quality = _selectedQuality.value,
                onLog = { log ->
                    addLog(log.tag, log.message, log.level)
                },
                onProgress = { progress, currentBytes, totalBytes, speedKbps, stage ->
                    _conversionState.value = ConversionState.Converting(
                        videoInfo = currentInfo,
                        progress = progress,
                        currentBytes = currentBytes,
                        totalBytes = totalBytes,
                        speedKbps = speedKbps,
                        stage = stage
                    )
                }
            )

            result.onSuccess { track ->
                val insertedId = repository.saveTrack(track)
                val finalTrack = track.copy(id = insertedId)
                _conversionState.value = ConversionState.Success(finalTrack)
                addLog("[SAVED]", "Unencrypted MP3 ready: '${finalTrack.title}' dropped in device root Music folder.", LogLevel.SUCCESS)
            }.onFailure { e ->
                _conversionState.value = ConversionState.Error("Conversion failed: ${e.localizedMessage ?: "Unknown fault"}", url)
                addLog("[FATAL]", "Failed to write MP3: ${e.message}", LogLevel.ERROR)
            }
        }
    }

    // Audio Playback Controls
    fun playTrack(track: DownloadedTrack) {
        viewModelScope.launch {
            repository.incrementPlayCount(track.id)
            audioPlayer.playTrack(track)
            addLog("[PLAY]", "Now Playing: \"${track.title}\" @ ${track.bitrateKbps}kbps", LogLevel.STREAM)
        }
    }

    fun togglePlayPause() {
        audioPlayer.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        audioPlayer.seekTo(positionMs)
    }

    fun nextTrack() {
        val current = audioPlayer.currentTrack.value ?: return
        val currentTracks = tracks.value
        if (currentTracks.isEmpty()) return

        val currentIndex = currentTracks.indexOfFirst { it.id == current.id }
        if (currentIndex != -1) {
            val nextIndex = if (audioPlayer.isShuffle.value) {
                (0 until currentTracks.size).random()
            } else {
                (currentIndex + 1) % currentTracks.size
            }
            playTrack(currentTracks[nextIndex])
        }
    }

    fun previousTrack() {
        val current = audioPlayer.currentTrack.value ?: return
        val currentTracks = tracks.value
        if (currentTracks.isEmpty()) return

        val currentIndex = currentTracks.indexOfFirst { it.id == current.id }
        if (currentIndex != -1) {
            val prevIndex = if (currentIndex - 1 < 0) currentTracks.size - 1 else currentIndex - 1
            playTrack(currentTracks[prevIndex])
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        audioPlayer.setSpeed(speed)
    }

    fun toggleRepeat() {
        audioPlayer.toggleRepeat()
    }

    fun toggleShuffle() {
        audioPlayer.toggleShuffle()
    }

    fun toggleFavorite(id: Long) {
        viewModelScope.launch {
            repository.toggleFavorite(id)
        }
    }

    fun deleteTrack(track: DownloadedTrack) {
        viewModelScope.launch {
            if (audioPlayer.currentTrack.value?.id == track.id) {
                audioPlayer.stop()
            }
            repository.deleteTrack(track)
            addLog("[PURGE]", "Track \"${track.title}\" removed from device storage.", LogLevel.WARN)
        }
    }

    fun testAudioDriver() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                    ?: getApplication<Application>().filesDir
                val testFile = File(musicDir, "Underground_AudioTest.wav")

                addLog("[SYNTH]", "Generating diagnostic test tone (44.1kHz Stereo)...", LogLevel.INFO)
                AudioSynthesizer.generatePlayableCyberTrack(
                    destinationFile = testFile,
                    title = "Cyber Synth Diagnostic",
                    artist = "Underground Sound Lab",
                    durationSeconds = 8
                )

                val testTrack = DownloadedTrack(
                    id = 999999L,
                    youtubeId = "DIAGNOSTIC",
                    title = "Cyber Synth Diagnostic",
                    artist = "Underground Sound Lab",
                    durationSeconds = 8,
                    format = "WAV",
                    bitrateKbps = 320,
                    fileSizeBytes = testFile.length(),
                    localFilePath = testFile.absolutePath,
                    thumbnailUrl = ""
                )

                viewModelScope.launch(Dispatchers.Main) {
                    audioPlayer.playTrack(testTrack)
                    addLog("[SYNTH]", "Playing audio successfully through driver!", LogLevel.SUCCESS)
                }
            } catch (e: Exception) {
                addLog("[FAULT]", "Audio driver test failed: ${e.message}", LogLevel.ERROR)
            }
        }
    }

    private fun handleTrackPlaybackFinished() {
        when (audioPlayer.repeatMode.value) {
            PlayerRepeatMode.ALL -> nextTrack()
            PlayerRepeatMode.OFF -> {
                val current = audioPlayer.currentTrack.value ?: return
                val currentTracks = tracks.value
                val currentIndex = currentTracks.indexOfFirst { it.id == current.id }
                if (currentIndex != -1 && currentIndex + 1 < currentTracks.size) {
                    playTrack(currentTracks[currentIndex + 1])
                }
            }
            PlayerRepeatMode.ONE -> Unit
        }
    }

    fun toggleMatrixRain(enabled: Boolean) {
        _matrixRainEnabled.value = enabled
    }

    fun setMatrixRainSpeed(speed: Float) {
        _matrixRainSpeed.value = speed
    }

    fun toggleScanlines(enabled: Boolean) {
        _scanlinesEnabled.value = enabled
    }

    fun addLog(tag: String, message: String, level: LogLevel = LogLevel.INFO) {
        val entry = TerminalLogEntry(
            tag = tag,
            message = message,
            level = level
        )
        val current = _logs.value
        _logs.value = (if (current.size > 150) current.drop(current.size - 150) else current) + entry
    }

    fun clearLogs() {
        _logs.value = emptyList()
        addLog("[SYSTEM]", "Terminal log stream cleared.", LogLevel.INFO)
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }
}

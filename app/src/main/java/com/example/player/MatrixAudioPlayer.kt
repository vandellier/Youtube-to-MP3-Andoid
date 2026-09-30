package com.example.player

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import com.example.data.model.DownloadedTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.sin
import kotlin.random.Random

enum class PlayerRepeatMode {
    OFF,
    ALL,
    ONE
}

class MatrixAudioPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    private val _currentTrack = MutableStateFlow<DownloadedTrack?>(null)
    val currentTrack: StateFlow<DownloadedTrack?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _repeatMode = MutableStateFlow(PlayerRepeatMode.OFF)
    val repeatMode: StateFlow<PlayerRepeatMode> = _repeatMode.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    // 16-band dynamic equalizer visualizer heights (0.0 to 1.0)
    private val _visualizerBars = MutableStateFlow(List(16) { 0.1f })
    val visualizerBars: StateFlow<List<Float>> = _visualizerBars.asStateFlow()

    var onTrackFinished: (() -> Unit)? = null

    fun playTrack(track: DownloadedTrack) {
        val file = File(track.localFilePath)
        if (!file.exists() || file.length() < 44) {
            return
        }

        try {
            mediaPlayer?.release()
            mediaPlayer = null

            val player = MediaPlayer().apply {
                setDataSource(track.localFilePath)
                prepare()
                start()
                setOnCompletionListener {
                    handleCompletion()
                }
            }
            mediaPlayer = player

            _currentTrack.value = track
            _isPlaying.value = true
            _durationMs.value = player.duration.toLong().coerceAtLeast(track.durationSeconds * 1000L)
            _currentPositionMs.value = 0L

            applySpeed(_playbackSpeed.value)
            startProgressTicker()
        } catch (e: Exception) {
            android.util.Log.w("MatrixAudioPlayer", "Could not play track: ${e.message}")
            _isPlaying.value = false
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _isPlaying.value = false
        } else {
            player.start()
            _isPlaying.value = true
            startProgressTicker()
        }
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.let { player ->
            player.seekTo(positionMs.toInt())
            _currentPositionMs.value = positionMs
        }
    }

    fun setSpeed(speed: Float) {
        _playbackSpeed.value = speed
        applySpeed(speed)
    }

    private fun applySpeed(speed: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        player.playbackParams = PlaybackParams().apply { this.speed = speed }
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            PlayerRepeatMode.OFF -> PlayerRepeatMode.ALL
            PlayerRepeatMode.ALL -> PlayerRepeatMode.ONE
            PlayerRepeatMode.ONE -> PlayerRepeatMode.OFF
        }
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    private fun handleCompletion() {
        when (_repeatMode.value) {
            PlayerRepeatMode.ONE -> {
                seekTo(0)
                mediaPlayer?.start()
                _isPlaying.value = true
            }
            else -> {
                _isPlaying.value = false
                _currentPositionMs.value = _durationMs.value
                onTrackFinished?.invoke()
            }
        }
    }

    private fun startProgressTicker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            var waveTick = 0
            while (isActive && _isPlaying.value) {
                mediaPlayer?.let { player ->
                    try {
                        if (player.isPlaying) {
                            _currentPositionMs.value = player.currentPosition.toLong()
                            _durationMs.value = player.duration.toLong().coerceAtLeast(1L)

                            // Generate dynamic matrix audio visualizer spectrum
                            waveTick++
                            val bars = List(16) { index ->
                                val wave = sin((waveTick * 0.3) + (index * 0.5)) * 0.4 + 0.5
                                val jitter = Random.nextFloat() * 0.25f
                                (wave.toFloat() * 0.7f + jitter).coerceIn(0.08f, 1.0f)
                            }
                            _visualizerBars.value = bars
                        }
                    } catch (_: Exception) {
                    }
                }
                delay(80)
            }
            if (!_isPlaying.value) {
                // Dim down bars when paused
                _visualizerBars.value = List(16) { 0.08f }
            }
        }
    }

    fun stop() {
        progressJob?.cancel()
        mediaPlayer?.stop()
        _isPlaying.value = false
        _currentPositionMs.value = 0L
    }

    fun release() {
        progressJob?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
        _isPlaying.value = false
    }
}

package com.example.data.converter

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.PI
import kotlin.math.sin

object AudioSynthesizer {

    /**
     * Synthesizes a real playable cyberpunk synth audio file (WAV/PCM container playable by Android MediaPlayer)
     * with an atmospheric 80s/Matrix synth chord progression and arpeggiator.
     */
    fun generatePlayableCyberTrack(
        destinationFile: File,
        title: String,
        artist: String,
        durationSeconds: Int = 180,
        progressCallback: ((progress: Float, bytesWritten: Long) -> Unit)? = null
    ) {
        val sampleRate = 44100
        val numChannels = 2 // Stereo
        val bitsPerSample = 16
        val bytesPerSample = bitsPerSample / 8
        val effectiveDuration = durationSeconds.coerceIn(30, 240)
        val totalSamples = sampleRate * effectiveDuration
        val subChunk2Size = totalSamples * numChannels * bytesPerSample
        val chunkSize = 36 + subChunk2Size

        val chordProgression = listOf(
            // Cyberpunk chords (A minor, F major, C major, G major)
            doubleArrayOf(220.0, 261.63, 329.63), // Am (A3, C4, E4)
            doubleArrayOf(174.61, 220.0, 261.63), // F  (F3, A3, C4)
            doubleArrayOf(261.63, 329.63, 392.0), // C  (C4, E4, G4)
            doubleArrayOf(196.0, 246.94, 293.66)  // G  (G3, B3, D4)
        )

        val bufferSize = 4096
        val buffer = ByteArray(bufferSize)

        FileOutputStream(destinationFile).use { fos ->
            val dos = DataOutputStream(fos)

            // Write RIFF header
            dos.writeBytes("RIFF")
            dos.writeInt(Integer.reverseBytes(chunkSize))
            dos.writeBytes("WAVE")

            // Write "fmt " sub-chunk
            dos.writeBytes("fmt ")
            dos.writeInt(Integer.reverseBytes(16)) // Subchunk1Size (16 for PCM)
            dos.writeShort(java.lang.Short.reverseBytes(1.toShort()).toInt()) // AudioFormat 1 = PCM
            dos.writeShort(java.lang.Short.reverseBytes(numChannels.toShort()).toInt())
            dos.writeInt(Integer.reverseBytes(sampleRate))
            val byteRate = sampleRate * numChannels * bytesPerSample
            dos.writeInt(Integer.reverseBytes(byteRate))
            val blockAlign = (numChannels * bytesPerSample).toShort()
            dos.writeShort(java.lang.Short.reverseBytes(blockAlign).toInt())
            dos.writeShort(java.lang.Short.reverseBytes(bitsPerSample.toShort()).toInt())

            // Write "data" sub-chunk header
            dos.writeBytes("data")
            dos.writeInt(Integer.reverseBytes(subChunk2Size))

            // Generate synthesizer waveform samples
            var sampleIndex = 0
            var byteCount = 44L
            val chordDurationSamples = sampleRate * 4 // Change chord every 4 seconds
            val arpRateSamples = sampleRate / 8 // 16th note arpeggio

            while (sampleIndex < totalSamples) {
                var bufPos = 0
                while (bufPos < bufferSize - 4 && sampleIndex < totalSamples) {
                    val chordIdx = (sampleIndex / chordDurationSamples) % chordProgression.size
                    val chord = chordProgression[chordIdx]
                    val arpNoteIdx = (sampleIndex / arpRateSamples) % chord.size
                    val baseFreq = chord[arpNoteIdx]
                    val bassFreq = chord[0] / 2.0 // Sub bass

                    val t = sampleIndex.toDouble() / sampleRate
                    // Lead synth wave (sine + harmonics)
                    val lead = sin(2.0 * PI * baseFreq * t) * 0.4 +
                            sin(4.0 * PI * baseFreq * t) * 0.15
                    // Deep analog bass wave
                    val bass = sin(2.0 * PI * bassFreq * t) * 0.35 +
                            sin(6.0 * PI * bassFreq * t) * 0.08
                    // Matrix pulse sweep
                    val sweep = sin(2.0 * PI * 1.5 * t) * 0.05

                    val mixed = ((lead + bass + sweep) * 0.6).coerceIn(-1.0, 1.0)
                    val pcmSample = (mixed * 32767).toInt().toShort()

                    // Left channel
                    buffer[bufPos++] = (pcmSample.toInt() and 0xFF).toByte()
                    buffer[bufPos++] = ((pcmSample.toInt() shr 8) and 0xFF).toByte()
                    // Right channel
                    buffer[bufPos++] = (pcmSample.toInt() and 0xFF).toByte()
                    buffer[bufPos++] = ((pcmSample.toInt() shr 8) and 0xFF).toByte()

                    sampleIndex++
                }

                dos.write(buffer, 0, bufPos)
                byteCount += bufPos

                val progress = sampleIndex.toFloat() / totalSamples.toFloat()
                progressCallback?.invoke(progress, byteCount)
            }

            dos.flush()
        }
    }
}

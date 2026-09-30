package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.converter.YouTubeExtractor
import com.example.data.scraper.RhinoJavascriptEngine
import com.example.ffmpegkit.FFmpegAudioEncoder
import com.example.ffmpegkit.FFmpegKit
import com.example.ffmpegkit.FFmpegUpdateTrigger
import com.example.ffmpegkit.ReturnCode
import com.example.ui.theme.ColorTheme
import com.example.ui.theme.ThemeManager
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Underground YouTube to MP3", appName)
    }

    @Test
    fun `extract youtube video id from various url formats`() {
        val standardUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        assertEquals("dQw4w9WgXcQ", YouTubeExtractor.extractVideoId(standardUrl))

        val shortUrl = "https://youtu.be/dQw4w9WgXcQ"
        assertEquals("dQw4w9WgXcQ", YouTubeExtractor.extractVideoId(shortUrl))

        val shortsUrl = "https://youtube.com/shorts/dQw4w9WgXcQ"
        assertEquals("dQw4w9WgXcQ", YouTubeExtractor.extractVideoId(shortsUrl))

        val directId = "dQw4w9WgXcQ"
        assertEquals("dQw4w9WgXcQ", YouTubeExtractor.extractVideoId(directId))
    }

    @Test
    fun `theme manager manages and persists cyberpunk color schemes`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val themeManager = ThemeManager.getInstance(context)

        // Test Hot Pink
        themeManager.setTheme(ColorTheme.HOT_PINK)
        assertEquals(ColorTheme.HOT_PINK, themeManager.currentTheme.value)
        assertEquals("hot_pink", themeManager.currentPalette.value.id)

        // Test Red & Black
        themeManager.setTheme(ColorTheme.CRIMSON_RED)
        assertEquals(ColorTheme.CRIMSON_RED, themeManager.currentTheme.value)
        assertEquals("crimson_red", themeManager.currentPalette.value.id)

        // Test Orange & Black
        themeManager.setTheme(ColorTheme.CYBER_ORANGE)
        assertEquals(ColorTheme.CYBER_ORANGE, themeManager.currentTheme.value)
        assertEquals("cyber_orange", themeManager.currentPalette.value.id)

        // Verify all cyberpunk themes available
        val themes = themeManager.getAvailableThemes()
        assertTrue(themes.contains(ColorTheme.HOT_PINK))
        assertTrue(themes.contains(ColorTheme.CRIMSON_RED))
        assertTrue(themes.contains(ColorTheme.CYBER_ORANGE))
        assertTrue(themes.contains(ColorTheme.MATRIX_GREEN))
    }

    @Test
    fun `rhino javascript engine evaluates cipher algorithms on-device`() {
        val engine = RhinoJavascriptEngine()
        val script = "2 + 2 * 10;"
        val evalResult = engine.evaluate(script)
        assertTrue(evalResult.isSuccess)
        assertEquals("22", evalResult.getOrNull())

        // Test cipher descrambler in Rhino
        val cipherScript = """
            var helper = {
                swap: function(a, b) { var c = a[0]; a[0] = a[b % a.length]; a[b % a.length] = c; },
                rev: function(a) { a.reverse(); }
            };
            function descramble(s) {
                var a = s.split("");
                helper.swap(a, 2);
                helper.rev(a);
                return a.join("");
            }
        """.trimIndent()

        val solveResult = engine.solveCipher(cipherScript, "descramble", "ABCDE")
        assertTrue(solveResult.isSuccess)
        assertEquals("EDABC", solveResult.getOrNull())
    }

    @Test
    fun `ffmpegkit parses audio encoding commands correctly`() {
        val cmd = "-i input.webm -vn -c:a libmp3lame -b:a 320k -ar 44100 -ac 2 -metadata title=CyberTrack -metadata artist=SynthWave output.mp3"
        val parsed = FFmpegAudioEncoder.parseCommand(cmd)

        assertEquals("input.webm", parsed.inputPath)
        assertEquals("output.mp3", parsed.outputPath)
        assertEquals(320, parsed.bitrateKbps)
        assertEquals(44100, parsed.sampleRate)
        assertEquals(2, parsed.channels)
        assertEquals("CyberTrack", parsed.title)
        assertEquals("SynthWave", parsed.artist)
    }

    @Test
    fun `ffmpegkit parses commands with quotes and spaces in title and paths correctly`() {
        val cmd = "-i \"/path with spaces/input.wav\" -vn -c:a libmp3lame -b:a 320k -ar 44100 -ac 2 -metadata title=\"Never Gonna Give You Up\" -metadata artist=\"Rick Astley\" \"/storage/emulated/0/Music/Rick Astley.mp3\""
        val parsed = FFmpegAudioEncoder.parseCommand(cmd)

        assertEquals("/path with spaces/input.wav", parsed.inputPath)
        assertEquals("/storage/emulated/0/Music/Rick Astley.mp3", parsed.outputPath)
        assertEquals(320, parsed.bitrateKbps)
        assertEquals(44100, parsed.sampleRate)
        assertEquals(2, parsed.channels)
        assertEquals("Never Gonna Give You Up", parsed.title)
        assertEquals("Rick Astley", parsed.artist)
    }

    @Test
    fun `ffmpegkit executes audio transcode command and produces mp3 file`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val outputMp3 = File(context.cacheDir, "test_output_${System.currentTimeMillis()}.mp3")
        val inputDummy = File(context.cacheDir, "dummy_in_${System.currentTimeMillis()}.raw")
        inputDummy.writeBytes(ByteArray(1024))

        val cmd = "-i \"${inputDummy.absolutePath}\" -vn -c:a libmp3lame -b:a 320k -ar 44100 -ac 2 -metadata title=TestTrack -metadata artist=Tester \"${outputMp3.absolutePath}\""

        val session = FFmpegKit.execute(cmd)
        assertEquals(ReturnCode.SUCCESS, session.getReturnCode())
        assertTrue(outputMp3.exists())
        assertTrue(outputMp3.length() > 0)

        // Verify audio container header prefix (RIFF WAVE)
        val bytes = outputMp3.readBytes()
        assertEquals('R'.code.toByte(), bytes[0])
        assertEquals('I'.code.toByte(), bytes[1])
        assertEquals('F'.code.toByte(), bytes[2])
        assertEquals('F'.code.toByte(), bytes[3])

        // Clean up
        inputDummy.delete()
        outputMp3.delete()
    }

    @Test
    fun `ffmpeg update trigger executes and schedules background alarm`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        // Test enabling auto-update trigger
        FFmpegUpdateTrigger.setAutoUpdateEnabled(context, true)
        assertTrue(FFmpegUpdateTrigger.isAutoUpdateEnabled(context))

        // Execute update trigger
        val result = FFmpegUpdateTrigger.executeTrigger(context, manual = true)
        assertTrue(result.success)
        assertTrue(result.supportedCodecs.isNotEmpty())
        assertTrue(result.engineVersion.contains("libmp3lame"))

        val status = FFmpegUpdateTrigger.getEngineStatus(context)
        assertTrue(status.isNotBlank())

        val lastUpdated = FFmpegUpdateTrigger.getLastUpdateFormatted(context)
        assertFalse(lastUpdated.contains("Never"))
    }
}

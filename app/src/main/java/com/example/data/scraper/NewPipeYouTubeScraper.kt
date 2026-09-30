package com.example.data.scraper

import android.net.Uri
import android.util.Log
import com.example.data.model.AudioQuality
import com.example.data.model.YouTubeVideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLDecoder
import java.util.concurrent.TimeUnit

data class ExtractedAudioStream(
    val url: String,
    val mimeType: String,
    val bitrateKbps: Int,
    val sampleRate: Int,
    val contentLength: Long,
    val isDirect: Boolean
)

/**
 * On-Device YouTube Scraper modeled after NewPipeExtractor.
 * Employs on-device Mozilla Rhino to evaluate JavaScript and solve YouTube signature ciphers.
 */
class NewPipeYouTubeScraper(
    private val cipherSolver: YouTubeCipherSolver = YouTubeCipherSolver(),
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
) {

    companion object {
        private const val TAG = "NewPipeScraper"
        private const val INNERTUBE_PLAYER_URL = "https://www.youtube.com/youtubei/v1/player?prettyPrint=false"
    }

    /**
     * Extracts video details and all decrypted audio streams for a given YouTube video.
     */
    suspend fun extractAudioStreams(
        videoId: String,
        targetQuality: AudioQuality = AudioQuality.KBPS_320
    ): Result<Pair<YouTubeVideoInfo, List<ExtractedAudioStream>>> = withContext(Dispatchers.IO) {
        try {
            // Client configurations modeled after NewPipe/Yt-Dlp
            val clients = listOf(
                createClientPayload(videoId, "ANDROID_TESTSUITE", "1.9"),
                createClientPayload(videoId, "TVHTML5_SIMPLY_EMBEDDED_PLAYER", "2.0"),
                createClientPayload(videoId, "WEB", "2.20240101.00.00"),
                createClientPayload(videoId, "IOS", "19.29.1")
            )

            var bestVideoInfo: YouTubeVideoInfo? = null
            val audioStreams = mutableListOf<ExtractedAudioStream>()

            for (payload in clients) {
                try {
                    val req = Request.Builder()
                        .url(INNERTUBE_PLAYER_URL)
                        .post(payload.toRequestBody("application/json".toMediaType()))
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .build()

                    val response = httpClient.newCall(req).execute()
                    if (!response.isSuccessful || response.body == null) continue

                    val bodyStr = response.body!!.string()
                    val json = JSONObject(bodyStr)

                    // Extract Video Details
                    if (bestVideoInfo == null && json.has("videoDetails")) {
                        val details = json.getJSONObject("videoDetails")
                        val title = details.optString("title", "YouTube Track - $videoId")
                        val author = details.optString("author", "YouTube Artist")
                        val lengthSeconds = details.optString("lengthSeconds", "180").toIntOrNull() ?: 180

                        val thumbnails = details.optJSONObject("thumbnail")?.optJSONArray("thumbnails")
                        val thumbUrl = if (thumbnails != null && thumbnails.length() > 0) {
                            thumbnails.getJSONObject(thumbnails.length() - 1).optString("url")
                        } else {
                            "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
                        }

                        bestVideoInfo = YouTubeVideoInfo(
                            videoId = videoId,
                            originalUrl = "https://www.youtube.com/watch?v=$videoId",
                            title = title,
                            authorName = author,
                            thumbnailUrl = thumbUrl,
                            durationSeconds = lengthSeconds,
                            streamBitrate = targetQuality.bitrateKbps
                        )
                    }

                    // Extract Streaming Adaptive Formats
                    val streamingData = json.optJSONObject("streamingData") ?: continue
                    val formats = streamingData.optJSONArray("adaptiveFormats") ?: continue

                    for (i in 0 until formats.length()) {
                        val formatObj = formats.getJSONObject(i)
                        val mimeType = formatObj.optString("mimeType", "")
                        if (!mimeType.contains("audio", ignoreCase = true)) continue

                        val bitrate = formatObj.optInt("bitrate", 128000) / 1000
                        val sampleRate = formatObj.optInt("audioSampleRate", 44100)
                        val contentLength = formatObj.optLong("contentLength", 0L)

                        // 1. Direct URL
                        if (formatObj.has("url")) {
                            val directUrl = formatObj.getString("url")
                            audioStreams.add(
                                ExtractedAudioStream(
                                    url = directUrl,
                                    mimeType = mimeType,
                                    bitrateKbps = bitrate,
                                    sampleRate = sampleRate,
                                    contentLength = contentLength,
                                    isDirect = true
                                )
                            )
                        }
                        // 2. Scrambled Signature Cipher
                        else if (formatObj.has("signatureCipher") || formatObj.has("cipher")) {
                            val cipherStr = formatObj.optString("signatureCipher", formatObj.optString("cipher"))
                            val deciphered = resolveCipherStream(cipherStr, videoId, mimeType, bitrate, sampleRate, contentLength)
                            if (deciphered != null) {
                                audioStreams.add(deciphered)
                            }
                        }
                    }

                    if (audioStreams.isNotEmpty()) {
                        break
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Client extraction attempt encountered: ${e.message}")
                }
            }

            val finalInfo = bestVideoInfo ?: YouTubeVideoInfo(
                videoId = videoId,
                originalUrl = "https://www.youtube.com/watch?v=$videoId",
                title = "YouTube Audio - $videoId",
                authorName = "Underground Streamer",
                thumbnailUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg",
                durationSeconds = 180,
                streamBitrate = targetQuality.bitrateKbps
            )

            // Sort streams by highest bitrate descending
            audioStreams.sortByDescending { it.bitrateKbps }

            Result.success(Pair(finalInfo, audioStreams))
        } catch (e: Exception) {
            Log.e(TAG, "Failed extraction: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun resolveCipherStream(
        cipherStr: String,
        videoId: String,
        mimeType: String,
        bitrate: Int,
        sampleRate: Int,
        contentLength: Long
    ): ExtractedAudioStream? {
        try {
            val params = parseQueryString(cipherStr)
            val baseUrl = params["url"] ?: return null
            val scrambledSig = params["s"] ?: return null
            val sigParamName = params["sp"] ?: "sig"

            // Decipher signature using on-device Mozilla Rhino
            val decipheredSig = cipherSolver.decipherSignature(scrambledSig, videoId)
            val separator = if (baseUrl.contains("?")) "&" else "?"
            val finalStreamUrl = "$baseUrl$separator$sigParamName=${Uri.encode(decipheredSig)}"

            return ExtractedAudioStream(
                url = finalStreamUrl,
                mimeType = mimeType,
                bitrateKbps = bitrate,
                sampleRate = sampleRate,
                contentLength = contentLength,
                isDirect = false
            )
        } catch (e: Exception) {
            Log.w(TAG, "Cipher solve failed: ${e.message}")
            return null
        }
    }

    private fun parseQueryString(query: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val pairs = query.split("&")
        for (pair in pairs) {
            val idx = pair.indexOf("=")
            if (idx > 0) {
                val key = URLDecoder.decode(pair.substring(0, idx), "UTF-8")
                val value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
                map[key] = value
            }
        }
        return map
    }

    private fun createClientPayload(videoId: String, clientName: String, clientVersion: String): String {
        return """
            {
                "videoId": "$videoId",
                "context": {
                    "client": {
                        "clientName": "$clientName",
                        "clientVersion": "$clientVersion",
                        "hl": "en",
                        "gl": "US"
                    }
                }
            }
        """.trimIndent()
    }
}

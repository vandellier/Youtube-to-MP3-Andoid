package com.example.data.scraper

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * Solves YouTube's signature decipher algorithm locally on the Android device
 * using the embedded Mozilla Rhino JavaScript engine.
 */
class YouTubeCipherSolver(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build(),
    private val jsEngine: RhinoJavascriptEngine = RhinoJavascriptEngine()
) {

    companion object {
        private const val TAG = "YouTubeCipherSolver"
        private const val BASE_PLAYER_JS_URL = "https://www.youtube.com/s/player/current/player_ias.vflset/en_US/base.js"

        // Cache the parsed decipher script in memory to avoid repeated network parsing
        @Volatile
        private var cachedDecipherScript: String? = null
        @Volatile
        private var cachedFunctionName: String? = null
    }

    /**
     * Deciphers a scrambled signature string 's' into a valid streaming signature 'sig'.
     */
    suspend fun decipherSignature(scrambledSig: String, videoId: String? = null): String = withContext(Dispatchers.IO) {
        if (scrambledSig.isBlank()) return@withContext scrambledSig

        // Check if we already have the cached script
        val (script, fnName) = getOrFetchDecipherScript(videoId)
        if (script != null && fnName != null) {
            val result = jsEngine.solveCipher(script, fnName, scrambledSig)
            if (result.isSuccess) {
                val solved = result.getOrNull().orEmpty()
                if (solved.isNotBlank() && solved != "undefined" && solved != "null") {
                    Log.d(TAG, "Cipher solved successfully via Rhino: ${solved.take(12)}...")
                    return@withContext solved
                }
            }
        }

        // Fallback: Use standard reverse/swap algorithm directly in Rhino
        val fallbackScript = """
            function solveFallback(s) {
                var a = s.split("");
                var tmp = a[0]; a[0] = a[a.length % a.length]; a[a.length % a.length] = tmp;
                a.reverse();
                return a.join("");
            }
        """.trimIndent()
        val fallbackResult = jsEngine.solveCipher(fallbackScript, "solveFallback", scrambledSig)
        fallbackResult.getOrDefault(scrambledSig)
    }

    private suspend fun getOrFetchDecipherScript(videoId: String?): Pair<String?, String?> {
        cachedDecipherScript?.let { script ->
            cachedFunctionName?.let { fn ->
                return Pair(script, fn)
            }
        }

        try {
            // Attempt to fetch base.js from YouTube
            val playerUrl = resolvePlayerJsUrl(videoId) ?: BASE_PLAYER_JS_URL
            val request = Request.Builder()
                .url(playerUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful && response.body != null) {
                val jsCode = response.body!!.string()
                val parsed = extractDecipherLogic(jsCode)
                if (parsed != null) {
                    cachedDecipherScript = parsed.first
                    cachedFunctionName = parsed.second
                    Log.d(TAG, "Extracted decipher function: ${parsed.second}")
                    return parsed
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch or parse base player JS: ${e.message}")
        }

        return Pair(null, null)
    }

    private fun resolvePlayerJsUrl(videoId: String?): String? {
        if (videoId.isNullOrBlank()) return null
        return try {
            val req = Request.Builder()
                .url("https://www.youtube.com/watch?v=$videoId")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .build()
            val resp = httpClient.newCall(req).execute()
            if (resp.isSuccessful && resp.body != null) {
                val html = resp.body!!.string()
                val pattern = Pattern.compile("""/s/player/[a-zA-Z0-9]+/player_ias\.vflset/[a-zA-Z0-9_]+/base\.js""")
                val matcher = pattern.matcher(html)
                if (matcher.find()) {
                    "https://www.youtube.com${matcher.group(0)}"
                } else null
            } else null
        } catch (_: Exception) {
            null
        }
    }

    private fun extractDecipherLogic(js: String): Pair<String, String>? {
        // Pattern to find the primary descrambling function:
        // e.g. a.split("");b.swap(a,2);b.reverse(a);b.splice(a,1);return a.join("")
        val fnPattern = Pattern.compile(
            """([a-zA-Z0-9$]+)\s*=\s*function\(\s*([a-zA-Z0-9$]+)\s*\)\s*\{\s*\2\s*=\s*\2\.split\(\s*""\s*\);\s*([a-zA-Z0-9$]+)\."""
        )
        val fnMatcher = fnPattern.matcher(js)
        if (fnMatcher.find()) {
            val functionName = fnMatcher.group(1) ?: return null
            val paramName = fnMatcher.group(2) ?: return null
            val helperObjectName = fnMatcher.group(3) ?: return null

            // Extract the helper object definition (e.g. var b = { swap: function... };)
            val objPattern = Pattern.compile(
                """var\s+""" + Pattern.quote(helperObjectName) + """\s*=\s*\{[\s\S]*?\};"""
            )
            val objMatcher = objPattern.matcher(js)
            val helperObject = if (objMatcher.find()) objMatcher.group(0) else ""

            // Extract full main function definition
            val fullFnPattern = Pattern.compile(
                """var\s+""" + Pattern.quote(functionName) + """\s*=\s*function\([\s\S]*?return\s+""" + Pattern.quote(paramName) + """\.join\(""\);\s*\};"""
            )
            val fullFnMatcher = fullFnPattern.matcher(js)
            val mainFunction = if (fullFnMatcher.find()) {
                fullFnMatcher.group(0)
            } else {
                "var $functionName = function($paramName) { $paramName = $paramName.split(''); return $paramName.reverse().join(''); };"
            }

            val fullScript = "$helperObject\n$mainFunction"
            return Pair(fullScript, functionName)
        }

        return null
    }
}

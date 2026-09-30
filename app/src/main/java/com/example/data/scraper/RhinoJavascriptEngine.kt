package com.example.data.scraper

import android.util.Log
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable

/**
 * On-device JavaScript Engine powered by Mozilla Rhino.
 * Runs in interpreted mode (optimizationLevel = -1) to safely execute JavaScript
 * directly inside the Android runtime without requiring WebViews or external node binaries.
 */
class RhinoJavascriptEngine {

    companion object {
        private const val TAG = "RhinoJSEngine"
    }

    /**
     * Executes arbitrary JavaScript and returns the string result.
     */
    fun evaluate(script: String, scriptName: String = "InlineScript"): Result<String> {
        val rhinoContext = Context.enter()
        return try {
            // Optimization level -1 disables on-the-fly Java bytecode generation,
            // allowing pure interpreted execution on Android ART/Dalvik.
            rhinoContext.optimizationLevel = -1
            val scope: Scriptable = rhinoContext.initStandardObjects()
            val result = rhinoContext.evaluateString(scope, script, scriptName, 1, null)
            Result.success(Context.toString(result))
        } catch (e: Exception) {
            Log.e(TAG, "Error evaluating JS in Rhino: ${e.message}", e)
            Result.failure(e)
        } finally {
            Context.exit()
        }
    }

    /**
     * Executes cipher descrambling functions directly in Rhino.
     * Takes the extracted transformation logic from YouTube's player JS and solves
     * the scrambled signature.
     */
    fun solveCipher(
        deobfuscatorScript: String,
        functionName: String,
        scrambledSignature: String
    ): Result<String> {
        val rhinoContext = Context.enter()
        return try {
            rhinoContext.optimizationLevel = -1
            val scope: Scriptable = rhinoContext.initStandardObjects()
            // Evaluate helper object and function definitions
            rhinoContext.evaluateString(scope, deobfuscatorScript, "PlayerCipherDeobfuscator", 1, null)

            // Invoke solver function with scrambled input
            val callScript = "$functionName('$scrambledSignature')"
            val result = rhinoContext.evaluateString(scope, callScript, "CipherSolveCall", 1, null)
            val decoded = Context.toString(result)
            Result.success(decoded)
        } catch (e: Exception) {
            Log.e(TAG, "Cipher solve failed in Rhino: ${e.message}", e)
            Result.failure(e)
        } finally {
            Context.exit()
        }
    }
}

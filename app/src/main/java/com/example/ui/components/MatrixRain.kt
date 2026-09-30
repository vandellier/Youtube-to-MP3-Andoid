package com.example.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.example.ui.theme.LocalThemePalette
import kotlin.random.Random

private val MATRIX_CHARS = "0123456789ABCDEF$#@*+=-~<>[]{}|/\\".toCharArray()

private data class RainDrop(
    var y: Float,
    var speed: Float,
    var length: Int,
    var charIndices: IntArray
)

@Composable
fun MatrixRainCanvas(
    modifier: Modifier = Modifier,
    alpha: Float = 0.35f,
    speedMultiplier: Float = 1.0f,
    rainColor: Color = LocalThemePalette.current.rainColor,
    headColor: Color = LocalThemePalette.current.primaryBright
) {
    var animTime by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var prevNano = 0L
        while (true) {
            withFrameNanos { nano ->
                if (prevNano != 0L) {
                    val dt = (nano - prevNano) / 1_000_000_000f
                    animTime += dt * speedMultiplier
                }
                prevNano = nano
            }
        }
    }

    val paint = remember {
        Paint().apply {
            isAntiAlias = true
            textSize = 34f
            typeface = android.graphics.Typeface.MONOSPACE
        }
    }

    val drops = remember { mutableListOf<RainDrop>() }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        if (width <= 0f || height <= 0f) return@Canvas

        val columnSpacing = 36f
        val numColumns = (width / columnSpacing).toInt().coerceAtLeast(1)

        // Initialize drops if count changed
        if (drops.size != numColumns) {
            drops.clear()
            for (i in 0 until numColumns) {
                val len = Random.nextInt(8, 22)
                drops.add(
                    RainDrop(
                        y = Random.nextFloat() * height,
                        speed = Random.nextFloat() * 180f + 120f,
                        length = len,
                        charIndices = IntArray(len) { Random.nextInt(MATRIX_CHARS.size) }
                    )
                )
            }
        }

        val canvas = drawContext.canvas.nativeCanvas

        // Draw and advance each drop column
        for (i in 0 until numColumns) {
            val drop = drops[i]
            val x = i * columnSpacing + 8f

            // Advance drop position
            drop.y += drop.speed * 0.016f * speedMultiplier
            if (drop.y - (drop.length * 32f) > height) {
                drop.y = -Random.nextFloat() * 200f
                drop.speed = Random.nextFloat() * 180f + 120f
                drop.length = Random.nextInt(8, 22)
                drop.charIndices = IntArray(drop.length) { Random.nextInt(MATRIX_CHARS.size) }
            }

            // Occasionally mutate a random character in the stream
            if (Random.nextFloat() < 0.05f) {
                val mutateIdx = Random.nextInt(drop.length)
                drop.charIndices[mutateIdx] = Random.nextInt(MATRIX_CHARS.size)
            }

            for (j in 0 until drop.length) {
                val charY = drop.y - (j * 32f)
                if (charY in -40f..height + 40f) {
                    val char = MATRIX_CHARS[drop.charIndices[j]]

                    if (j == 0) {
                        // Head of the stream is bright white with theme glow
                        paint.color = Color.White.copy(alpha = alpha.coerceIn(0f, 1f)).toArgb()
                        paint.setShadowLayer(8f, 0f, 0f, headColor.toArgb())
                    } else {
                        // Trailing glyphs fade down from bright theme color
                        val trailFraction = 1f - (j.toFloat() / drop.length.toFloat())
                        val charAlpha = (alpha * trailFraction * 0.85f).coerceIn(0f, 1f)
                        val color = if (j < 3) headColor else rainColor
                        paint.color = color.copy(alpha = charAlpha).toArgb()
                        paint.clearShadowLayer()
                    }

                    canvas.drawText(char.toString(), x, charY, paint)
                }
            }
        }
    }
}

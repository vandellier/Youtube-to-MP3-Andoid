package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalThemePalette

@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
    backgroundColor: Color? = null,
    cornerSize: Dp = 8.dp,
    showBrackets: Boolean = true,
    content: @Composable () -> Unit
) {
    val palette = LocalThemePalette.current
    val effectiveBorderColor = borderColor ?: palette.primary.copy(alpha = 0.45f)
    val effectiveBackgroundColor = backgroundColor ?: palette.surface.copy(alpha = 0.90f)

    Box(
        modifier = modifier
            .border(
                border = BorderStroke(1.dp, effectiveBorderColor),
                shape = RoundedCornerShape(cornerSize)
            )
            .background(
                color = effectiveBackgroundColor,
                shape = RoundedCornerShape(cornerSize)
            )
            .clip(RoundedCornerShape(cornerSize))
    ) {
        if (showBrackets) {
            // Draw tech corner brackets with theme bright color
            val bracketColor = palette.primaryBright.copy(alpha = 0.75f)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val bracketLen = 14.dp.toPx()
                val strokeW = 1.5.dp.toPx()

                // Top-Left bracket
                drawLine(bracketColor, Offset(0f, 0f), Offset(bracketLen, 0f), strokeW)
                drawLine(bracketColor, Offset(0f, 0f), Offset(0f, bracketLen), strokeW)

                // Top-Right bracket
                drawLine(bracketColor, Offset(size.width, 0f), Offset(size.width - bracketLen, 0f), strokeW)
                drawLine(bracketColor, Offset(size.width, 0f), Offset(size.width, bracketLen), strokeW)

                // Bottom-Left bracket
                drawLine(bracketColor, Offset(0f, size.height), Offset(bracketLen, size.height), strokeW)
                drawLine(bracketColor, Offset(0f, size.height), Offset(0f, size.height - bracketLen), strokeW)

                // Bottom-Right bracket
                drawLine(bracketColor, Offset(size.width, size.height), Offset(size.width - bracketLen, size.height), strokeW)
                drawLine(bracketColor, Offset(size.width, size.height), Offset(size.width, size.height - bracketLen), strokeW)
            }
        }
        content()
    }
}

@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    testTag: String = "neon_button",
    isPrimary: Boolean = true
) {
    val palette = LocalThemePalette.current
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    val borderBrush = if (enabled && isPrimary) {
        Brush.horizontalGradient(
            colors = listOf(
                palette.primary.copy(alpha = pulseGlow),
                palette.primaryBright.copy(alpha = pulseGlow),
                palette.primary.copy(alpha = pulseGlow)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                palette.primaryDark.copy(alpha = 0.6f),
                palette.primaryDark.copy(alpha = 0.6f)
            )
        )
    }

    val bgBrush = if (isPrimary && enabled) {
        Brush.verticalGradient(
            colors = listOf(
                palette.primaryDark.copy(alpha = 0.45f),
                palette.background.copy(alpha = 0.95f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                palette.surface.copy(alpha = 0.7f),
                palette.background.copy(alpha = 0.85f)
            )
        )
    }

    Box(
        modifier = modifier
            .testTag(testTag)
            .defaultMinSize(minHeight = 48.dp)
            .border(BorderStroke(1.5.dp, borderBrush), RoundedCornerShape(8.dp))
            .background(bgBrush, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = palette.primaryBright),
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) palette.primaryBright else palette.textSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = if (enabled) palette.textPrimary else palette.textSecondary.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
fun TerminalPrompt(
    command: String,
    modifier: Modifier = Modifier
) {
    val palette = LocalThemePalette.current
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(550),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_blink"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(palette.background.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
            .border(1.dp, palette.primaryDark, RoundedCornerShape(4.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "root@matrix:~$ ",
            color = palette.primaryBright,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = command,
            color = palette.textPrimary,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp
        )
        Text(
            text = "█",
            color = palette.primary.copy(alpha = cursorAlpha),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp
        )
    }
}

@Composable
fun CRTScanlineOverlay(
    modifier: Modifier = Modifier,
    lineSpacing: Dp = 4.dp,
    scanlineAlpha: Float = 0.08f
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val spacingPx = lineSpacing.toPx()
        val numLines = (size.height / spacingPx).toInt()
        val scanlineColor = Color.Black.copy(alpha = scanlineAlpha)

        for (i in 0 until numLines) {
            val y = i * spacingPx
            drawLine(
                color = scanlineColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
        }
    }
}

@Composable
fun AudioWaveVisualizer(
    bars: List<Float>,
    modifier: Modifier = Modifier,
    barColor: Color? = null,
    barWidth: Dp = 3.5.dp,
    spacing: Dp = 2.dp
) {
    val palette = LocalThemePalette.current
    val effectiveBarColor = barColor ?: palette.primaryBright

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        bars.forEach { amplitude ->
            val clampedAmp = amplitude.coerceIn(0.08f, 1.0f)
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(28.dp * clampedAmp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                effectiveBarColor,
                                palette.primaryDark
                            )
                        ),
                        shape = RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}

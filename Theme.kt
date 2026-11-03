package com.realityfx.ai.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Cyan = Color(0xFF22D3EE)
val Teal = Color(0xFF14B8A6)
val GlassFill = Color(0x1AFFFFFF)
val GlassBorder = Color(0x4022D3EE)
val MutedText = Color(0xFF9FB3C8)
val TextPrimary = Color(0xFFE6F4FA)

@Composable
fun RealityFxTheme(amoled: Boolean, content: @Composable () -> Unit) {
    val background = if (amoled) Color.Black else Color(0xFF0B0F14)
    val scheme = darkColorScheme(
        primary = Cyan,
        onPrimary = Color(0xFF00222A),
        secondary = Teal,
        onSecondary = Color(0xFF00201C),
        background = background,
        onBackground = TextPrimary,
        surface = Color(0xFF121A24),
        onSurface = TextPrimary,
        surfaceVariant = Color(0xFF1A2530),
        onSurfaceVariant = MutedText
    )
    MaterialTheme(colorScheme = scheme, typography = Typography(), content = content)
}

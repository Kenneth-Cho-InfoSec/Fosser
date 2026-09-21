package org.fosser.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import org.fosser.app.domain.model.ThemeMode
import org.fosser.app.domain.model.ThemeSettings

/**
 * Fosser theme, driven by [ThemeSettings].
 *
 * - matchSystemAccent on Android 12+: framework Material You dynamic schemes
 *   (Compose equivalent of Solipsism's values-v31 system overlays).
 * - Otherwise a custom scheme built from the accent seed, in Light / Dark /
 *   true-Black variants (Solipsism's per-theme overlays, Compose form).
 */
@Composable
fun FosserTheme(
    settings: ThemeSettings,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val dark = settings.mode.isDark(systemDark)
    val black = settings.mode == ThemeMode.BLACK
    val scheme = if (settings.matchSystemAccent && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        val dynamic = if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        if (black) dynamic.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceVariant = Color(0xFF141414),
            onSurfaceVariant = FosserMuted,
        ) else dynamic
    } else {
        customScheme(Color(settings.accent.seedArgb), dark, black)
    }
    MaterialTheme(colorScheme = scheme, content = content)
}

/** Pure custom-scheme builder (unit-testable): accent seed + Fosser brand surfaces. */
fun customScheme(seed: Color, dark: Boolean, black: Boolean): ColorScheme {
    val onSeed: Color = if (seed.luminance() > 0.5f) FosserInk else Color.White
    return if (!dark) {
        lightColorScheme(
            primary = seed,
            onPrimary = onSeed,
            primaryContainer = lerp(seed, Color.White, 0.82f),
            onPrimaryContainer = lerp(seed, FosserInk, 0.65f),
            secondary = FosserAmber,
            onSecondary = FosserInk,
            tertiary = FosserLike,
            background = FosserMist,
            onBackground = FosserInk,
            surface = Color.White,
            onSurface = FosserInk,
            surfaceVariant = FosserCard,
            onSurfaceVariant = FosserMist,
        )
    } else if (black) {
        darkColorScheme(
            primary = lerp(seed, Color.White, 0.15f),
            onPrimary = FosserInk,
            primaryContainer = lerp(seed, Color.Black, 0.55f),
            onPrimaryContainer = lerp(seed, Color.White, 0.8f),
            secondary = FosserAmber,
            onSecondary = FosserInk,
            tertiary = FosserLike,
            background = Color.Black,
            onBackground = FosserMist,
            surface = Color.Black,
            onSurface = FosserMist,
            surfaceVariant = Color(0xFF141414),
            onSurfaceVariant = FosserMuted,
        )
    } else {
        darkColorScheme(
            primary = seed,
            onPrimary = onSeed,
            primaryContainer = lerp(seed, Color.Black, 0.55f),
            onPrimaryContainer = lerp(seed, Color.White, 0.8f),
            secondary = FosserAmber,
            onSecondary = FosserInk,
            tertiary = FosserLike,
            background = FosserInk,
            onBackground = FosserMist,
            surface = FosserSlate,
            onSurface = FosserMist,
            surfaceVariant = FosserCard,
            onSurfaceVariant = FosserMist,
        )
    }
}

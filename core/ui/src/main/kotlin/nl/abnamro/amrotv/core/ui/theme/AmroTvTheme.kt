package nl.abnamro.amrotv.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * AMRO TV app theme using Material 3.
 *
 * Applies ABN Amro palette (green, gold, gray) with light/dark mode support.
 *
 * Dimensions are kept consistent across all device sizes per Material Design principles. Layout
 * adaptation (single column → multi-column) is handled at the composable level.
 *
 * @param darkTheme Whether to use dark color scheme. If null, uses system setting
 *   (isSystemInDarkTheme).
 * @param content Composable content to theme
 */
@Composable
fun AmroTvTheme(darkTheme: Boolean? = null, content: @Composable () -> Unit) {
    val useDarkTheme = darkTheme ?: isSystemInDarkTheme()

    val colorScheme =
        remember(useDarkTheme) {
            if (useDarkTheme) {
                AmroTvColors.darkColorScheme()
            } else {
                AmroTvColors.lightColorScheme()
            }
        }
    val typography = remember { amroTvTypography() }
    val shapes = remember { amroTvShapes() }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = shapes,
        content = content,
    )
}

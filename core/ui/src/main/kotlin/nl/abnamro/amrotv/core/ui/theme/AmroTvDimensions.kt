package nl.abnamro.amrotv.core.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Dimension values for the AMRO TV app.
 *
 * Centralized spacing, padding, and corner radius definitions used throughout the theme and
 * component hierarchy. All hardcoded dimensions should reference these values.
 *
 * Per Material Design principles, these dimensions are kept consistent across all device sizes
 * (phone, tablet, large screen). Layout adaptation (single column → multi-column) is handled by
 * composable structure, not dimension scaling.
 */
object AmroTvDimensions {
    // Corner radii
    val cornerRadiusExtraSmall = 4.dp
    val cornerRadiusSmall = 8.dp
    val cornerRadiusMedium = 16.dp
    val cornerRadiusLarge = 24.dp
    val cornerRadiusExtraLarge = 32.dp

    // Spacing and padding
    val spacingExtraSmall = 4.dp
    val spacingSmall = 8.dp
    val spacingMedium = 16.dp
    val spacingLarge = 24.dp
    val spacingExtraLarge = 32.dp

    // Component-specific sizes
    val errorIconSize = 64.dp
    val loadingIndicatorSize = 48.dp
    val emptyStateIconSize = 64.dp

    // Elevation
    val elevationSmall = 4.dp
}

package nl.abnamro.amrotv.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

/**
 * Material 3 shapes for the AMRO TV app.
 *
 * Uses corner radius values from [AmroTvDimensions] for consistent spacing.
 */
fun amroTvShapes(): Shapes = Shapes(
    extraSmall = RoundedCornerShape(AmroTvDimensions.cornerRadiusExtraSmall),
    small = RoundedCornerShape(AmroTvDimensions.cornerRadiusSmall),
    medium = RoundedCornerShape(AmroTvDimensions.cornerRadiusMedium),
    large = RoundedCornerShape(AmroTvDimensions.cornerRadiusLarge),
    extraLarge = RoundedCornerShape(AmroTvDimensions.cornerRadiusExtraLarge),
)

@file:Suppress("MagicNumber")

package nl.abnamro.amrotv.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Material 3 typography for the AMRO TV app.
 *
 * Uses system font stack for optimal performance and consistency.
 */
fun amroTvTypography(): Typography =
    Typography(
        displayLarge = displayLargeStyle(),
        displayMedium = displayMediumStyle(),
        displaySmall = displaySmallStyle(),
        headlineLarge = headlineLargeStyle(),
        headlineMedium = headlineMediumStyle(),
        headlineSmall = headlineSmallStyle(),
        titleLarge = titleLargeStyle(),
        titleMedium = titleMediumStyle(),
        titleSmall = titleSmallStyle(),
        bodyLarge = bodyLargeStyle(),
        bodyMedium = bodyMediumStyle(),
        bodySmall = bodySmallStyle(),
        labelLarge = labelLargeStyle(),
        labelMedium = labelMediumStyle(),
        labelSmall = labelSmallStyle(),
    )

// Display styles — light weight creates contrast against bold headlines below
private fun displayLargeStyle() =
    TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W300,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    )

private fun displayMediumStyle() =
    TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W300,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp,
    )

private fun displaySmallStyle() =
    TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W300,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
    )

// Headline styles — bold for strong visual hierarchy
private fun headlineLargeStyle() =
    TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W700,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp,
    )

private fun headlineMediumStyle() =
    TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W700,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.25).sp,
    )

private fun headlineSmallStyle() =
    TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W700,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    )

// Title styles — semi-bold to bold
private fun titleLargeStyle() =
    TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W700,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    )

private fun titleMediumStyle() =
    TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W600,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    )

private fun titleSmallStyle() =
    TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W600,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    )

// Body styles
private fun bodyLargeStyle() =
    TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W400,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    )

private fun bodyMediumStyle() =
    TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W400,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    )

private fun bodySmallStyle() =
    TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W400,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    )

// Label styles
private fun labelLargeStyle() =
    TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    )

private fun labelMediumStyle() =
    TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W500,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    )

private fun labelSmallStyle() =
    TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W500,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    )

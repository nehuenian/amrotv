package nl.abnamro.amrotv.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow

/**
 * ABN Amro palette colors for the AMRO TV app.
 *
 * This object defines explicit color specifications for the Material 3 theme. The
 * `lightColorScheme()` and `darkColorScheme()` functions do NOT automatically derive a complete
 * tonal palette from primary/secondary/tertiary colors alone. Instead, unspecified colors fall back
 * to Material 3 library defaults. To ensure the color scheme aligns with the ABN AMRO brand palette
 * and provides proper contrast across light and dark modes, this object explicitly specifies brand
 * colors, on-colors, and container roles for all key semantic roles.
 *
 * Brand palette:
 * - Primary: Green #009488 (main brand color, primary actions, highlights)
 * - Secondary: Gold #F9BD20 (accents, toggles, secondary actions)
 * - Tertiary: Gray #878787 (neutral secondary actions, disabled states)
 */
@Suppress("MagicNumber")
object AmroTvColors {
    val PrimaryGreen = Color(0xFF009488)
    val SecondaryGold = Color(0xFFF9BD20)
    val TertiaryGray = Color(0xFF878787)

    // On-media text — for text/icons overlaid on backdrop images
    val OnMediaPrimary = Color(0xFFFFFFFF) // titles, primary actions
    val OnMediaSecondary = Color(0xCCFFFFFF) // ratings, secondary labels (80% white)
    val OnMediaTertiary = Color(0xBFFFFFFF) // muted labels, e.g. release year (75% white)

    // Secondary text on primary-colored surfaces (e.g. TopAppBar subtitle)
    val OnPrimarySubtle = Color(0xCCFFFFFF) // 80% white — visible on primary green

    // Drop shadow applied to all text overlaid on backdrop images.
    // Ensures legibility regardless of the underlying image brightness.
    val OnMediaTextShadow =
        Shadow(color = Color.Black.copy(alpha = 0.75f), offset = Offset(0f, 1f), blurRadius = 6f)

    // Scrim gradients — for backdrop image overlays
    val MediaScrimTransparent =
        Color(0x00000000) // fully transparent gradient anchor / transparent surfaces
    val MediaScrimStart = Color(0x26000000) // 15% black — top of banner gradient
    val MediaScrimMid = Color(0x59000000) // 35% black — mid-point stop
    val MediaScrimEnd = Color(0xD9000000) // 85% black — bottom of card/banner gradient
    val MediaScrimDetailStart = Color(0x1A000000) // 10% black — top of detail screen gradient
    val MediaScrimDetailMid = Color(0x40000000) // 25% black — mid-point of detail gradient
    val MediaNavIconBackground =
        Color(0x66000000) // 40% black — circular background for nav icons over images

    // Light mode colors
    private val OnPrimaryLight = Color(0xFFFFFFFF)
    private val PrimaryContainerLight = Color(0xFFB8F2EB)
    private val OnPrimaryContainerLight = Color(0xFF00201D)

    private val OnSecondaryLight = Color(0xFF2A1800)
    private val SecondaryContainerLight = Color(0xFFFFDEA1)
    private val OnSecondaryContainerLight = Color(0xFF251A00)

    private val OnTertiaryLight = Color(0xFFFFFFFF)
    private val TertiaryContainerLight = Color(0xFFE3E2E2)
    private val OnTertiaryContainerLight = Color(0xFF1C1C1C)

    // Dark mode colors — using lighter variants for better contrast on dark surfaces
    private val OnPrimaryDark = Color(0xFFFFFFFF)
    private val PrimaryContainerDark = Color(0xFF00A398)
    private val OnPrimaryContainerDark = Color(0xFF002B27)

    private val OnSecondaryDark = Color(0xFF2A1800)
    private val SecondaryContainerDark = Color(0xFFF9BD20)
    private val OnSecondaryContainerDark = Color(0xFF401D00)

    private val OnTertiaryDark = Color(0xFFFFFFFF)
    private val TertiaryContainerDark = Color(0xFFA8A8A8)
    private val OnTertiaryContainerDark = Color(0xFF2D2D2D)

    /**
     * Creates a light Material 3 color scheme using ABN Amro brand colors.
     *
     * Explicitly specifies primary, secondary, tertiary, and their on/container roles to avoid
     * falling back to unrelated Material 3 defaults.
     */
    fun lightColorScheme(): ColorScheme =
        lightColorScheme(
            primary = PrimaryGreen,
            onPrimary = OnPrimaryLight,
            primaryContainer = PrimaryContainerLight,
            onPrimaryContainer = OnPrimaryContainerLight,
            secondary = SecondaryGold,
            onSecondary = OnSecondaryLight,
            secondaryContainer = SecondaryContainerLight,
            onSecondaryContainer = OnSecondaryContainerLight,
            tertiary = TertiaryGray,
            onTertiary = OnTertiaryLight,
            tertiaryContainer = TertiaryContainerLight,
            onTertiaryContainer = OnTertiaryContainerLight,
        )

    /**
     * Creates a dark Material 3 color scheme using ABN Amro brand colors.
     *
     * Uses lighter/brighter variants of the brand colors for proper contrast against dark surfaces.
     * Explicitly specifies primary, secondary, tertiary, and their on/container roles to ensure
     * visual clarity and alignment with the ABN AMRO brand in dark mode.
     */
    fun darkColorScheme(): ColorScheme =
        darkColorScheme(
            primary = PrimaryGreen,
            onPrimary = OnPrimaryDark,
            primaryContainer = PrimaryContainerDark,
            onPrimaryContainer = OnPrimaryContainerDark,
            secondary = SecondaryGold,
            onSecondary = OnSecondaryDark,
            secondaryContainer = SecondaryContainerDark,
            onSecondaryContainer = OnSecondaryContainerDark,
            tertiary = TertiaryGray,
            onTertiary = OnTertiaryDark,
            tertiaryContainer = TertiaryContainerDark,
            onTertiaryContainer = OnTertiaryContainerDark,
        )
}

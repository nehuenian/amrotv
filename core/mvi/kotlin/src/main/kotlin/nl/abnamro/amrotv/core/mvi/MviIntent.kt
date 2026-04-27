package nl.abnamro.amrotv.core.mvi

/**
 * Marker interface for all MVI screen intent hierarchies.
 *
 * Implementations must be `sealed interface`, using `data object` for intents without
 * payload and `data class` for intents that carry data.
 */
interface MviIntent

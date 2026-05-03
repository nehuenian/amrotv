package nl.abnamro.amrotv.core.mvi

/**
 * Marker interface for all MVI screen effect hierarchies.
 *
 * Effects represent one-time side-effect events (navigation, snackbars, URL opens) consumed exactly
 * once by the UI via [kotlinx.coroutines.flow.Flow.collect].
 */
interface MviEffect

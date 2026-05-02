package nl.abnamro.amrotv.feature.movies.presentation.api.moviedetail

import nl.abnamro.amrotv.core.mvi.MviEffect

/**
 * One-time side effects for the Movie Detail screen.
 *
 * Effects are consumed exactly once by the UI and are not replayed on recomposition.
 */
sealed interface MovieDetailEffect : MviEffect {

    /** Requests the UI to navigate back to the previous screen. */
    data object NavigateBack : MovieDetailEffect

    /** Requests the UI to open the given [url] in an external browser. */
    data class OpenUrl(val url: String) : MovieDetailEffect
}

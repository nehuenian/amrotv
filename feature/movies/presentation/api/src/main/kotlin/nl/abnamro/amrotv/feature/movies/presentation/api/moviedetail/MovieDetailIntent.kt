package nl.abnamro.amrotv.feature.movies.presentation.api.moviedetail

import nl.abnamro.amrotv.core.mvi.MviIntent

/**
 * User intents for the Movie Detail screen.
 *
 * Each subtype represents a distinct user action. Implementations must dispatch these via
 * [nl.abnamro.amrotv.core.mvi.AmroTvViewModel.handleIntent].
 */
sealed interface MovieDetailIntent : MviIntent {

    /** Triggers the initial load of the movie detail. */
    data object Load : MovieDetailIntent

    /** Requests navigation back to the previous screen. */
    data object NavigateBack : MovieDetailIntent

    /** Retries loading the movie detail after a failure. */
    data object Retry : MovieDetailIntent

    /** Requests navigation to the IMDB page for the given [imdbId]. */
    data class OpenImdb(val imdbId: String) : MovieDetailIntent
}

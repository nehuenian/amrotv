package nl.abnamro.amrotv.feature.movies.presentation.api.moviedetail

import nl.abnamro.amrotv.core.mvi.MviIntent

/**
 * User intents for the Movie Detail screen.
 *
 * Each subtype represents a distinct user action. Implementations must dispatch
 * these via [nl.abnamro.amrotv.core.mvi.AmroTvViewModel.handleIntent].
 */
sealed interface MovieDetailIntent : MviIntent {

    /** Requests the full detail for the given [movieId]. */
    data class LoadMovieDetail(val movieId: Int) : MovieDetailIntent

    /** Requests navigation to the IMDB page for the given [imdbId]. */
    data class OpenImdb(val imdbId: String) : MovieDetailIntent
}

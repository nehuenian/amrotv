package nl.abnamro.amrotv.feature.movies.presentation.api.trendingmovies

import nl.abnamro.amrotv.core.mvi.MviEffect

/**
 * One-time side effects for the Trending Movies screen.
 *
 * Effects are consumed exactly once by the UI and are not replayed on recomposition.
 */
sealed interface TrendingMoviesEffect : MviEffect {

    /** Requests navigation to the detail screen for the given [movieId]. */
    data class NavigateToMovieDetail(val movieId: Int) : TrendingMoviesEffect
}

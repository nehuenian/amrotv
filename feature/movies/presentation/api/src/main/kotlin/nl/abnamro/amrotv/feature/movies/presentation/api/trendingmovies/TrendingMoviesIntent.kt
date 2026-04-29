package nl.abnamro.amrotv.feature.movies.presentation.api.trendingmovies

import nl.abnamro.amrotv.core.mvi.MviIntent
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOption

/**
 * User intents for the Trending Movies screen.
 *
 * Each subtype represents a distinct user action. Implementations must dispatch
 * these via [nl.abnamro.amrotv.core.mvi.AmroTvViewModel.handleIntent].
 */
sealed interface TrendingMoviesIntent : MviIntent {

    /** Triggers an initial load of movies and genres. Sent automatically on ViewModel creation. */
    data object LoadMovies : TrendingMoviesIntent

    /** Applies a genre filter to the movie list, or clears it when [genreId] is null. */
    data class FilterByGenre(val genreId: Int?) : TrendingMoviesIntent

    /** Changes the field used for sorting the movie list. */
    data class ChangeSortOption(val sortOption: SortOption) : TrendingMoviesIntent

    /** Toggles the sort direction between ascending and descending. */
    data object ToggleSortOrder : TrendingMoviesIntent

    /** Requests navigation to the detail screen for the given [movieId]. */
    data class OpenMovieDetail(val movieId: Int) : TrendingMoviesIntent
}

package nl.abnamro.amrotv.feature.movies.presentation.api.trendingmovies

import nl.abnamro.amrotv.core.mvi.MviIntent
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOption
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOrder

/**
 * User intents for the Trending Movies screen.
 *
 * Each subtype represents a distinct user action. Implementations must dispatch these via
 * [nl.abnamro.amrotv.core.mvi.AmroTvViewModel.handleIntent].
 */
sealed interface TrendingMoviesIntent : MviIntent {

    /** Triggers an initial load of movies and genres. Sent automatically on ViewModel creation. */
    data object LoadMovies : TrendingMoviesIntent

    /** Applies a genre filter to the movie list, or clears it when [genreId] is null. */
    data class FilterByGenre(val genreId: Int?) : TrendingMoviesIntent

    /** Changes the field used for sorting the movie list. */
    data class ChangeSortOption(val sortOption: SortOption) : TrendingMoviesIntent

    /** Selects the sort direction. No-op if [order] is already active. */
    data class SelectSortOrder(val order: SortOrder) : TrendingMoviesIntent

    /** Controls the visibility of the sort options bottom sheet. */
    data class SetSortSheetVisible(val visible: Boolean) : TrendingMoviesIntent

    /** Requests navigation to the detail screen for the given [movieId]. */
    data class OpenMovieDetail(val movieId: Int) : TrendingMoviesIntent
}

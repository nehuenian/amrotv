package nl.abnamro.amrotv.feature.movies.presentation.implementation.trendingmovies

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import nl.abnamro.amrotv.core.mvi.StateReducer
import nl.abnamro.amrotv.feature.movies.domain.api.model.Genre
import nl.abnamro.amrotv.feature.movies.domain.api.model.Movie
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOption
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOrder
import nl.abnamro.amrotv.feature.movies.presentation.api.MovieError
import nl.abnamro.amrotv.feature.movies.presentation.api.trendingmovies.TrendingMoviesState
import javax.inject.Inject

class TrendingMoviesStateReducers @Inject constructor() {

    fun initialState(): TrendingMoviesState = TrendingMoviesState()

    fun loading(): StateReducer<TrendingMoviesState> =
        StateReducer { it.copy(isLoading = true, errors = persistentListOf()) }

    fun contentLoaded(
        movies: List<Movie>,
        genres: List<Genre>,
        errors: List<MovieError> = emptyList(),
    ): StateReducer<TrendingMoviesState> =
        StateReducer {
            it.copy(
                isLoading = false,
                movies = movies.toPersistentList(),
                genres = genres.toPersistentList(),
                errors = errors.toPersistentList()
            )
        }

    fun filterByGenre(genreId: Int?, movies: List<Movie>): StateReducer<TrendingMoviesState> =
        StateReducer { it.copy(selectedGenreId = genreId, movies = movies.toPersistentList()) }

    fun changeSortOption(
        sortOption: SortOption,
        movies: List<Movie>
    ): StateReducer<TrendingMoviesState> =
        StateReducer {
            it.copy(
                selectedSortOption = sortOption,
                movies = movies.toPersistentList()
            )
        }

    fun toggleSortOrder(
        sortOrder: SortOrder,
        movies: List<Movie>
    ): StateReducer<TrendingMoviesState> =
        StateReducer { it.copy(selectedSortOrder = sortOrder, movies = movies.toPersistentList()) }

    fun loadFailed(errors: List<MovieError>): StateReducer<TrendingMoviesState> =
        StateReducer { it.copy(isLoading = false, errors = errors.toPersistentList()) }
}

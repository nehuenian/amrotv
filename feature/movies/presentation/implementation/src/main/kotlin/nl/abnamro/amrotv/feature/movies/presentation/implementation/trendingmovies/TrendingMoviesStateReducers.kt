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
import nl.abnamro.amrotv.feature.movies.presentation.implementation.mapper.GenreDomainToPresentationMapper
import nl.abnamro.amrotv.feature.movies.presentation.implementation.mapper.MovieDomainToPresentationMapper
import javax.inject.Inject

class TrendingMoviesStateReducers @Inject constructor(
    private val movieDomainToPresentationMapper: MovieDomainToPresentationMapper,
    private val genreDomainToPresentationMapper: GenreDomainToPresentationMapper,
    private val weekRangeLabelProvider: WeekRangeLabelProvider,
) {

    fun initialState(): TrendingMoviesState = TrendingMoviesState(
        weekRangeLabel = weekRangeLabelProvider.currentWeekRangeLabel(),
    )

    fun loading(): StateReducer<TrendingMoviesState> =
        StateReducer { it.copy(isLoading = true, errors = persistentListOf()) }

    fun contentLoaded(
        movies: List<Movie>,
        genres: List<Genre>,
        errors: List<MovieError> = emptyList(),
    ): StateReducer<TrendingMoviesState> {
        val label = weekRangeLabelProvider.currentWeekRangeLabel()
        return StateReducer {
            it.copy(
                isLoading = false,
                weekRangeLabel = label,
                movies = mapMovies(movies),
                genres = mapGenres(genres),
                errors = errors.toPersistentList()
            )
        }
    }

    fun filterByGenre(genreId: Int?, movies: List<Movie>): StateReducer<TrendingMoviesState> =
        StateReducer { it.copy(selectedGenreId = genreId, movies = mapMovies(movies)) }

    fun changeSortOption(
        sortOption: SortOption,
        movies: List<Movie>
    ): StateReducer<TrendingMoviesState> =
        StateReducer {
            it.copy(
                selectedSortOption = sortOption,
                movies = mapMovies(movies)
            )
        }

    fun selectSortOrder(
        sortOrder: SortOrder,
        movies: List<Movie>
    ): StateReducer<TrendingMoviesState> =
        StateReducer { it.copy(selectedSortOrder = sortOrder, movies = mapMovies(movies)) }

    private fun mapMovies(movies: List<Movie>) =
        movies.map { movieDomainToPresentationMapper.map(it) }.toPersistentList()

    private fun mapGenres(genres: List<Genre>) =
        genres.map { genreDomainToPresentationMapper.map(it) }.toPersistentList()

    fun loadFailed(errors: List<MovieError>): StateReducer<TrendingMoviesState> =
        StateReducer { it.copy(isLoading = false, errors = errors.toPersistentList()) }

    fun sortSheetVisible(visible: Boolean): StateReducer<TrendingMoviesState> =
        StateReducer { it.copy(showSortSheet = visible) }
}

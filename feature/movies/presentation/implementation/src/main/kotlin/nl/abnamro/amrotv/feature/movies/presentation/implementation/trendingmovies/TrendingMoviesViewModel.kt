package nl.abnamro.amrotv.feature.movies.presentation.implementation.trendingmovies

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import nl.abnamro.amrotv.core.mvi.BaseAmroTvViewModel
import nl.abnamro.amrotv.core.mvi.reduceWith
import nl.abnamro.amrotv.feature.movies.domain.api.model.Genre
import nl.abnamro.amrotv.feature.movies.domain.api.model.Movie
import nl.abnamro.amrotv.core.domain.model.Outcome
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOrder
import nl.abnamro.amrotv.feature.movies.domain.api.usecase.FilterAndSortMoviesUseCase
import nl.abnamro.amrotv.feature.movies.domain.api.usecase.GetGenresUseCase
import nl.abnamro.amrotv.feature.movies.domain.api.usecase.GetTrendingMoviesUseCase
import nl.abnamro.amrotv.feature.movies.presentation.api.MovieError
import nl.abnamro.amrotv.feature.movies.presentation.api.trendingmovies.TrendingMoviesEffect
import nl.abnamro.amrotv.feature.movies.presentation.api.trendingmovies.TrendingMoviesIntent
import nl.abnamro.amrotv.feature.movies.presentation.api.trendingmovies.TrendingMoviesState
import nl.abnamro.amrotv.libraries.logger.api.LogLevel
import nl.abnamro.amrotv.libraries.logger.api.Logger
import javax.inject.Inject

@HiltViewModel
class TrendingMoviesViewModel @Inject constructor(
    private val getTrendingMoviesUseCase: GetTrendingMoviesUseCase,
    private val getGenresUseCase: GetGenresUseCase,
    private val stateReducers: TrendingMoviesStateReducers,
    private val filterAndSortMoviesUseCase: FilterAndSortMoviesUseCase,
    private val logger: Logger,
) : BaseAmroTvViewModel<TrendingMoviesState, TrendingMoviesIntent, TrendingMoviesEffect>(
    initialState = stateReducers.initialState(),
) {

    private var allMovies: List<Movie> = emptyList()

    init {
        handleIntent(TrendingMoviesIntent.LoadMovies)
    }

    override fun handleIntent(intent: TrendingMoviesIntent) {
        when (intent) {
            TrendingMoviesIntent.LoadMovies -> {
                updateState { it.reduceWith(stateReducers.loading()) }
                loadData()
            }

            is TrendingMoviesIntent.FilterByGenre -> {
                updateState {
                    val filtered = filterAndSortMoviesUseCase(
                        allMovies,
                        intent.genreId,
                        it.selectedSortOption,
                        it.selectedSortOrder
                    )
                    it.reduceWith(stateReducers.filterByGenre(intent.genreId, filtered))
                }
            }

            is TrendingMoviesIntent.ChangeSortOption -> {
                updateState {
                    val sorted = filterAndSortMoviesUseCase(
                        allMovies,
                        it.selectedGenreId,
                        intent.sortOption,
                        it.selectedSortOrder
                    )
                    it.reduceWith(
                        stateReducers.changeSortOption(
                            intent.sortOption,
                            sorted
                        )
                    )
                }
            }

            TrendingMoviesIntent.ToggleSortOrder -> {
                updateState {
                    val newOrder =
                        if (it.selectedSortOrder == SortOrder.ASC) SortOrder.DESC else SortOrder.ASC
                    val sorted = filterAndSortMoviesUseCase(
                        allMovies,
                        it.selectedGenreId,
                        it.selectedSortOption,
                        newOrder
                    )
                    it.reduceWith(stateReducers.toggleSortOrder(newOrder, sorted))
                }
            }

            is TrendingMoviesIntent.OpenMovieDetail ->
                sendEffect(TrendingMoviesEffect.NavigateToMovieDetail(intent.movieId))
        }
    }

    private fun loadData() {
        val currentState = state.value
        viewModelScope.launch {
            coroutineScope {
                val moviesDeferred = async {
                    getTrendingMoviesUseCase()
                }

                val genresResult = getGenresUseCase()
                val genres: List<Genre> = when (genresResult) {
                    is Outcome.Success -> genresResult.data
                    is Outcome.Error -> {
                        logger.log(
                            LogLevel.ERROR,
                            TAG,
                            "Failed to load genres: ${genresResult.cause.message}",
                            genresResult.cause
                        )
                        currentState.genres
                    }
                }

                when (val moviesResult = moviesDeferred.await()) {
                    is Outcome.Success -> {
                        val errors = buildList {
                            if (genresResult is Outcome.Error) add(MovieError.GENRES_LOAD_FAILED)
                        }
                        allMovies = moviesResult.data
                        updateState {
                            val movies = filterAndSortMoviesUseCase(
                                allMovies,
                                it.selectedGenreId,
                                it.selectedSortOption,
                                it.selectedSortOrder
                            )
                            it.reduceWith(
                                stateReducers.contentLoaded(
                                    movies,
                                    genres,
                                    errors = errors
                                )
                            )
                        }
                    }

                    is Outcome.Error -> {
                        logger.log(
                            LogLevel.ERROR,
                            TAG,
                            "Failed to load movies: ${moviesResult.cause.message}",
                            moviesResult.cause
                        )
                        val errors = buildList {
                            add(MovieError.MOVIES_LOAD_FAILED)
                            if (genresResult is Outcome.Error) add(MovieError.GENRES_LOAD_FAILED)
                        }
                        val staleMovies = moviesResult.data
                        if (staleMovies != null) {
                            allMovies = staleMovies
                            updateState {
                                val movies = filterAndSortMoviesUseCase(
                                    allMovies,
                                    it.selectedGenreId,
                                    it.selectedSortOption,
                                    it.selectedSortOrder
                                )
                                it.reduceWith(
                                    stateReducers.contentLoaded(
                                        movies,
                                        genres,
                                        errors = errors
                                    )
                                )
                            }
                        } else {
                            updateState { it.reduceWith(stateReducers.loadFailed(errors)) }
                        }
                    }
                }
            }
        }
    }

    private companion object {
        const val TAG = "TrendingMoviesViewModel"
    }
}

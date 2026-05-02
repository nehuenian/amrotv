package nl.abnamro.amrotv.feature.movies.presentation.implementation.trendingmovies

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import nl.abnamro.amrotv.core.domain.model.Outcome
import nl.abnamro.amrotv.core.mvi.BaseAmroTvViewModel
import nl.abnamro.amrotv.core.mvi.reduceWith
import nl.abnamro.amrotv.feature.movies.domain.api.model.Genre
import nl.abnamro.amrotv.feature.movies.domain.api.model.Movie
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
                updateState { currentState ->
                    val filtered = filterAndSortMoviesUseCase(
                        allMovies,
                        intent.genreId,
                        currentState.selectedSortOption,
                        currentState.selectedSortOrder,
                    )
                    currentState.reduceWith(stateReducers.filterByGenre(intent.genreId, filtered))
                }
            }

            is TrendingMoviesIntent.ChangeSortOption -> {
                updateState { currentState ->
                    val sorted = filterAndSortMoviesUseCase(
                        allMovies,
                        currentState.selectedGenreId,
                        intent.sortOption,
                        currentState.selectedSortOrder,
                    )
                    currentState.reduceWith(stateReducers.changeSortOption(intent.sortOption, sorted))
                }
            }

            is TrendingMoviesIntent.SelectSortOrder -> {
                updateState { currentState ->
                    if (currentState.selectedSortOrder == intent.order) return@updateState currentState
                    val sorted = filterAndSortMoviesUseCase(
                        allMovies,
                        currentState.selectedGenreId,
                        currentState.selectedSortOption,
                        intent.order,
                    )
                    currentState.reduceWith(stateReducers.selectSortOrder(intent.order, sorted))
                }
            }

            is TrendingMoviesIntent.OpenMovieDetail ->
                sendEffect(TrendingMoviesEffect.NavigateToMovieDetail(intent.movieId))

            is TrendingMoviesIntent.SetSortSheetVisible ->
                updateState { it.reduceWith(stateReducers.sortSheetVisible(intent.visible)) }
        }
    }

    private fun loadData() {
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
                        genresResult.data.orEmpty()
                    }
                }

                when (val moviesResult = moviesDeferred.await()) {
                    is Outcome.Success -> {
                        val errors = buildList {
                            if (genresResult is Outcome.Error) add(MovieError.GENRES_LOAD_FAILED)
                        }
                        allMovies = moviesResult.data
                        updateState { currentState ->
                            val movies = filterAndSortMoviesUseCase(
                                allMovies,
                                currentState.selectedGenreId,
                                currentState.selectedSortOption,
                                currentState.selectedSortOrder,
                            )
                            currentState.reduceWith(
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
                            updateState { currentState ->
                                val movies = filterAndSortMoviesUseCase(
                                    allMovies,
                                    currentState.selectedGenreId,
                                    currentState.selectedSortOption,
                                    currentState.selectedSortOrder,
                                )
                                currentState.reduceWith(
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

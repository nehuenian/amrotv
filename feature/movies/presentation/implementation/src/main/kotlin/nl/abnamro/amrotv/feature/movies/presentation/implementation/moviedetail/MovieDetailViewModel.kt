package nl.abnamro.amrotv.feature.movies.presentation.implementation.moviedetail

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import nl.abnamro.amrotv.core.domain.model.Outcome
import nl.abnamro.amrotv.core.mvi.BaseAmroTvViewModel
import nl.abnamro.amrotv.core.mvi.reduceWith
import nl.abnamro.amrotv.feature.movies.domain.api.usecase.GetMovieDetailUseCase
import nl.abnamro.amrotv.feature.movies.presentation.api.MovieError
import nl.abnamro.amrotv.feature.movies.presentation.api.moviedetail.MovieDetailEffect
import nl.abnamro.amrotv.feature.movies.presentation.api.moviedetail.MovieDetailIntent
import nl.abnamro.amrotv.feature.movies.presentation.api.moviedetail.MovieDetailState
import nl.abnamro.amrotv.libraries.logger.api.LogLevel
import nl.abnamro.amrotv.libraries.logger.api.Logger

internal const val IMDB_TITLE_BASE_URL = "https://www.imdb.com/title/"

@HiltViewModel(assistedFactory = MovieDetailViewModel.Factory::class)
class MovieDetailViewModel
@AssistedInject
constructor(
    @Assisted private val movieId: Int,
    private val getMovieDetailUseCase: GetMovieDetailUseCase,
    private val stateReducers: MovieDetailStateReducers,
    private val logger: Logger,
) :
    BaseAmroTvViewModel<MovieDetailState, MovieDetailIntent, MovieDetailEffect>(
        initialState = stateReducers.initialState()
    ) {

    @AssistedFactory
    interface Factory {
        fun create(movieId: Int): MovieDetailViewModel
    }

    init {
        handleIntent(MovieDetailIntent.Load)
    }

    override fun handleIntent(intent: MovieDetailIntent) {
        when (intent) {
            is MovieDetailIntent.Load -> {
                updateState { it.reduceWith(stateReducers.loading()) }
                loadDetail(movieId)
            }

            is MovieDetailIntent.Retry -> handleIntent(MovieDetailIntent.Load)

            is MovieDetailIntent.NavigateBack -> sendEffect(MovieDetailEffect.NavigateBack)

            is MovieDetailIntent.OpenImdb -> {
                sendEffect(MovieDetailEffect.OpenUrl("$IMDB_TITLE_BASE_URL${intent.imdbId}/"))
            }
        }
    }

    private var loadJob: Job? = null

    private fun loadDetail(movieId: Int) {
        loadJob?.cancel()
        loadJob =
            viewModelScope.launch {
                when (val result = getMovieDetailUseCase(movieId)) {
                    is Outcome.Success ->
                        updateState { it.reduceWith(stateReducers.detailLoaded(result.data)) }
                    is Outcome.Error -> {
                        logger.log(
                            LogLevel.ERROR,
                            TAG,
                            "Failed to load movie detail for id=$movieId: ${result.cause.message}",
                            result.cause,
                        )
                        updateState {
                            it.reduceWith(
                                stateReducers.loadFailed(
                                    listOf(MovieError.MOVIE_DETAIL_LOAD_FAILED)
                                )
                            )
                        }
                    }
                }
            }
    }

    private companion object {
        const val TAG = "MovieDetailViewModel"
    }
}

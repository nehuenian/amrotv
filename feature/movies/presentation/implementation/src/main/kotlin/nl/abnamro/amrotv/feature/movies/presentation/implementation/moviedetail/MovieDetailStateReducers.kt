package nl.abnamro.amrotv.feature.movies.presentation.implementation.moviedetail

import javax.inject.Inject
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import nl.abnamro.amrotv.core.mvi.StateReducer
import nl.abnamro.amrotv.feature.movies.domain.api.model.MovieDetail
import nl.abnamro.amrotv.feature.movies.presentation.api.MovieError
import nl.abnamro.amrotv.feature.movies.presentation.api.moviedetail.MovieDetailState
import nl.abnamro.amrotv.feature.movies.presentation.implementation.mapper.MovieDetailDomainToPresentationMapper

class MovieDetailStateReducers
@Inject
constructor(private val movieDetailMapper: MovieDetailDomainToPresentationMapper) {

    fun initialState(): MovieDetailState = MovieDetailState()

    fun loading(): StateReducer<MovieDetailState> = StateReducer {
        it.copy(isLoading = true, errors = persistentListOf())
    }

    fun detailLoaded(movieDetail: MovieDetail): StateReducer<MovieDetailState> = StateReducer {
        it.copy(
            isLoading = false,
            movieDetail = movieDetailMapper.map(movieDetail),
            errors = persistentListOf(),
        )
    }

    fun loadFailed(errors: List<MovieError>): StateReducer<MovieDetailState> = StateReducer {
        it.copy(isLoading = false, errors = errors.toPersistentList())
    }
}

package nl.abnamro.amrotv.feature.movies.presentation.api.moviedetail

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.abnamro.amrotv.core.mvi.MviState
import nl.abnamro.amrotv.feature.movies.domain.api.model.MovieDetail
import nl.abnamro.amrotv.feature.movies.presentation.api.MovieError

data class MovieDetailState(
    val isLoading: Boolean = false,
    val movieDetail: MovieDetail? = null,
    val errors: ImmutableList<MovieError> = persistentListOf(),
) : MviState

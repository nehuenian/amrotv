package nl.abnamro.amrotv.feature.movies.presentation.api.moviedetail

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.abnamro.amrotv.core.mvi.MviState
import nl.abnamro.amrotv.feature.movies.presentation.api.MovieError
import nl.abnamro.amrotv.feature.movies.presentation.api.model.MovieDetailPresentationModel

data class MovieDetailState(
    val isLoading: Boolean = false,
    val movieDetail: MovieDetailPresentationModel? = null,
    val errors: ImmutableList<MovieError> = persistentListOf(),
) : MviState

package nl.abnamro.amrotv.feature.movies.presentation.api.trendingmovies

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.abnamro.amrotv.core.mvi.MviState
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOption
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOrder
import nl.abnamro.amrotv.feature.movies.presentation.api.MovieError
import nl.abnamro.amrotv.feature.movies.presentation.api.model.GenrePresentationModel
import nl.abnamro.amrotv.feature.movies.presentation.api.model.MoviePresentationModel

data class TrendingMoviesState(
    val isLoading: Boolean = false,
    val movies: ImmutableList<MoviePresentationModel> = persistentListOf(),
    val genres: ImmutableList<GenrePresentationModel> = persistentListOf(),
    val selectedGenreId: Int? = null,
    val selectedSortOption: SortOption = SortOption.POPULARITY,
    val selectedSortOrder: SortOrder = SortOrder.DESC,
    val showSortSheet: Boolean = false,
    val errors: ImmutableList<MovieError> = persistentListOf(),
    val weekRangeLabel: String = "",
) : MviState

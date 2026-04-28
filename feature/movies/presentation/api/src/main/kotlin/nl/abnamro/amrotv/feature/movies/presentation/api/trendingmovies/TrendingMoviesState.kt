package nl.abnamro.amrotv.feature.movies.presentation.api.trendingmovies

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.abnamro.amrotv.core.mvi.MviState
import nl.abnamro.amrotv.feature.movies.domain.api.model.Genre
import nl.abnamro.amrotv.feature.movies.domain.api.model.Movie
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOption
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOrder
import nl.abnamro.amrotv.feature.movies.presentation.api.MovieError

data class TrendingMoviesState(
    val isLoading: Boolean = false,
    val movies: ImmutableList<Movie> = persistentListOf(),
    val genres: ImmutableList<Genre> = persistentListOf(),
    val selectedGenreId: Int? = null,
    val selectedSortOption: SortOption = SortOption.POPULARITY,
    val selectedSortOrder: SortOrder = SortOrder.DESC,
    val errors: ImmutableList<MovieError> = persistentListOf(),
) : MviState

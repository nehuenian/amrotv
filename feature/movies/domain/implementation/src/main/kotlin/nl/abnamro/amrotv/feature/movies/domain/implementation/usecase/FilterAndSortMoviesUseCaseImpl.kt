package nl.abnamro.amrotv.feature.movies.domain.implementation.usecase

import javax.inject.Inject
import nl.abnamro.amrotv.feature.movies.domain.api.model.Movie
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOption
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOrder
import nl.abnamro.amrotv.feature.movies.domain.api.usecase.FilterAndSortMoviesUseCase

internal class FilterAndSortMoviesUseCaseImpl @Inject constructor() : FilterAndSortMoviesUseCase {

    override fun invoke(
        movies: List<Movie>,
        genreId: Int?,
        sortOption: SortOption,
        sortOrder: SortOrder,
    ): List<Movie> {
        val filtered = if (genreId != null) movies.filter { genreId in it.genreIds } else movies
        return when (sortOrder) {
            SortOrder.ASC ->
                when (sortOption) {
                    SortOption.POPULARITY -> filtered.sortedBy { it.popularity }
                    SortOption.TITLE -> filtered.sortedBy { it.title }
                    SortOption.RELEASE_DATE -> filtered.sortedBy { it.releaseDate }
                }

            SortOrder.DESC ->
                when (sortOption) {
                    SortOption.POPULARITY -> filtered.sortedByDescending { it.popularity }
                    SortOption.TITLE -> filtered.sortedByDescending { it.title }
                    SortOption.RELEASE_DATE -> filtered.sortedByDescending { it.releaseDate }
                }
        }
    }
}

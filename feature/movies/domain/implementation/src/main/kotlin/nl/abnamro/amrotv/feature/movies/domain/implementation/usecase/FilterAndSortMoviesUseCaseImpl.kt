package nl.abnamro.amrotv.feature.movies.domain.implementation.usecase

import java.util.Comparator.naturalOrder
import java.util.Comparator.nullsLast
import java.util.Comparator.reverseOrder
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
                    SortOption.RELEASE_DATE -> filtered.sortedWith(releaseDateAscComparator)
                }
            SortOrder.DESC ->
                when (sortOption) {
                    SortOption.POPULARITY -> filtered.sortedByDescending { it.popularity }
                    SortOption.TITLE -> filtered.sortedByDescending { it.title }
                    SortOption.RELEASE_DATE -> filtered.sortedWith(releaseDateDescComparator)
                }
        }
    }

    private companion object {
        private val releaseDateAscComparator: Comparator<Movie> =
            Comparator.comparing({ movie: Movie -> movie.releaseDate }, nullsLast(naturalOrder()))
        private val releaseDateDescComparator: Comparator<Movie> =
            Comparator.comparing({ movie: Movie -> movie.releaseDate }, nullsLast(reverseOrder()))
    }
}

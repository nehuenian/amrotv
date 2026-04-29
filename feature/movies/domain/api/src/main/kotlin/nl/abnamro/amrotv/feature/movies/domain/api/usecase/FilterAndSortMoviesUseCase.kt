package nl.abnamro.amrotv.feature.movies.domain.api.usecase

import nl.abnamro.amrotv.feature.movies.domain.api.model.Movie
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOption
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOrder

/**
 * Filters and sorts a list of [Movie] objects in memory without any I/O.
 */
fun interface FilterAndSortMoviesUseCase {

    /**
     * Applies optional genre filtering and sorting to [movies].
     *
     * @param movies the full unfiltered movie list to operate on.
     * @param genreId when non-null, only movies whose genre list contains this ID are returned.
     * @param sortOption the field to sort the result by.
     * @param sortOrder ascending or descending direction for the sort.
     * @return the filtered and sorted subset of [movies].
     */
    operator fun invoke(
        movies: List<Movie>,
        genreId: Int?,
        sortOption: SortOption,
        sortOrder: SortOrder,
    ): List<Movie>
}

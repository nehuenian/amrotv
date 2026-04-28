package nl.abnamro.amrotv.feature.movies.domain.api.usecase

import nl.abnamro.amrotv.core.domain.model.Outcome
import nl.abnamro.amrotv.feature.movies.domain.api.model.Movie
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOption
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOrder

/**
 * Retrieves the trending movies list, applying optional genre filtering and sorting. Filtering and
 * sorting are applied client-side.
 */
fun interface GetTrendingMoviesUseCase {

    /**
     * Fetches, filters, and sorts the trending movies list.
     *
     * @param genreId when non-null, only movies whose genre list contains this ID are returned.
     * @param sortOption the field to sort the result by.
     * @param sortOrder ascending or descending direction for the sort.
     * @return [Outcome.Success] with the filtered and sorted movie list, or [Outcome.Error] on failure.
     */
    suspend operator fun invoke(
        genreId: Int?,
        sortOption: SortOption,
        sortOrder: SortOrder,
    ): Outcome<List<Movie>>
}

package nl.abnamro.amrotv.feature.movies.domain.api.usecase

import nl.abnamro.amrotv.core.domain.model.Outcome
import nl.abnamro.amrotv.feature.movies.domain.api.model.Movie

/**
 * Retrieves the full list of trending movies without any filtering or sorting.
 *
 * Use [FilterAndSortMoviesUseCase] to apply genre filtering and sorting on the result.
 */
fun interface GetTrendingMoviesUseCase {

    /**
     * Fetches the complete trending movies list from the data source.
     *
     * @return [Outcome.Success] with the full unfiltered movie list, or [Outcome.Error] on failure.
     *   [Outcome.Error] may carry stale cached data.
     */
    suspend operator fun invoke(): Outcome<List<Movie>>
}

package nl.abnamro.amrotv.feature.movies.domain.implementation.usecase

import nl.abnamro.amrotv.feature.movies.domain.api.model.Movie
import nl.abnamro.amrotv.feature.movies.domain.api.model.Outcome
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOption
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOrder
import nl.abnamro.amrotv.feature.movies.domain.api.repository.MovieRepository
import nl.abnamro.amrotv.feature.movies.domain.api.usecase.GetTrendingMoviesUseCase
import javax.inject.Inject

internal class GetTrendingMoviesUseCaseImpl @Inject constructor(
    private val movieRepository: MovieRepository,
) : GetTrendingMoviesUseCase {

    override suspend fun invoke(
        genreId: Int?,
        sortOption: SortOption,
        sortOrder: SortOrder
    ): Outcome<List<Movie>> {
        return when (val outcome = movieRepository.getTrendingMovies()) {
            is Outcome.Success -> {
                Outcome.Success(data = outcome.data.filterAndSort(genreId, sortOption, sortOrder))
            }

            is Outcome.Error -> {
                Outcome.Error(
                    outcome.cause,
                    data = outcome.data?.filterAndSort(genreId, sortOption, sortOrder)
                )
            }
        }
    }

    private fun List<Movie>.filterAndSort(
        genreId: Int?,
        sortOption: SortOption,
        sortOrder: SortOrder
    ): List<Movie> {
        val filtered = filterByGenre(genreId)
        return filtered.sortBy(sortOption, sortOrder)
    }

    private fun List<Movie>.filterByGenre(genreId: Int?): List<Movie> =
        if (genreId != null) filter { genreId in it.genreIds } else this

    private fun List<Movie>.sortBy(sortOption: SortOption, sortOrder: SortOrder): List<Movie> =
        when (sortOrder) {
            SortOrder.ASC -> when (sortOption) {
                SortOption.POPULARITY -> sortedBy { it.popularity }
                SortOption.TITLE -> sortedBy { it.title }
                SortOption.RELEASE_DATE -> sortedBy { it.releaseDate }
            }

            SortOrder.DESC -> when (sortOption) {
                SortOption.POPULARITY -> sortedByDescending { it.popularity }
                SortOption.TITLE -> sortedByDescending { it.title }
                SortOption.RELEASE_DATE -> sortedByDescending { it.releaseDate }
            }
        }
}

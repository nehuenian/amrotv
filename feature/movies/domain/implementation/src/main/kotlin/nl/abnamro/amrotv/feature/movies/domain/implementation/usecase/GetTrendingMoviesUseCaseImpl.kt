package nl.abnamro.amrotv.feature.movies.domain.implementation.usecase

import nl.abnamro.amrotv.core.domain.model.Outcome
import nl.abnamro.amrotv.feature.movies.domain.api.model.Movie
import nl.abnamro.amrotv.feature.movies.domain.api.repository.MovieRepository
import nl.abnamro.amrotv.feature.movies.domain.api.usecase.GetTrendingMoviesUseCase
import javax.inject.Inject

internal class GetTrendingMoviesUseCaseImpl @Inject constructor(
    private val movieRepository: MovieRepository,
) : GetTrendingMoviesUseCase {

    override suspend fun invoke(): Outcome<List<Movie>> = movieRepository.getTrendingMovies()
}


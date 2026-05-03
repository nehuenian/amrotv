package nl.abnamro.amrotv.feature.movies.domain.implementation.usecase

import javax.inject.Inject
import nl.abnamro.amrotv.core.domain.model.Outcome
import nl.abnamro.amrotv.feature.movies.domain.api.model.Movie
import nl.abnamro.amrotv.feature.movies.domain.api.repository.MovieRepository
import nl.abnamro.amrotv.feature.movies.domain.api.usecase.GetTrendingMoviesUseCase

internal class GetTrendingMoviesUseCaseImpl
@Inject
constructor(private val movieRepository: MovieRepository) : GetTrendingMoviesUseCase {

    override suspend fun invoke(): Outcome<List<Movie>> = movieRepository.getTrendingMovies()
}

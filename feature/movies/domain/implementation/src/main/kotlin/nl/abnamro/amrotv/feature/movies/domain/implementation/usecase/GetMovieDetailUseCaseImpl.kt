package nl.abnamro.amrotv.feature.movies.domain.implementation.usecase

import javax.inject.Inject
import nl.abnamro.amrotv.core.domain.model.Outcome
import nl.abnamro.amrotv.feature.movies.domain.api.model.MovieDetail
import nl.abnamro.amrotv.feature.movies.domain.api.repository.MovieRepository
import nl.abnamro.amrotv.feature.movies.domain.api.usecase.GetMovieDetailUseCase

internal class GetMovieDetailUseCaseImpl @Inject constructor(
    private val movieRepository: MovieRepository,
) : GetMovieDetailUseCase {

    override suspend fun invoke(movieId: Int): Outcome<MovieDetail> = movieRepository.getMovieDetail(movieId)
}

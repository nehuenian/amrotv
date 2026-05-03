package nl.abnamro.amrotv.feature.movies.domain.implementation.usecase

import javax.inject.Inject
import nl.abnamro.amrotv.core.domain.model.Outcome
import nl.abnamro.amrotv.feature.movies.domain.api.model.Genre
import nl.abnamro.amrotv.feature.movies.domain.api.repository.MovieRepository
import nl.abnamro.amrotv.feature.movies.domain.api.usecase.GetGenresUseCase

internal class GetGenresUseCaseImpl
@Inject
constructor(private val movieRepository: MovieRepository) : GetGenresUseCase {

    override suspend fun invoke(): Outcome<List<Genre>> = movieRepository.getGenres()
}

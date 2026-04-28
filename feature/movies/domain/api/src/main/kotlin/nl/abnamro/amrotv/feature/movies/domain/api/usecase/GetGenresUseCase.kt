package nl.abnamro.amrotv.feature.movies.domain.api.usecase

import nl.abnamro.amrotv.core.domain.model.Outcome
import nl.abnamro.amrotv.feature.movies.domain.api.model.Genre

/**
 * Retrieves the list of all available movie genres.
 *
 * @see nl.abnamro.amrotv.feature.movies.domain.api.repository.MovieRepository.getGenres
 */
fun interface GetGenresUseCase {

    /**
     * Fetches the current list of all available movie genres.
     *
     * @return [Outcome.Success] with the genre list, or [Outcome.Error] on failure.
     */
    suspend operator fun invoke(): Outcome<List<Genre>>
}

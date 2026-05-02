package nl.abnamro.amrotv.feature.movies.domain.api.usecase

import nl.abnamro.amrotv.core.domain.model.Outcome
import nl.abnamro.amrotv.feature.movies.domain.api.model.MovieDetail

/** Retrieves the full detail of a single movie. */
fun interface GetMovieDetailUseCase {

    /**
     * Fetches detail data for the given movie.
     *
     * @param movieId unique movie identifier.
     * @return [Outcome.Success] with [MovieDetail], or [Outcome.Error] on failure.
     */
    suspend operator fun invoke(movieId: Int): Outcome<MovieDetail>
}

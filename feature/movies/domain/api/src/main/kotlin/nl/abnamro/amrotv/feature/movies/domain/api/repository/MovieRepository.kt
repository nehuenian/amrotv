package nl.abnamro.amrotv.feature.movies.domain.api.repository

import nl.abnamro.amrotv.core.domain.model.Outcome
import nl.abnamro.amrotv.feature.movies.domain.api.model.Genre
import nl.abnamro.amrotv.feature.movies.domain.api.model.Movie
import nl.abnamro.amrotv.feature.movies.domain.api.model.MovieDetail

/**
 * Source of truth for all movie data.
 *
 * Implementations are expected to apply a network-first strategy: fetch from the remote source,
 * persist to the local cache, and fall back to the cache on network failure. The caller is
 * responsible for triggering retries — there is no automatic recovery.
 */
interface MovieRepository {

    /**
     * Fetches the top 100 trending movies of the week from the remote source.
     *
     * @return [Outcome.Success] with the current list, or [Outcome.Error] on failure (may include
     *   stale cached data).
     */
    suspend fun getTrendingMovies(): Outcome<List<Movie>>

    /**
     * Fetches the full detail of the movie identified by [id].
     *
     * @param id unique movie identifier.
     * @return [Outcome.Success] with [MovieDetail], or [Outcome.Error] on failure (may include
     *   stale cached data).
     */
    suspend fun getMovieDetail(id: Int): Outcome<MovieDetail>

    /**
     * Fetches the current list of all available movie genres.
     *
     * @return [Outcome.Success] with the genre list, or [Outcome.Error] on failure (may include
     *   stale cached data).
     */
    suspend fun getGenres(): Outcome<List<Genre>>
}

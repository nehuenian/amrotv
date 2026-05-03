package nl.abnamro.amrotv.feature.movies.data.datasource.local

import nl.abnamro.amrotv.feature.movies.domain.api.model.Genre
import nl.abnamro.amrotv.feature.movies.domain.api.model.Movie

/**
 * Contract for persisting and retrieving movie data from a local store.
 *
 * Implementations handle all entity mapping internally. The interface exposes only domain models —
 * no persistence details cross this boundary.
 */
internal interface LocalMovieDataSource {

    /**
     * Persists [movies], overwriting any previously cached entries.
     *
     * @param movies domain models to store.
     */
    suspend fun saveMovies(movies: List<Movie>)

    /** Returns all locally cached movies, or an empty list if no cache exists. */
    suspend fun getCachedMovies(): List<Movie>

    /**
     * Persists [genres], overwriting any previously cached entries.
     *
     * @param genres domain models to store.
     */
    suspend fun saveGenres(genres: List<Genre>)

    /** Returns all locally cached genres, or an empty list if no cache exists. */
    suspend fun getCachedGenres(): List<Genre>
}

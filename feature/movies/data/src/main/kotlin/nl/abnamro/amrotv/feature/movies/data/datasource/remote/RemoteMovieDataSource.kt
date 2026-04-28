package nl.abnamro.amrotv.feature.movies.data.datasource.remote

import nl.abnamro.amrotv.feature.movies.domain.api.model.Genre
import nl.abnamro.amrotv.feature.movies.domain.api.model.Movie
import nl.abnamro.amrotv.feature.movies.domain.api.model.MovieDetail

/**
 * Contract for fetching movie data from a remote source.
 *
 * Implementations are responsible for all network communication, DTO mapping,
 * and pagination. The interface exposes only domain models — no DTOs cross this boundary.
 * Implementations throw on failure; callers are responsible for catching and converting
 * to the appropriate domain [nl.abnamro.amrotv.core.domain.model.Outcome].
 */
internal interface RemoteMovieDataSource {

    /**
     * Fetches up to 100 trending movies for the current week.
     *
     * @return flat list of trending movies.
     * @throws Exception on network or server failure.
     */
    suspend fun getTrendingMovies(): List<Movie>

    /**
     * Fetches the full detail of the movie identified by [id].
     *
     * @param id unique movie identifier.
     * @return the [MovieDetail] for the given id.
     * @throws Exception on network or server failure.
     */
    suspend fun getMovieDetail(id: Int): MovieDetail

    /**
     * Fetches the complete list of available movie genres.
     *
     * @return list of all available genres.
     * @throws Exception on network or server failure.
     */
    suspend fun getGenres(): List<Genre>
}

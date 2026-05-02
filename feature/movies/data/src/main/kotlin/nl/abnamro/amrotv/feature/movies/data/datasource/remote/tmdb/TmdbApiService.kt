package nl.abnamro.amrotv.feature.movies.data.datasource.remote.tmdb

import nl.abnamro.amrotv.feature.movies.data.datasource.remote.tmdb.dto.GenreListResponseDto
import nl.abnamro.amrotv.feature.movies.data.datasource.remote.tmdb.dto.MovieDetailDto
import nl.abnamro.amrotv.feature.movies.data.datasource.remote.tmdb.dto.TrendingMoviesResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit service interface for the TMDB (The Movie Database) REST API v3.
 *
 * All endpoints are suspend functions and return deserialized DTO objects. Base URL is configured
 * at the Hilt provider level.
 */
internal interface TmdbApiService {

    /**
     * Fetches a paginated list of trending movies for the given time window.
     *
     * @param timeWindow trending period — either `"day"` or `"week"`.
     * @param page the page number to fetch (1-based).
     * @param language ISO 639-1 language code, e.g. `"en-US"`.
     * @return [TrendingMoviesResponseDto] containing the results and pagination metadata.
     */
    @GET("trending/movie/{timeWindow}")
    suspend fun getTrendingMovies(
        @Path("timeWindow") timeWindow: String,
        @Query("page") page: Int,
        @Query("language") language: String,
    ): TrendingMoviesResponseDto

    /**
     * Fetches the full detail for a single movie by its TMDB ID.
     *
     * @param movieId the TMDB movie identifier.
     * @param language ISO 639-1 language code, e.g. `"en-US"`.
     * @return [MovieDetailDto] with the movie's full metadata.
     */
    @GET("movie/{movieId}")
    suspend fun getMovieDetail(
        @Path("movieId") movieId: Int,
        @Query("language") language: String,
    ): MovieDetailDto

    /**
     * Fetches the official list of movie genres defined by TMDB.
     *
     * @param language ISO 639-1 language code, e.g. `"en-US"`.
     * @return [GenreListResponseDto] containing all available genres.
     */
    @GET("genre/movie/list")
    suspend fun getGenres(@Query("language") language: String): GenreListResponseDto
}

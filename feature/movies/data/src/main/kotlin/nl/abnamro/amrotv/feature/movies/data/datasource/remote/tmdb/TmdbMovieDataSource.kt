package nl.abnamro.amrotv.feature.movies.data.datasource.remote.tmdb

import kotlinx.coroutines.CancellationException
import nl.abnamro.amrotv.feature.movies.data.datasource.remote.RemoteMovieDataSource
import nl.abnamro.amrotv.feature.movies.data.datasource.remote.tmdb.dto.MovieDto
import nl.abnamro.amrotv.feature.movies.data.datasource.remote.tmdb.dto.toDomain
import nl.abnamro.amrotv.feature.movies.domain.api.model.Genre
import nl.abnamro.amrotv.feature.movies.domain.api.model.Movie
import nl.abnamro.amrotv.feature.movies.domain.api.model.MovieDetail
import nl.abnamro.amrotv.libraries.logger.api.LogLevel
import nl.abnamro.amrotv.libraries.logger.api.Logger
import javax.inject.Inject

private const val TAG = "TmdbMovieDataSource"
private const val LANGUAGE = "en-US"
private const val TIME_WINDOW = "week"
private const val TARGET_MOVIE_COUNT = 100

internal class TmdbMovieDataSource @Inject constructor(
    private val apiService: TmdbApiService,
    private val logger: Logger,
) : RemoteMovieDataSource {

    // Pages are fetched sequentially by design. Page 1 failure throws to the caller.
    // Any subsequent page failure stops pagination early and returns what was accumulated so far
    // to avoid cascading requests on persistent errors (e.g. auth failure, rate limiting).
    override suspend fun getTrendingMovies(): List<Movie> {
        val accumulated = mutableListOf<MovieDto>()
        var page = 1
        var totalPages = Int.MAX_VALUE
        do {
            try {
                val response = apiService.getTrendingMovies(TIME_WINDOW, page, LANGUAGE)
                totalPages = response.totalPages
                accumulated += response.results
                if (response.results.isEmpty()) break
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                if (page == 1) throw e
                logger.log(LogLevel.WARN, TAG, "Failed to fetch page $page, stopping pagination with ${accumulated.size} movies so far", throwable = e)
                break
            }
            page++
        } while (accumulated.size < TARGET_MOVIE_COUNT && page <= totalPages)
        return accumulated.take(TARGET_MOVIE_COUNT).map { it.toDomain() }
    }

    override suspend fun getMovieDetail(id: Int): MovieDetail =
        apiService.getMovieDetail(movieId = id, language = LANGUAGE).toDomain()

    override suspend fun getGenres(): List<Genre> =
        apiService.getGenres(language = LANGUAGE).genres.map { it.toDomain() }
}

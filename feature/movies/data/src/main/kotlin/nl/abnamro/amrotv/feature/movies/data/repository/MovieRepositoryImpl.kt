package nl.abnamro.amrotv.feature.movies.data.repository

import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import nl.abnamro.amrotv.core.domain.model.Outcome
import nl.abnamro.amrotv.feature.movies.data.datasource.local.LocalMovieDataSource
import nl.abnamro.amrotv.feature.movies.data.datasource.remote.RemoteMovieDataSource
import nl.abnamro.amrotv.feature.movies.domain.api.model.Genre
import nl.abnamro.amrotv.feature.movies.domain.api.model.Movie
import nl.abnamro.amrotv.feature.movies.domain.api.model.MovieDetail
import nl.abnamro.amrotv.feature.movies.domain.api.repository.MovieRepository
import nl.abnamro.amrotv.libraries.logger.api.LogLevel
import nl.abnamro.amrotv.libraries.logger.api.Logger

private const val TAG = "MovieRepositoryImpl"

internal class MovieRepositoryImpl
@Inject
constructor(
    private val remote: RemoteMovieDataSource,
    private val local: LocalMovieDataSource,
    private val logger: Logger,
) : MovieRepository {

    override suspend fun getTrendingMovies(): Outcome<List<Movie>> =
        try {
            val movies = remote.getTrendingMovies()
            // Cache is not working yet, it will be integrated in a later stage.
            local.saveMovies(movies)
            Outcome.Success(movies)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.log(LogLevel.ERROR, TAG, "Failed to fetch trending movies", e)
            // Cache fallback is not fully integrated yet; return cached movies only when the cache
            // is
            // not empty.
            val cached = local.getCachedMovies()
            Outcome.Error(e, cached.takeIf { it.isNotEmpty() })
        }

    override suspend fun getMovieDetail(id: Int): Outcome<MovieDetail> =
        try {
            Outcome.Success(remote.getMovieDetail(id))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.log(LogLevel.ERROR, TAG, "Failed to fetch movie detail for id=$id", e)
            Outcome.Error(e)
        }

    override suspend fun getGenres(): Outcome<List<Genre>> =
        try {
            val genres = remote.getGenres()
            local.saveGenres(genres)
            Outcome.Success(genres)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.log(LogLevel.ERROR, TAG, "Failed to fetch genres, checking cache", e)
            val cached = local.getCachedGenres()
            Outcome.Error(e, cached.takeIf { it.isNotEmpty() })
        }
}

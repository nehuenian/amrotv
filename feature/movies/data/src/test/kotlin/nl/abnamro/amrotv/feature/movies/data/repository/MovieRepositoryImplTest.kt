package nl.abnamro.amrotv.feature.movies.data.repository

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import nl.abnamro.amrotv.feature.movies.data.MovieDataMocks
import nl.abnamro.amrotv.feature.movies.data.datasource.local.LocalMovieDataSource
import nl.abnamro.amrotv.feature.movies.data.datasource.remote.RemoteMovieDataSource
import nl.abnamro.amrotv.feature.movies.data.util.requireError
import nl.abnamro.amrotv.feature.movies.data.util.requireSuccess
import nl.abnamro.amrotv.feature.movies.domain.api.repository.MovieRepository
import nl.abnamro.amrotv.libraries.logger.api.LogLevel
import nl.abnamro.amrotv.libraries.logger.api.Logger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("MovieRepositoryImpl")
internal class MovieRepositoryImplTest {

    @MockK lateinit var remote: RemoteMovieDataSource
    @MockK lateinit var local: LocalMovieDataSource
    @MockK lateinit var logger: Logger

    private lateinit var repository: MovieRepository

    private val movies = MovieDataMocks.Domain.movies
    private val detail = MovieDataMocks.Domain.detail
    private val genres = MovieDataMocks.Domain.genres
    private val networkError = RuntimeException("network error")

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        repository = MovieRepositoryImpl(remote, local, logger)
    }

    // region getTrendingMovies

    @Nested
    @DisplayName("GIVEN remote returns trending movies successfully")
    inner class GivenRemoteReturnsTrendingMovies {

        @BeforeEach
        fun setUp() {
            coEvery { remote.getTrendingMovies() } returns movies
        }

        @Nested
        @DisplayName("WHEN getTrendingMovies() is called")
        inner class WhenGetTrendingMoviesIsCalled {

            @Test
            @DisplayName("THEN Outcome.Success is returned with the movies")
            fun returnsSuccess() = runTest {
                assertEquals(movies, repository.getTrendingMovies().requireSuccess())
            }

            @Test
            @DisplayName("THEN movies are saved to the local cache")
            fun savesToLocalCache() = runTest {
                repository.getTrendingMovies()
                coVerify(exactly = 1) { local.saveMovies(movies) }
            }
        }
    }

    @Nested
    @DisplayName("GIVEN remote throws and local cache has movies")
    inner class GivenRemoteThrowsWithCachedMovies {

        @BeforeEach
        fun setUp() {
            coEvery { remote.getTrendingMovies() } throws networkError
            coEvery { local.getCachedMovies() } returns movies
        }

        @Nested
        @DisplayName("WHEN getTrendingMovies() is called")
        inner class WhenGetTrendingMoviesIsCalled {

            @Test
            @DisplayName("THEN the error cause is propagated")
            fun errorCausePropagated() = runTest {
                assertEquals(networkError, repository.getTrendingMovies().requireError().cause)
            }

            @Test
            @DisplayName("THEN stale cached movies are included in the error")
            fun staleDataIncluded() = runTest {
                assertEquals(movies, repository.getTrendingMovies().requireError().data)
            }

            @Test
            @DisplayName("THEN the error is logged at ERROR level")
            fun errorIsLogged() = runTest {
                repository.getTrendingMovies()
                verify(exactly = 1) { logger.log(LogLevel.ERROR, "MovieRepositoryImpl", "Failed to fetch trending movies", networkError) }
            }
        }
    }

    @Nested
    @DisplayName("GIVEN remote throws and local cache is empty")
    inner class GivenRemoteThrowsWithEmptyCache {

        @BeforeEach
        fun setUp() {
            coEvery { remote.getTrendingMovies() } throws networkError
            coEvery { local.getCachedMovies() } returns emptyList()
        }

        @Nested
        @DisplayName("WHEN getTrendingMovies() is called")
        inner class WhenGetTrendingMoviesIsCalled {

            @Test
            @DisplayName("THEN the error cause is propagated")
            fun errorCausePropagated() = runTest {
                assertEquals(networkError, repository.getTrendingMovies().requireError().cause)
            }

            @Test
            @DisplayName("THEN data is null")
            fun dataIsNull() = runTest {
                assertNull(repository.getTrendingMovies().requireError().data)
            }

            @Test
            @DisplayName("THEN the error is logged at ERROR level")
            fun errorIsLogged() = runTest {
                repository.getTrendingMovies()
                verify(exactly = 1) { logger.log(LogLevel.ERROR, "MovieRepositoryImpl", "Failed to fetch trending movies", networkError) }
            }
        }
    }

    // endregion

    // region getMovieDetail

    @Nested
    @DisplayName("GIVEN remote returns movie detail successfully")
    inner class GivenRemoteReturnsMovieDetail {

        @BeforeEach
        fun setUp() {
            coEvery { remote.getMovieDetail(detail.id) } returns detail
        }

        @Nested
        @DisplayName("WHEN getMovieDetail() is called")
        inner class WhenGetMovieDetailIsCalled {

            @Test
            @DisplayName("THEN Outcome.Success is returned with the detail")
            fun returnsSuccess() = runTest {
                assertEquals(detail, repository.getMovieDetail(detail.id).requireSuccess())
            }
        }
    }

    @Nested
    @DisplayName("GIVEN remote throws for movie detail")
    inner class GivenRemoteThrowsForMovieDetail {

        @BeforeEach
        fun setUp() {
            coEvery { remote.getMovieDetail(detail.id) } throws networkError
        }

        @Nested
        @DisplayName("WHEN getMovieDetail() is called")
        inner class WhenGetMovieDetailIsCalled {

            @Test
            @DisplayName("THEN the error cause is propagated")
            fun errorCausePropagated() = runTest {
                assertEquals(networkError, repository.getMovieDetail(detail.id).requireError().cause)
            }

            @Test
            @DisplayName("THEN data is null (no local cache for detail)")
            fun dataIsNull() = runTest {
                assertNull(repository.getMovieDetail(detail.id).requireError().data)
            }

            @Test
            @DisplayName("THEN the error is logged at ERROR level")
            fun errorIsLogged() = runTest {
                repository.getMovieDetail(detail.id)
                verify(exactly = 1) { logger.log(LogLevel.ERROR, "MovieRepositoryImpl", "Failed to fetch movie detail for id=${detail.id}", networkError) }
            }
        }
    }

    // endregion

    // region getGenres

    @Nested
    @DisplayName("GIVEN remote returns genres successfully")
    inner class GivenRemoteReturnsGenres {

        @BeforeEach
        fun setUp() {
            coEvery { remote.getGenres() } returns genres
        }

        @Nested
        @DisplayName("WHEN getGenres() is called")
        inner class WhenGetGenresIsCalled {

            @Test
            @DisplayName("THEN Outcome.Success is returned with the genres")
            fun returnsSuccess() = runTest {
                assertEquals(genres, repository.getGenres().requireSuccess())
            }

            @Test
            @DisplayName("THEN genres are saved to the local cache")
            fun savesToLocalCache() = runTest {
                repository.getGenres()
                coVerify(exactly = 1) { local.saveGenres(genres) }
            }
        }
    }

    @Nested
    @DisplayName("GIVEN remote throws and local genre cache has genres")
    inner class GivenRemoteThrowsWithCachedGenres {

        @BeforeEach
        fun setUp() {
            coEvery { remote.getGenres() } throws networkError
            coEvery { local.getCachedGenres() } returns genres
        }

        @Nested
        @DisplayName("WHEN getGenres() is called")
        inner class WhenGetGenresIsCalled {

            @Test
            @DisplayName("THEN the error cause is propagated")
            fun errorCausePropagated() = runTest {
                assertEquals(networkError, repository.getGenres().requireError().cause)
            }

            @Test
            @DisplayName("THEN stale cached genres are included in the error")
            fun staleDataIncluded() = runTest {
                assertEquals(genres, repository.getGenres().requireError().data)
            }

            @Test
            @DisplayName("THEN the error is logged at ERROR level")
            fun errorIsLogged() = runTest {
                repository.getGenres()
                verify(exactly = 1) { logger.log(LogLevel.ERROR, "MovieRepositoryImpl", "Failed to fetch genres, checking cache", networkError) }
            }
        }
    }

    @Nested
    @DisplayName("GIVEN remote throws and local genre cache is empty")
    inner class GivenRemoteThrowsWithEmptyGenreCache {

        @BeforeEach
        fun setUp() {
            coEvery { remote.getGenres() } throws networkError
            coEvery { local.getCachedGenres() } returns emptyList()
        }

        @Nested
        @DisplayName("WHEN getGenres() is called")
        inner class WhenGetGenresIsCalled {

            @Test
            @DisplayName("THEN the error cause is propagated")
            fun errorCausePropagated() = runTest {
                assertEquals(networkError, repository.getGenres().requireError().cause)
            }

            @Test
            @DisplayName("THEN data is null")
            fun dataIsNull() = runTest {
                assertNull(repository.getGenres().requireError().data)
            }

            @Test
            @DisplayName("THEN the error is logged at ERROR level")
            fun errorIsLogged() = runTest {
                repository.getGenres()
                verify(exactly = 1) { logger.log(LogLevel.ERROR, "MovieRepositoryImpl", "Failed to fetch genres, checking cache", networkError) }
            }
        }
    }

    // endregion
}


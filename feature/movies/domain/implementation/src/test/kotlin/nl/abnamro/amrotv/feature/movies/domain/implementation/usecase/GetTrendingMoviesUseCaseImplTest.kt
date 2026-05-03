package nl.abnamro.amrotv.feature.movies.domain.implementation.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import nl.abnamro.amrotv.core.domain.model.Outcome
import nl.abnamro.amrotv.feature.movies.domain.api.model.Movie
import nl.abnamro.amrotv.feature.movies.domain.api.repository.MovieRepository
import nl.abnamro.amrotv.feature.movies.domain.api.usecase.GetTrendingMoviesUseCase
import nl.abnamro.amrotv.feature.movies.domain.implementation.usecase.MovieDomainMocks.Movies
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GetTrendingMoviesUseCaseImplTest {

    @MockK lateinit var repository: MovieRepository

    private lateinit var useCase: GetTrendingMoviesUseCase

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = GetTrendingMoviesUseCaseImpl(repository)
    }

    @Nested
    @DisplayName("GIVEN the repository returns a successful movie list")
    inner class GivenRepositoryReturnsSuccessfulMovieList {

        @BeforeEach
        fun setUp() {
            coEvery { repository.getTrendingMovies() } returns Outcome.Success(Movies.all)
        }

        @Nested
        @DisplayName("WHEN the use case is invoked")
        inner class WhenInvoked {

            @Test
            @DisplayName("THEN the full unfiltered movie list is returned")
            fun fullMovieListReturned() = runTest {
                val data = useCase().requireSuccess()
                assertEquals(Movies.all, data)
            }
        }
    }

    @Nested
    @DisplayName("GIVEN the repository returns an error without stale data")
    inner class GivenRepositoryReturnsErrorWithNoStaleData {

        private val networkError = RuntimeException("network error")

        @BeforeEach
        fun setUp() {
            coEvery { repository.getTrendingMovies() } returns Outcome.Error(networkError)
        }

        @Nested
        @DisplayName("WHEN the use case is invoked")
        inner class WhenInvoked {

            @Test
            @DisplayName("THEN the error cause is propagated")
            fun errorCausePropagated() = runTest {
                val error = useCase().requireError()
                assertEquals(networkError, error.cause)
            }

            @Test
            @DisplayName("THEN the data is null")
            fun dataIsNull() = runTest {
                val error = useCase().requireError()
                assertNull(error.data)
            }
        }
    }

    @Nested
    @DisplayName("GIVEN the repository returns an error with stale cached data")
    inner class GivenRepositoryReturnsErrorWithStaleData {

        private val networkError = RuntimeException("refresh failed")

        @BeforeEach
        fun setUp() {
            coEvery { repository.getTrendingMovies() } returns
                Outcome.Error(networkError, Movies.all)
        }

        @Nested
        @DisplayName("WHEN the use case is invoked")
        inner class WhenInvoked {

            @Test
            @DisplayName("THEN the error cause is preserved")
            fun errorCausePreserved() = runTest {
                val error = useCase().requireError()
                assertEquals(networkError, error.cause)
            }

            @Test
            @DisplayName("THEN the full stale movie list is returned unmodified")
            fun staleMovieListReturnedUnmodified() = runTest {
                val stale = requireNotNull(useCase().requireError().data)
                assertEquals(Movies.all, stale)
            }
        }
    }
}

private fun Outcome<List<Movie>>.requireSuccess(): List<Movie> =
    when (this) {
        is Outcome.Success -> data
        is Outcome.Error -> error("Expected Outcome.Success but got Outcome.Error: $cause")
    }

private fun Outcome<List<Movie>>.requireError(): Outcome.Error<List<Movie>> =
    when (this) {
        is Outcome.Error -> this
        is Outcome.Success -> error("Expected Outcome.Error but got Outcome.Success")
    }

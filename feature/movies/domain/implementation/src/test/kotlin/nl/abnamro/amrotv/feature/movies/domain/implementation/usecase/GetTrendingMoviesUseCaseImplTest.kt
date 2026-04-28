package nl.abnamro.amrotv.feature.movies.domain.implementation.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import nl.abnamro.amrotv.feature.movies.domain.api.model.Movie
import nl.abnamro.amrotv.core.domain.model.Outcome
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOption
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOrder
import nl.abnamro.amrotv.feature.movies.domain.api.repository.MovieRepository
import nl.abnamro.amrotv.feature.movies.domain.api.usecase.GetTrendingMoviesUseCase
import nl.abnamro.amrotv.feature.movies.domain.implementation.usecase.MovieDomainMocks.Movies
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GetTrendingMoviesUseCaseImplTest {

    @MockK
    lateinit var repository: MovieRepository

    private lateinit var useCase: GetTrendingMoviesUseCase

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = GetTrendingMoviesUseCaseImpl(repository)
    }

    @Nested
    @DisplayName("GIVEN a list of movies returned by the repository")
    inner class GivenRepositoryReturnsSuccessfulMovieList {

        @BeforeEach
        fun setUp() {
            coEvery { repository.getTrendingMovies() } returns Outcome.Success(Movies.all)
        }

        @Nested
        @DisplayName("WHEN invoked with no genre filter and popularity sort DESC")
        inner class WhenInvokedWithNoFilterAndPopularitySortDesc {

            @Test
            @DisplayName("THEN movies are sorted most to least popular")
            fun moviesSortedMostToLeastPopular() = runTest {
                val data = useCase(null, SortOption.POPULARITY, SortOrder.DESC).requireSuccess()
                assertEquals(listOf(Movies.actionComedy, Movies.action, Movies.comedy), data)
            }
        }

        @Nested
        @DisplayName("WHEN invoked with no genre filter and popularity sort ASC")
        inner class WhenInvokedWithNoFilterAndPopularitySortAsc {

            @Test
            @DisplayName("THEN movies are sorted least to most popular")
            fun moviesSortedLeastToMostPopular() = runTest {
                val data = useCase(null, SortOption.POPULARITY, SortOrder.ASC).requireSuccess()
                assertEquals(listOf(Movies.comedy, Movies.action, Movies.actionComedy), data)
            }
        }

        @Nested
        @DisplayName("WHEN invoked with action genre filter and popularity sort ASC")
        inner class WhenInvokedWithActionGenreFilterAndPopularitySortAsc {

            @Test
            @DisplayName("THEN only action movies are returned in popularity order")
            fun onlyActionMoviesReturnedInPopularityOrder() = runTest {
                val data = useCase(Movies.ACTION_GENRE_ID, SortOption.POPULARITY, SortOrder.ASC).requireSuccess()
                assertEquals(listOf(Movies.action, Movies.actionComedy), data)
            }
        }

        @Nested
        @DisplayName("WHEN invoked with an unknown genre filter")
        inner class WhenInvokedWithUnknownGenreFilter {

            @Test
            @DisplayName("THEN an empty list is returned")
            fun emptyListReturned() = runTest {
                val data = useCase(Movies.UNKNOWN_GENRE_ID, SortOption.POPULARITY, SortOrder.DESC).requireSuccess()
                assertTrue(data.isEmpty())
            }
        }

        @Nested
        @DisplayName("WHEN invoked with no genre filter and title sort ASC")
        inner class WhenInvokedWithNoFilterAndTitleSortAsc {

            @Test
            @DisplayName("THEN movies are sorted alphabetically")
            fun moviesSortedAlphabetically() = runTest {
                val data = useCase(null, SortOption.TITLE, SortOrder.ASC).requireSuccess()
                assertEquals(listOf(Movies.action, Movies.comedy, Movies.actionComedy), data)
            }
        }

        @Nested
        @DisplayName("WHEN invoked with no genre filter and title sort DESC")
        inner class WhenInvokedWithNoFilterAndTitleSortDesc {

            @Test
            @DisplayName("THEN movies are sorted in reverse alphabetical order")
            fun moviesSortedReverseAlphabetically() = runTest {
                val data = useCase(null, SortOption.TITLE, SortOrder.DESC).requireSuccess()
                assertEquals(listOf(Movies.actionComedy, Movies.comedy, Movies.action), data)
            }
        }

        @Nested
        @DisplayName("WHEN invoked with no genre filter and release date sort DESC")
        inner class WhenInvokedWithNoFilterAndReleaseDateSortDesc {

            @Test
            @DisplayName("THEN movies are sorted from newest to oldest")
            fun moviesSortedFromNewestToOldest() = runTest {
                val data = useCase(null, SortOption.RELEASE_DATE, SortOrder.DESC).requireSuccess()
                assertEquals(listOf(Movies.action, Movies.comedy, Movies.actionComedy), data)
            }
        }
    }

    @Nested
    @DisplayName("GIVEN repository returns an error without stale data")
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
                val error = useCase(null, SortOption.POPULARITY, SortOrder.DESC).requireError()
                assertEquals(networkError, error.cause)
            }

            @Test
            @DisplayName("THEN the data is null")
            fun dataIsNull() = runTest {
                val error = useCase(null, SortOption.POPULARITY, SortOrder.DESC).requireError()
                assertNull(error.data)
            }
        }
    }

    @Nested
    @DisplayName("GIVEN repository returns an error with stale cached data")
    inner class GivenRepositoryReturnsErrorWithStaleData {

        private val networkError = RuntimeException("refresh failed")

        @BeforeEach
        fun setUp() {
            coEvery { repository.getTrendingMovies() } returns Outcome.Error(networkError, Movies.all)
        }

        @Nested
        @DisplayName("WHEN invoked with action genre filter and popularity sort ASC")
        inner class WhenInvokedWithActionGenreFilterAndPopularitySortAsc {

            @Test
            @DisplayName("THEN the error cause is preserved")
            fun errorCausePreserved() = runTest {
                val error = useCase(Movies.ACTION_GENRE_ID, SortOption.POPULARITY, SortOrder.ASC).requireError()
                assertEquals(networkError, error.cause)
            }

            @Test
            @DisplayName("THEN the stale data is filtered and sorted correctly")
            fun staleDataFilteredAndSortedCorrectly() = runTest {
                val stale = requireNotNull(
                    useCase(Movies.ACTION_GENRE_ID, SortOption.POPULARITY, SortOrder.ASC).requireError().data
                )
                assertEquals(listOf(Movies.action, Movies.actionComedy), stale)
            }
        }
    }
}

private fun Outcome<List<Movie>>.requireSuccess(): List<Movie> = when (this) {
    is Outcome.Success -> data
    is Outcome.Error -> error("Expected Outcome.Success but got Outcome.Error: $cause")
}

private fun Outcome<List<Movie>>.requireError(): Outcome.Error<List<Movie>> = when (this) {
    is Outcome.Error -> this
    is Outcome.Success -> error("Expected Outcome.Error but got Outcome.Success")
}

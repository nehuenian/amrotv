package nl.abnamro.amrotv.feature.movies.data.datasource.remote.tmdb

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import nl.abnamro.amrotv.feature.movies.data.MovieDataMocks.Dtos
import nl.abnamro.amrotv.feature.movies.data.datasource.remote.RemoteMovieDataSource
import nl.abnamro.amrotv.feature.movies.data.datasource.remote.tmdb.dto.TrendingMoviesResponseDto
import nl.abnamro.amrotv.libraries.logger.api.LogLevel
import nl.abnamro.amrotv.libraries.logger.api.Logger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("TmdbMovieDataSource")
internal class TmdbMovieDataSourceTest {

  @MockK lateinit var apiService: TmdbApiService
  @MockK lateinit var logger: Logger

  private lateinit var dataSource: RemoteMovieDataSource

  @BeforeEach
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)
    dataSource = TmdbMovieDataSource(apiService, logger)
  }

  @Nested
  @DisplayName("GIVEN page 1 throws a network exception")
  inner class GivenPage1Throws {

    private val networkError = RuntimeException("network error")

    @BeforeEach
    fun setUp() {
      coEvery { apiService.getTrendingMovies(any(), 1, any()) } throws networkError
    }

    @Nested
    @DisplayName("WHEN getTrendingMovies() is called")
    inner class WhenGetTrendingMoviesIsCalled {

      @Test
      @DisplayName("THEN the exception propagates to the caller")
      fun exceptionPropagates() = runTest {
        val thrown = runCatching { dataSource.getTrendingMovies() }.exceptionOrNull()
        assertEquals(networkError, thrown)
      }
    }
  }

  @Nested
  @DisplayName("GIVEN a single page response with fewer than 100 movies")
  inner class GivenSinglePageFewerThan100 {

    @BeforeEach
    fun setUp() {
      coEvery { apiService.getTrendingMovies(any(), 1, any()) } returns
          TrendingMoviesResponseDto(results = listOf(Dtos.movieDto), page = 1, totalPages = 1)
    }

    @Nested
    @DisplayName("WHEN getTrendingMovies() is called")
    inner class WhenGetTrendingMoviesIsCalled {

      @Test
      @DisplayName("THEN all available movies are returned")
      fun returnsAllAvailableMovies() = runTest {
        val result = dataSource.getTrendingMovies()
        assertEquals(1, result.size)
        assertEquals(Dtos.movieDto.id, result.first().id)
      }
    }
  }

  @Nested
  @DisplayName("GIVEN page 1 succeeds but page 2 throws (totalPages = 2)")
  inner class GivenPage1SucceedsPage2Throws {

    @BeforeEach
    fun setUp() {
      coEvery { apiService.getTrendingMovies(any(), 1, any()) } returns
          TrendingMoviesResponseDto(results = listOf(Dtos.movieDto), page = 1, totalPages = 2)
      coEvery { apiService.getTrendingMovies(any(), 2, any()) } throws
          RuntimeException("page 2 error")
    }

    @Nested
    @DisplayName("WHEN getTrendingMovies() is called")
    inner class WhenGetTrendingMoviesIsCalled {

      @Test
      @DisplayName("THEN movies from page 1 are returned")
      fun returnsPage1Movies() = runTest {
        val result = dataSource.getTrendingMovies()
        assertEquals(1, result.size)
        assertEquals(Dtos.movieDto.id, result.first().id)
      }

      @Test
      @DisplayName("THEN the page failure is logged at WARN level")
      fun pageFailureIsLogged() = runTest {
        dataSource.getTrendingMovies()
        verify(exactly = 1) {
          logger.log(
              LogLevel.WARN,
              "TmdbMovieDataSource",
              "Failed to fetch page 2, stopping pagination with 1 movies so far",
              any(),
          )
        }
      }
    }
  }

  @Nested
  @DisplayName("GIVEN page 1 succeeds but page 2 of 3 throws")
  inner class GivenPage2Of3Throws {

    @BeforeEach
    fun setUp() {
      coEvery { apiService.getTrendingMovies(any(), 1, any()) } returns
          TrendingMoviesResponseDto(
              results = listOf(Dtos.movieDto.copy(id = 1)),
              page = 1,
              totalPages = 3,
          )
      coEvery { apiService.getTrendingMovies(any(), 2, any()) } throws
          RuntimeException("page 2 error")
    }

    @Nested
    @DisplayName("WHEN getTrendingMovies() is called")
    inner class WhenGetTrendingMoviesIsCalled {

      @Test
      @DisplayName("THEN only movies from page 1 are returned and fetching stops")
      fun stopsAfterFirstFailure() = runTest {
        val result = dataSource.getTrendingMovies()
        assertEquals(1, result.size)
        assertEquals(1, result[0].id)
      }

      @Test
      @DisplayName("THEN page 3 is never requested")
      fun page3IsNeverRequested() = runTest {
        dataSource.getTrendingMovies()
        coVerify(exactly = 0) { apiService.getTrendingMovies(any(), 3, any()) }
      }

      @Test
      @DisplayName("THEN the page failure is logged at WARN level")
      fun pageFailureIsLogged() = runTest {
        dataSource.getTrendingMovies()
        verify(exactly = 1) {
          logger.log(
              LogLevel.WARN,
              "TmdbMovieDataSource",
              "Failed to fetch page 2, stopping pagination with 1 movies so far",
              any(),
          )
        }
      }
    }
  }

  @Nested
  @DisplayName("GIVEN the API has more than 100 movies across pages")
  inner class GivenMoreThan100MoviesAvailable {

    private val moviesPage1 = (1..60).map { Dtos.movieDto.copy(id = it) }
    private val moviesPage2 = (61..120).map { Dtos.movieDto.copy(id = it) }

    @BeforeEach
    fun setUp() {
      coEvery { apiService.getTrendingMovies(any(), 1, any()) } returns
          TrendingMoviesResponseDto(results = moviesPage1, page = 1, totalPages = 2)
      coEvery { apiService.getTrendingMovies(any(), 2, any()) } returns
          TrendingMoviesResponseDto(results = moviesPage2, page = 2, totalPages = 2)
    }

    @Nested
    @DisplayName("WHEN getTrendingMovies() is called")
    inner class WhenGetTrendingMoviesIsCalled {

      @Test
      @DisplayName("THEN exactly 100 movies are returned")
      fun returns100Movies() = runTest { assertEquals(100, dataSource.getTrendingMovies().size) }
    }
  }

  @Nested
  @DisplayName("GIVEN pages contain overlapping duplicate movie IDs")
  inner class GivenPagesContainDuplicateIds {

    // Page 1: 60 unique movies (IDs 1–60)
    // Page 2: 30 duplicates (IDs 1–30) + 30 new (IDs 61–90) → only 30 contribute
    //   → 90 unique after page 2, still < 100, fetch page 3
    // Page 3: 30 movies (IDs 91–120) → first 10 fill the target of 100
    private val page1 = (1..60).map { Dtos.movieDto.copy(id = it) }
    private val page2 =
        (1..30).map { Dtos.movieDto.copy(id = it) } + (61..90).map { Dtos.movieDto.copy(id = it) }
    private val page3 = (91..120).map { Dtos.movieDto.copy(id = it) }

    @BeforeEach
    fun setUp() {
      coEvery { apiService.getTrendingMovies(any(), 1, any()) } returns
          TrendingMoviesResponseDto(results = page1, page = 1, totalPages = 3)
      coEvery { apiService.getTrendingMovies(any(), 2, any()) } returns
          TrendingMoviesResponseDto(results = page2, page = 2, totalPages = 3)
      coEvery { apiService.getTrendingMovies(any(), 3, any()) } returns
          TrendingMoviesResponseDto(results = page3, page = 3, totalPages = 3)
    }

    @Nested
    @DisplayName("WHEN getTrendingMovies() is called")
    inner class WhenGetTrendingMoviesIsCalled {

      @Test
      @DisplayName("THEN exactly 100 unique movies are returned")
      fun returns100UniqueMovies() = runTest {
        assertEquals(100, dataSource.getTrendingMovies().size)
      }

      @Test
      @DisplayName("THEN all returned movie IDs are unique")
      fun allReturnedIdsAreUnique() = runTest {
        val result = dataSource.getTrendingMovies()
        assertEquals(result.size, result.map { it.id }.distinct().size)
      }

      @Test
      @DisplayName("THEN page 3 is fetched to compensate for duplicates on page 2")
      fun page3IsFetchedToCompensateForDuplicates() = runTest {
        dataSource.getTrendingMovies()
        coVerify(exactly = 1) { apiService.getTrendingMovies(any(), 3, any()) }
      }
    }
  }

  @Nested
  @DisplayName("GIVEN the API returns a movie detail")
  inner class GivenApiReturnsMovieDetail {

    @BeforeEach
    fun setUp() {
      coEvery { apiService.getMovieDetail(Dtos.movieDetailDto.id, any()) } returns
          Dtos.movieDetailDto
    }

    @Nested
    @DisplayName("WHEN getMovieDetail() is called")
    inner class WhenGetMovieDetailIsCalled {

      @Test
      @DisplayName("THEN the detail is mapped to a domain MovieDetail")
      fun returnsMappedDetail() = runTest {
        val result = dataSource.getMovieDetail(Dtos.movieDetailDto.id)
        assertEquals(Dtos.movieDetailDto.id, result.id)
        assertEquals(Dtos.movieDetailDto.title, result.title)
      }
    }
  }

  @Nested
  @DisplayName("GIVEN the API returns the genre list")
  inner class GivenApiReturnsGenres {

    @BeforeEach
    fun setUp() {
      coEvery { apiService.getGenres(any()) } returns Dtos.genreListResponseDto
    }

    @Nested
    @DisplayName("WHEN getGenres() is called")
    inner class WhenGetGenresIsCalled {

      @Test
      @DisplayName("THEN the genres are mapped to domain Genre objects")
      fun returnsMappedGenres() = runTest {
        val result = dataSource.getGenres()
        assertEquals(1, result.size)
        assertEquals(Dtos.genreDto.id, result.first().id)
        assertEquals(Dtos.genreDto.name, result.first().name)
      }
    }
  }
}

package nl.abnamro.amrotv.feature.movies.presentation.implementation.trendingmovies

import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import nl.abnamro.amrotv.core.domain.model.Outcome
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOption
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOrder
import nl.abnamro.amrotv.feature.movies.domain.api.usecase.FilterAndSortMoviesUseCase
import nl.abnamro.amrotv.feature.movies.domain.api.usecase.GetGenresUseCase
import nl.abnamro.amrotv.feature.movies.domain.api.usecase.GetTrendingMoviesUseCase
import nl.abnamro.amrotv.feature.movies.presentation.api.trendingmovies.TrendingMoviesEffect
import nl.abnamro.amrotv.feature.movies.presentation.api.trendingmovies.TrendingMoviesIntent
import nl.abnamro.amrotv.feature.movies.presentation.implementation.PresentationMocks
import nl.abnamro.amrotv.feature.movies.presentation.implementation.util.MainDispatcherExtension
import nl.abnamro.amrotv.libraries.logger.api.Logger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class TrendingMoviesViewModelTest {

    @JvmField
    @RegisterExtension
    val mainExtension = MainDispatcherExtension()

    @MockK
    private lateinit var getTrendingMoviesUseCase: GetTrendingMoviesUseCase

    @MockK
    private lateinit var getGenresUseCase: GetGenresUseCase

    @MockK
    private lateinit var logger: Logger

    private lateinit var filterAndSortMoviesUseCase: FilterAndSortMoviesUseCase

    private lateinit var viewModel: TrendingMoviesViewModel

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        filterAndSortMoviesUseCase = FilterAndSortMoviesUseCase { movies, genreId, _, _ ->
            if (genreId != null) movies.filter { genreId in it.genreIds } else movies
        }
    }

    @Nested
    @DisplayName("GIVEN use cases return success")
    inner class GivenUseCasesReturnSuccess {

        @BeforeEach
        fun setUp() {
            coEvery {
                getTrendingMoviesUseCase()
            } returns Outcome.Success(PresentationMocks.Movies.all)
            coEvery { getGenresUseCase() } returns Outcome.Success(PresentationMocks.Genres.all)
            viewModel = TrendingMoviesViewModel(
                getTrendingMoviesUseCase,
                getGenresUseCase,
                TrendingMoviesStateReducers(),
                filterAndSortMoviesUseCase,
                logger,
            )
        }

        @Nested
        @DisplayName("WHEN the ViewModel is created")
        inner class WhenViewModelIsCreated {

            @Test
            @DisplayName("THEN state transitions through loading to loaded with movies and genres")
            fun stateTransitionsToLoaded() = runTest(mainExtension.testDispatcher) {
                viewModel.state.test {
                    val loading = awaitItem()
                    assertTrue(loading.isLoading)
                    advanceUntilIdle()
                    val loaded = awaitItem()
                    assertFalse(loaded.isLoading)
                    assertTrue(loaded.errors.isEmpty())
                    assertEquals(PresentationMocks.Movies.all, loaded.movies)
                    assertEquals(PresentationMocks.Genres.all, loaded.genres)
                }
            }
        }

        @Nested
        @DisplayName("WHEN FilterByGenre intent is sent")
        inner class WhenFilterByGenreIsSent {

            @Test
            @DisplayName("THEN movies are filtered in memory without a new network call")
            fun filtersMoviesInMemory() = runTest(mainExtension.testDispatcher) {
                advanceUntilIdle()
                viewModel.handleIntent(TrendingMoviesIntent.FilterByGenre(PresentationMocks.Movies.ACTION_GENRE_ID))
                val state = viewModel.state.value
                assertEquals(PresentationMocks.Movies.ACTION_GENRE_ID, state.selectedGenreId)
                assertEquals(listOf(PresentationMocks.Movies.action), state.movies)
                coVerify(exactly = 1) { getTrendingMoviesUseCase() }
            }
        }

        @Nested
        @DisplayName("WHEN ChangeSortOption intent is sent")
        inner class WhenChangeSortOptionIsSent {

            @Test
            @DisplayName("THEN sort option is updated in memory without a new network call")
            fun updatesSortOptionInMemory() = runTest(mainExtension.testDispatcher) {
                advanceUntilIdle()
                viewModel.handleIntent(TrendingMoviesIntent.ChangeSortOption(SortOption.TITLE))
                val state = viewModel.state.value
                assertEquals(SortOption.TITLE, state.selectedSortOption)
                coVerify(exactly = 1) { getTrendingMoviesUseCase() }
            }
        }

        @Nested
        @DisplayName("WHEN ToggleSortOrder intent is sent")
        inner class WhenToggleSortOrderIsSent {

            @Test
            @DisplayName("THEN sort order is toggled in memory without a new network call")
            fun togglesSortOrderInMemory() = runTest(mainExtension.testDispatcher) {
                advanceUntilIdle()
                viewModel.handleIntent(TrendingMoviesIntent.ToggleSortOrder)
                val state = viewModel.state.value
                assertEquals(SortOrder.ASC, state.selectedSortOrder)
                coVerify(exactly = 1) { getTrendingMoviesUseCase() }
            }
        }

        @Nested
        @DisplayName("WHEN OpenMovieDetail intent is sent")
        inner class WhenOpenMovieDetailIsSent {

            @Test
            @DisplayName("THEN NavigateToMovieDetail effect is emitted with the correct movieId")
            fun emitsNavigateToMovieDetailEffect() = runTest(mainExtension.testDispatcher) {
                advanceUntilIdle()
                viewModel.effects.test {
                    viewModel.handleIntent(TrendingMoviesIntent.OpenMovieDetail(movieId = PresentationMocks.Movies.action.id))
                    val effect = awaitItem()
                    assertTrue(effect is TrendingMoviesEffect.NavigateToMovieDetail)
                    assertEquals(PresentationMocks.Movies.action.id, (effect as TrendingMoviesEffect.NavigateToMovieDetail).movieId)
                }
            }
        }
    }

    @Nested
    @DisplayName("GIVEN the movies use case returns an error")
    inner class GivenMoviesUseCaseReturnsError {

        @BeforeEach
        fun setUp() {
            coEvery {
                getTrendingMoviesUseCase()
            } returns Outcome.Error(RuntimeException("network error"))
            coEvery { getGenresUseCase() } returns Outcome.Success(emptyList())
            viewModel = TrendingMoviesViewModel(
                getTrendingMoviesUseCase,
                getGenresUseCase,
                TrendingMoviesStateReducers(),
                filterAndSortMoviesUseCase,
                logger,
            )
        }

        @Nested
        @DisplayName("WHEN the ViewModel is created")
        inner class WhenViewModelIsCreated {

            @Test
            @DisplayName("THEN state has an error and isLoading is false")
            fun stateHasError() = runTest(mainExtension.testDispatcher) {
                viewModel.state.test {
                    val loading = awaitItem()
                    assertTrue(loading.isLoading)
                    advanceUntilIdle()
                    val state = awaitItem()
                    assertFalse(state.isLoading)
                    assertTrue(state.errors.isNotEmpty())
                }
            }
        }
    }

    @Nested
    @DisplayName("GIVEN movies succeed but genres use case returns an error")
    inner class GivenGenresUseCaseReturnsError {

        @BeforeEach
        fun setUp() {
            coEvery {
                getTrendingMoviesUseCase()
            } returns Outcome.Success(PresentationMocks.Movies.all)
            coEvery { getGenresUseCase() } returns Outcome.Error(RuntimeException("genres error"))
            viewModel = TrendingMoviesViewModel(
                getTrendingMoviesUseCase,
                getGenresUseCase,
                TrendingMoviesStateReducers(),
                filterAndSortMoviesUseCase,
                logger,
            )
        }

        @Nested
        @DisplayName("WHEN the ViewModel is created")
        inner class WhenViewModelIsCreated {

            @Test
            @DisplayName("THEN movies are shown alongside an error")
            fun moviesShownWithError() = runTest(mainExtension.testDispatcher) {
                viewModel.state.test {
                    val loading = awaitItem()
                    assertTrue(loading.isLoading)
                    advanceUntilIdle()
                    val state = awaitItem()
                    assertFalse(state.isLoading)
                    assertEquals(PresentationMocks.Movies.all, state.movies)
                    assertTrue(state.errors.isNotEmpty())
                }
            }
        }
    }

    @Nested
    @DisplayName("GIVEN movies use case returns an error with stale data")
    inner class GivenMoviesUseCaseReturnsErrorWithStaleData {

        @BeforeEach
        fun setUp() {
            coEvery {
                getTrendingMoviesUseCase()
            } returns Outcome.Error(RuntimeException("refresh failed"), PresentationMocks.Movies.all)
            coEvery { getGenresUseCase() } returns Outcome.Success(PresentationMocks.Genres.all)
            viewModel = TrendingMoviesViewModel(
                getTrendingMoviesUseCase,
                getGenresUseCase,
                TrendingMoviesStateReducers(),
                filterAndSortMoviesUseCase,
                logger,
            )
        }

        @Nested
        @DisplayName("WHEN the ViewModel is created")
        inner class WhenViewModelIsCreated {

            @Test
            @DisplayName("THEN stale movies are shown alongside an error")
            fun staleMoviesShownWithError() = runTest(mainExtension.testDispatcher) {
                viewModel.state.test {
                    val loading = awaitItem()
                    assertTrue(loading.isLoading)
                    advanceUntilIdle()
                    val state = awaitItem()
                    assertFalse(state.isLoading)
                    assertEquals(PresentationMocks.Movies.all, state.movies)
                    assertTrue(state.errors.isNotEmpty())
                }
            }
        }
    }
}

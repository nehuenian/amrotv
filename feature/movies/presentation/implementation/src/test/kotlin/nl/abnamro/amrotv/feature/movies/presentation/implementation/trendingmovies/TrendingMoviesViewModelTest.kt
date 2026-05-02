package nl.abnamro.amrotv.feature.movies.presentation.implementation.trendingmovies

import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import nl.abnamro.amrotv.core.domain.model.Outcome
import nl.abnamro.amrotv.core.mvi.AmroTvViewModel
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOption
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOrder
import nl.abnamro.amrotv.feature.movies.domain.api.usecase.FilterAndSortMoviesUseCase
import nl.abnamro.amrotv.feature.movies.domain.api.usecase.GetGenresUseCase
import nl.abnamro.amrotv.feature.movies.domain.api.usecase.GetTrendingMoviesUseCase
import nl.abnamro.amrotv.feature.movies.presentation.api.trendingmovies.TrendingMoviesEffect
import nl.abnamro.amrotv.feature.movies.presentation.api.trendingmovies.TrendingMoviesIntent
import nl.abnamro.amrotv.feature.movies.presentation.api.trendingmovies.TrendingMoviesState
import nl.abnamro.amrotv.feature.movies.presentation.implementation.PresentationMocks
import nl.abnamro.amrotv.feature.movies.presentation.implementation.mapper.GenreDomainToPresentationMapper
import nl.abnamro.amrotv.feature.movies.presentation.implementation.mapper.MovieDomainToPresentationMapper
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

    private lateinit var viewModel: AmroTvViewModel<TrendingMoviesState, TrendingMoviesIntent, TrendingMoviesEffect>

    private val fakeWeekRangeLabelProvider = object : WeekRangeLabelProvider {
        override fun currentWeekRangeLabel(nowMillis: Long): String = "Jan 1 – Jan 7"
    }

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
                TrendingMoviesStateReducers(MovieDomainToPresentationMapper(), GenreDomainToPresentationMapper(), fakeWeekRangeLabelProvider),
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
                    assertEquals(PresentationMocks.PresentationMovies.all.toPersistentList(), loaded.movies)
                    assertEquals(PresentationMocks.PresentationGenres.all.toPersistentList(), loaded.genres)
                }
            }
        }

        @Nested
        @DisplayName("WHEN the user filters movies by the action genre")
        inner class WhenUserFiltersByActionGenre {

            @Test
            @DisplayName("THEN movies are filtered to the selected genre")
            fun filtersMoviesInMemory() = runTest(mainExtension.testDispatcher) {
                advanceUntilIdle()
                viewModel.handleIntent(TrendingMoviesIntent.FilterByGenre(PresentationMocks.Movies.ACTION_GENRE_ID))
                val state = viewModel.state.value
                assertEquals(PresentationMocks.Movies.ACTION_GENRE_ID, state.selectedGenreId)
                assertEquals(listOf(PresentationMocks.PresentationMovies.action), state.movies)
            }
        }

        @Nested
        @DisplayName("WHEN the user switches the sort option to title")
        inner class WhenUserSwitchesSortOptionToTitle {

            @Test
            @DisplayName("THEN title becomes the active sort option")
            fun updatesSortOptionInMemory() = runTest(mainExtension.testDispatcher) {
                advanceUntilIdle()
                viewModel.handleIntent(TrendingMoviesIntent.ChangeSortOption(SortOption.TITLE))
                val state = viewModel.state.value
                assertEquals(SortOption.TITLE, state.selectedSortOption)
            }
        }

        @Nested
        @DisplayName("WHEN the user sets the sort order to ascending")
        inner class WhenUserSetsSortOrderToAscending {

            @Test
            @DisplayName("THEN ascending becomes the active sort order")
            fun selectsSortOrderInMemory() = runTest(mainExtension.testDispatcher) {
                advanceUntilIdle()
                viewModel.handleIntent(TrendingMoviesIntent.SelectSortOrder(SortOrder.ASC))
                val state = viewModel.state.value
                assertEquals(SortOrder.ASC, state.selectedSortOrder)
            }
        }

        @Nested
        @DisplayName("WHEN the user selects the sort order that is already active (DESC)")
        inner class WhenUserSelectsAlreadyActiveSortOrderDesc {

            @Test
            @DisplayName("THEN selected sort order remains DESC")
            fun selectingAlreadyActiveOrderIsNoOp() = runTest(mainExtension.testDispatcher) {
                advanceUntilIdle()
                viewModel.handleIntent(TrendingMoviesIntent.SelectSortOrder(SortOrder.DESC))
                val state = viewModel.state.value
                assertEquals(SortOrder.DESC, state.selectedSortOrder)
            }
        }

        @Nested
        @DisplayName("WHEN the user opens movie detail for a specific movie")
        inner class WhenUserOpensMovieDetailForASpecificMovie {

            @Test
            @DisplayName("THEN the NavigateToMovieDetail effect is emitted for the selected movie")
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

        @Nested
        @DisplayName("WHEN the user opens the sort options sheet")
        inner class WhenUserOpensTheSortOptionsSheet {

            @Test
            @DisplayName("THEN showSortSheet becomes true")
            fun showsSortSheet() = runTest(mainExtension.testDispatcher) {
                advanceUntilIdle()
                viewModel.handleIntent(TrendingMoviesIntent.SetSortSheetVisible(visible = true))
                assertTrue(viewModel.state.value.showSortSheet)
            }
        }
    }

    @Nested
    @DisplayName("GIVEN use cases return success and the sort sheet is open")
    inner class GivenUseCasesReturnSuccessAndSortSheetIsOpen {

        @BeforeEach
        fun setUp() {
            coEvery { getTrendingMoviesUseCase() } returns Outcome.Success(PresentationMocks.Movies.all)
            coEvery { getGenresUseCase() } returns Outcome.Success(PresentationMocks.Genres.all)
            viewModel = TrendingMoviesViewModel(
                getTrendingMoviesUseCase,
                getGenresUseCase,
                TrendingMoviesStateReducers(MovieDomainToPresentationMapper(), GenreDomainToPresentationMapper(), fakeWeekRangeLabelProvider),
                filterAndSortMoviesUseCase,
                logger,
            )
            runTest(mainExtension.testDispatcher) {
                advanceUntilIdle()
                viewModel.handleIntent(TrendingMoviesIntent.SetSortSheetVisible(visible = true))
            }
        }

        @Nested
        @DisplayName("WHEN the user closes the sort options sheet")
        inner class WhenUserClosesTheSortOptionsSheet {

            @Test
            @DisplayName("THEN showSortSheet becomes false")
            fun hidesSortSheet() = runTest(mainExtension.testDispatcher) {
                viewModel.handleIntent(TrendingMoviesIntent.SetSortSheetVisible(visible = false))
                assertFalse(viewModel.state.value.showSortSheet)
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
                TrendingMoviesStateReducers(MovieDomainToPresentationMapper(), GenreDomainToPresentationMapper(), fakeWeekRangeLabelProvider),
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
                TrendingMoviesStateReducers(MovieDomainToPresentationMapper(), GenreDomainToPresentationMapper(), fakeWeekRangeLabelProvider),
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
                    assertEquals(PresentationMocks.PresentationMovies.all.toPersistentList(), state.movies)
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
                TrendingMoviesStateReducers(MovieDomainToPresentationMapper(), GenreDomainToPresentationMapper(), fakeWeekRangeLabelProvider),
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
                    assertEquals(PresentationMocks.PresentationMovies.all.toPersistentList(), state.movies)
                    assertTrue(state.errors.isNotEmpty())
                }
            }
        }
    }
}

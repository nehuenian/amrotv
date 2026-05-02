package nl.abnamro.amrotv.feature.movies.presentation.implementation.moviedetail

import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import nl.abnamro.amrotv.core.domain.model.Outcome
import nl.abnamro.amrotv.core.mvi.AmroTvViewModel
import nl.abnamro.amrotv.feature.movies.domain.api.usecase.GetMovieDetailUseCase
import nl.abnamro.amrotv.feature.movies.presentation.api.moviedetail.MovieDetailEffect
import nl.abnamro.amrotv.feature.movies.presentation.api.moviedetail.MovieDetailIntent
import nl.abnamro.amrotv.feature.movies.presentation.api.moviedetail.MovieDetailState
import nl.abnamro.amrotv.feature.movies.presentation.implementation.PresentationMocks
import nl.abnamro.amrotv.feature.movies.presentation.implementation.mapper.GenreDomainToPresentationMapper
import nl.abnamro.amrotv.feature.movies.presentation.implementation.mapper.MovieDetailDomainToPresentationMapper
import nl.abnamro.amrotv.feature.movies.presentation.implementation.util.MainDispatcherExtension
import nl.abnamro.amrotv.libraries.logger.api.Logger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class MovieDetailViewModelTest {

    @JvmField
    @RegisterExtension
    val mainExtension = MainDispatcherExtension()

    @MockK
    private lateinit var getMovieDetailUseCase: GetMovieDetailUseCase

    @MockK
    private lateinit var logger: Logger

    private lateinit var viewModel: AmroTvViewModel<MovieDetailState, MovieDetailIntent, MovieDetailEffect>

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = MovieDetailViewModel(
            getMovieDetailUseCase,
            MovieDetailStateReducers(
                MovieDetailDomainToPresentationMapper(
                    GenreDomainToPresentationMapper()
                )
            ),
            logger,
        )
    }

    @Nested
    @DisplayName("GIVEN the use case returns valid movie details")
    inner class GivenUseCaseReturnsValidMovieDetails {

        @BeforeEach
        fun setUp() {
            coEvery { getMovieDetailUseCase(any()) } returns Outcome.Success(
                PresentationMocks.Details.of(
                    id = PresentationMocks.Details.DEFAULT_ID
                )
            )
        }

        @Nested
        @DisplayName("WHEN the screen requests detail for a valid movie")
        inner class WhenScreenRequestsDetailForAValidMovie {

            @Test
            @DisplayName("THEN state transitions through loading to loaded with movie detail")
            fun stateTransitionsToLoaded() = runTest(mainExtension.testDispatcher) {
                viewModel.state.test {
                    awaitItem() // initial idle state
                    viewModel.handleIntent(MovieDetailIntent.LoadMovieDetail(movieId = PresentationMocks.Details.DEFAULT_ID))
                    val loading = awaitItem()
                    assertTrue(loading.isLoading)
                    advanceUntilIdle()
                    val loaded = awaitItem()
                    assertFalse(loaded.isLoading)
                    assertTrue(loaded.errors.isEmpty())
                    assertNotNull(loaded.movieDetail)
                    assertEquals(PresentationMocks.Details.DEFAULT_ID, loaded.movieDetail?.id)
                }
            }
        }

        @Nested
        @DisplayName("WHEN the user taps the IMDB link")
        inner class WhenUserTapsImdbLink {

            @Test
            @DisplayName("THEN the OpenUrl effect is emitted with the IMDB URL")
            fun emitsOpenUrlEffect() = runTest(mainExtension.testDispatcher) {
                viewModel.effects.test {
                    viewModel.handleIntent(MovieDetailIntent.OpenImdb(imdbId = PresentationMocks.Details.IMDB_ID))
                    val effect = awaitItem()
                    assertTrue(effect is MovieDetailEffect.OpenUrl)
                    assertEquals(
                        "$IMDB_TITLE_BASE_URL${PresentationMocks.Details.IMDB_ID}/",
                        (effect as MovieDetailEffect.OpenUrl).url,
                    )
                }
            }
        }

        @Nested
        @DisplayName("WHEN the user navigates back")
        inner class WhenUserNavigatesBack {

            @Test
            @DisplayName("THEN the NavigateBack effect is emitted")
            fun emitsNavigateBackEffect() = runTest(mainExtension.testDispatcher) {
                viewModel.effects.test {
                    viewModel.handleIntent(MovieDetailIntent.NavigateBack)
                    val effect = awaitItem()
                    assertTrue(effect is MovieDetailEffect.NavigateBack)
                }
            }
        }
    }

    @Nested
    @DisplayName("GIVEN the use case returns an error")
    inner class GivenUseCaseReturnsError {

        @BeforeEach
        fun setUp() {
            coEvery { getMovieDetailUseCase(any()) } returns Outcome.Error(RuntimeException("not found"))
        }

        @Nested
        @DisplayName("WHEN the screen requests detail for a movie")
        inner class WhenScreenRequestsDetailForAMovie {

            @Test
            @DisplayName("THEN state has an error and isLoading is false")
            fun stateHasError() = runTest(mainExtension.testDispatcher) {
                viewModel.state.test {
                    awaitItem() // initial idle state
                    viewModel.handleIntent(MovieDetailIntent.LoadMovieDetail(movieId = 99))
                    val loading = awaitItem()
                    assertTrue(loading.isLoading)
                    advanceUntilIdle()
                    val error = awaitItem()
                    assertFalse(error.isLoading)
                    assertTrue(error.errors.isNotEmpty())
                }
            }
        }
    }

    @Nested
    @DisplayName("GIVEN use case returns error on first load then success on retry, and LoadMovieDetail was already sent")
    inner class GivenLoadMovieDetailFailedAndRetryWillSucceed {

        @BeforeEach
        fun setUp() = runTest(mainExtension.testDispatcher) {
            coEvery { getMovieDetailUseCase(any()) } returnsMany listOf(
                Outcome.Error(RuntimeException("fail")),
                Outcome.Success(PresentationMocks.Details.of(id = PresentationMocks.Details.DEFAULT_ID)),
            )
            viewModel.handleIntent(MovieDetailIntent.LoadMovieDetail(movieId = PresentationMocks.Details.DEFAULT_ID))
            advanceUntilIdle()
        }

        @Nested
        @DisplayName("WHEN the user retries loading")
        inner class WhenUserRetriesLoading {

            @Test
            @DisplayName("THEN state becomes loaded")
            fun retryLoadsDetail() = runTest(mainExtension.testDispatcher) {
                viewModel.handleIntent(MovieDetailIntent.Retry)
                advanceUntilIdle()
                val loaded = viewModel.state.value
                assertFalse(loaded.isLoading)
                assertTrue(loaded.errors.isEmpty())
                assertNotNull(loaded.movieDetail)
            }
        }
    }

    @Nested
    @DisplayName("GIVEN no movie has been loaded yet")
    inner class GivenNoMovieHasBeenLoadedYet {

        @Nested
        @DisplayName("WHEN the user retries")
        inner class WhenUserRetries {

            @Test
            @DisplayName("THEN the use case is not called")
            fun retryIsIgnoredWhenNoIdLoaded() = runTest(mainExtension.testDispatcher) {
                viewModel.handleIntent(MovieDetailIntent.Retry)
                advanceUntilIdle()
                coVerify(exactly = 0) { getMovieDetailUseCase(any()) }
            }
        }
    }
}

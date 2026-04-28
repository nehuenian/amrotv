package nl.abnamro.amrotv.feature.movies.presentation.implementation.moviedetail

import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import nl.abnamro.amrotv.core.domain.model.Outcome
import nl.abnamro.amrotv.feature.movies.domain.api.usecase.GetMovieDetailUseCase
import nl.abnamro.amrotv.feature.movies.presentation.api.moviedetail.MovieDetailEffect
import nl.abnamro.amrotv.feature.movies.presentation.api.moviedetail.MovieDetailIntent
import nl.abnamro.amrotv.feature.movies.presentation.implementation.PresentationMocks
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

    private lateinit var viewModel: MovieDetailViewModel

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = MovieDetailViewModel(getMovieDetailUseCase, MovieDetailStateReducers(), logger)
    }

    @Nested
    @DisplayName("GIVEN use case returns success")
    inner class GivenUseCaseReturnsSuccess {

        @BeforeEach
        fun setUp() {
            coEvery { getMovieDetailUseCase(any()) } returns Outcome.Success(
                PresentationMocks.Details.of(
                    id = PresentationMocks.Details.DEFAULT_ID
                )
            )
        }

        @Nested
        @DisplayName("WHEN LoadMovieDetail intent is sent")
        inner class WhenLoadMovieDetailIsSent {

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
    }

    @Nested
    @DisplayName("GIVEN use case returns an error")
    inner class GivenUseCaseReturnsError {

        @BeforeEach
        fun setUp() {
            coEvery { getMovieDetailUseCase(any()) } returns Outcome.Error(RuntimeException("not found"))
        }

        @Nested
        @DisplayName("WHEN LoadMovieDetail intent is sent")
        inner class WhenLoadMovieDetailIsSent {

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
    @DisplayName("GIVEN an IMDB id is available")
    inner class GivenImdbIdIsAvailable {

        @Nested
        @DisplayName("WHEN OpenImdb intent is sent")
        inner class WhenOpenImdbIsSent {

            @Test
            @DisplayName("THEN OpenUrl effect is emitted with the correct IMDB URL")
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
    }
}

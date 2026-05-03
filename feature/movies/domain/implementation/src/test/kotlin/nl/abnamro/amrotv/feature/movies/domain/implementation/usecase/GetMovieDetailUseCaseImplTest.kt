package nl.abnamro.amrotv.feature.movies.domain.implementation.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import nl.abnamro.amrotv.core.domain.model.Outcome
import nl.abnamro.amrotv.feature.movies.domain.api.repository.MovieRepository
import nl.abnamro.amrotv.feature.movies.domain.api.usecase.GetMovieDetailUseCase
import nl.abnamro.amrotv.feature.movies.domain.implementation.usecase.MovieDomainMocks.Details
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GetMovieDetailUseCaseImplTest {

    @MockK lateinit var repository: MovieRepository

    private lateinit var useCase: GetMovieDetailUseCase

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = GetMovieDetailUseCaseImpl(repository)
    }

    @Nested
    @DisplayName("GIVEN a valid movie id")
    inner class GivenValidMovieId {

        private val movieId = 42
        private val expectedDetail = Details.of(movieId)

        @BeforeEach
        fun setUp() {
            coEvery { repository.getMovieDetail(movieId) } returns Outcome.Success(expectedDetail)
        }

        @Nested
        @DisplayName("WHEN the use case is invoked with that id")
        inner class WhenInvoked {

            @Test
            @DisplayName("THEN it returns the movie detail")
            fun returnsMovieDetail() = runTest {
                val result = useCase(movieId)
                assertEquals(Outcome.Success(expectedDetail), result)
            }
        }
    }
}

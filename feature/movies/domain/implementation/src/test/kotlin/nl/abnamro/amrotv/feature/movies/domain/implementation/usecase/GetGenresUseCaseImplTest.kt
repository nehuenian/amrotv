package nl.abnamro.amrotv.feature.movies.domain.implementation.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import nl.abnamro.amrotv.core.domain.model.Outcome
import nl.abnamro.amrotv.feature.movies.domain.api.repository.MovieRepository
import nl.abnamro.amrotv.feature.movies.domain.api.usecase.GetGenresUseCase
import nl.abnamro.amrotv.feature.movies.domain.implementation.usecase.MovieDomainMocks.Genres
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GetGenresUseCaseImplTest {

    @MockK lateinit var repository: MovieRepository

    private lateinit var useCase: GetGenresUseCase

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = GetGenresUseCaseImpl(repository)
    }

    @Nested
    @DisplayName("GIVEN a repository with genres available")
    inner class GivenRepositoryWithGenres {

        @BeforeEach
        fun setUp() {
            coEvery { repository.getGenres() } returns Outcome.Success(Genres.all)
        }

        @Nested
        @DisplayName("WHEN the use case is invoked")
        inner class WhenInvoked {

            @Test
            @DisplayName("THEN it returns the genre list")
            fun returnsGenreList() = runTest {
                val result = useCase()
                assertEquals(Outcome.Success(Genres.all), result)
            }
        }
    }
}

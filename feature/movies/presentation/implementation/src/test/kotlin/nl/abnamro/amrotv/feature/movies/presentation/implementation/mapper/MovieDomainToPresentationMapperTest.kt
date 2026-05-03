package nl.abnamro.amrotv.feature.movies.presentation.implementation.mapper

import java.time.LocalDate
import kotlinx.collections.immutable.persistentListOf
import nl.abnamro.amrotv.core.mvi.Mapper
import nl.abnamro.amrotv.feature.movies.domain.api.model.Movie
import nl.abnamro.amrotv.feature.movies.presentation.api.model.MoviePresentationModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MovieDomainToPresentationMapperTest {

    private lateinit var mapper: Mapper<Movie, MoviePresentationModel>

    @BeforeEach
    fun setUp() {
        mapper = MovieDomainToPresentationMapper()
    }

    @Nested
    @DisplayName("GIVEN a movie with all fields populated and a valid ISO release date")
    inner class GivenAMovieWithAllFieldsAndValidReleaseDate {

        private val movie =
            Movie(
                id = 1,
                title = "The Dark Knight",
                posterUrl = "https://example.com/poster.jpg",
                backdropUrl = "https://example.com/backdrop.jpg",
                genreIds = listOf(28, 80, 53),
                popularity = 92.5,
                releaseDate = LocalDate.of(2008, 7, 18),
                voteAverage = 9.0,
            )

        @Nested
        @DisplayName("WHEN mapped to presentation model")
        inner class WhenMapped {

            @Test
            @DisplayName("THEN id is preserved")
            fun idIsPreserved() {
                assertEquals(movie.id, mapper.map(movie).id)
            }

            @Test
            @DisplayName("THEN title is preserved")
            fun titleIsPreserved() {
                assertEquals(movie.title, mapper.map(movie).title)
            }

            @Test
            @DisplayName("THEN posterUrl is preserved")
            fun posterUrlIsPreserved() {
                assertEquals(movie.posterUrl, mapper.map(movie).posterUrl)
            }

            @Test
            @DisplayName("THEN backdropUrl is preserved")
            fun backdropUrlIsPreserved() {
                assertEquals(movie.backdropUrl, mapper.map(movie).backdropUrl)
            }

            @Test
            @DisplayName("THEN genreIds are converted to ImmutableList")
            fun genreIdsConvertedToImmutableList() {
                assertEquals(persistentListOf(28, 80, 53), mapper.map(movie).genreIds)
            }

            @Test
            @DisplayName("THEN popularity is preserved")
            fun popularityIsPreserved() {
                assertEquals(movie.popularity, mapper.map(movie).popularity)
            }

            @Test
            @DisplayName("THEN voteAverage is formatted as a one-decimal rating string")
            fun voteAverageIsFormattedAsRating() {
                assertEquals("9.0", mapper.map(movie).formattedRating)
            }

            @Test
            @DisplayName("THEN releaseDate is formatted as MMM d, yyyy")
            fun releaseDateIsFormatted() {
                assertEquals("Jul 18, 2008", mapper.map(movie).releaseDate)
            }

            @Test
            @DisplayName("THEN isReleased is true for a past release date")
            fun isReleasedIsTrueForPastDate() {
                assertTrue(mapper.map(movie).isReleased)
            }
        }
    }

    @Nested
    @DisplayName("GIVEN a movie with null optional fields")
    inner class GivenAMovieWithNullOptionalFields {

        private val movie =
            Movie(
                id = 2,
                title = "Unknown",
                posterUrl = null,
                backdropUrl = null,
                genreIds = emptyList(),
                popularity = 0.0,
                releaseDate = LocalDate.of(2020, 1, 1),
                voteAverage = 0.0,
            )

        @Nested
        @DisplayName("WHEN mapped to presentation model")
        inner class WhenMapped {

            @Test
            @DisplayName("THEN posterUrl is null")
            fun posterUrlIsNull() {
                assertNull(mapper.map(movie).posterUrl)
            }

            @Test
            @DisplayName("THEN backdropUrl is null")
            fun backdropUrlIsNull() {
                assertNull(mapper.map(movie).backdropUrl)
            }

            @Test
            @DisplayName("THEN genreIds is empty")
            fun genreIdsIsEmpty() {
                assertEquals(persistentListOf<Int>(), mapper.map(movie).genreIds)
            }
        }
    }

    @Nested
    @DisplayName("GIVEN a movie with no release date (null)")
    inner class GivenAMovieWithNullReleaseDate {

        private val movie =
            Movie(
                id = 3,
                title = "Unknown Date",
                posterUrl = null,
                backdropUrl = null,
                genreIds = emptyList(),
                popularity = 0.0,
                releaseDate = null,
                voteAverage = 0.0,
            )

        @Nested
        @DisplayName("WHEN mapped to presentation model")
        inner class WhenMapped {

            @Test
            @DisplayName("THEN releaseDate is null")
            fun releaseDateIsNull() {
                assertNull(mapper.map(movie).releaseDate)
            }

            @Test
            @DisplayName("THEN isReleased is false")
            fun isReleasedIsFalse() {
                assertFalse(mapper.map(movie).isReleased)
            }
        }
    }

    @Nested
    @DisplayName("GIVEN a movie with a future release date")
    inner class GivenAMovieWithFutureReleaseDate {

        private val movie =
            Movie(
                id = 4,
                title = "Upcoming Movie",
                posterUrl = null,
                backdropUrl = null,
                genreIds = emptyList(),
                popularity = 0.0,
                releaseDate = LocalDate.of(2099, 1, 1),
                voteAverage = 0.0,
            )

        @Nested
        @DisplayName("WHEN mapped to presentation model")
        inner class WhenMapped {

            @Test
            @DisplayName("THEN isReleased is false")
            fun isReleasedIsFalse() {
                assertFalse(mapper.map(movie).isReleased)
            }
        }
    }
}

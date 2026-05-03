package nl.abnamro.amrotv.feature.movies.presentation.implementation.mapper

import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale
import kotlinx.collections.immutable.persistentListOf
import nl.abnamro.amrotv.core.mvi.Mapper
import nl.abnamro.amrotv.feature.movies.domain.api.model.Genre
import nl.abnamro.amrotv.feature.movies.domain.api.model.MovieDetail
import nl.abnamro.amrotv.feature.movies.presentation.api.model.GenrePresentationModel
import nl.abnamro.amrotv.feature.movies.presentation.api.model.MovieDetailPresentationModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MovieDetailDomainToPresentationMapperTest {

    private lateinit var mapper: Mapper<MovieDetail, MovieDetailPresentationModel>

    @BeforeEach
    fun setUp() {
        mapper = MovieDetailDomainToPresentationMapper(GenreDomainToPresentationMapper())
    }

    @Nested
    @DisplayName("GIVEN a fully populated movie detail with genres and a valid ISO release date")
    inner class GivenFullyPopulatedMovieDetail {

        private val movieDetail =
            MovieDetail(
                id = 155,
                title = "The Dark Knight",
                tagline = "Why so serious?",
                posterUrl = "https://example.com/poster.jpg",
                backdropUrl = "https://example.com/backdrop.jpg",
                genres = listOf(Genre(28, "Action"), Genre(80, "Crime"), Genre(53, "Thriller")),
                overview = "Batman faces the Joker.",
                voteAverage = 9.0,
                voteCount = 30_455,
                budget = 185_000_000L,
                revenue = 1_004_934_033L,
                imdbId = "tt0468569",
                status = "Released",
                runtimeInMinutes = 152,
                releaseDate = LocalDate.of(2008, 7, 18),
            )

        @Nested
        @DisplayName("WHEN mapped to presentation model")
        inner class WhenMapped {

            @Test
            @DisplayName("THEN id is preserved")
            fun idIsPreserved() {
                assertEquals(movieDetail.id, mapper.map(movieDetail).id)
            }

            @Test
            @DisplayName("THEN title is preserved")
            fun titleIsPreserved() {
                assertEquals(movieDetail.title, mapper.map(movieDetail).title)
            }

            @Test
            @DisplayName("THEN tagline is preserved")
            fun taglineIsPreserved() {
                assertEquals(movieDetail.tagline, mapper.map(movieDetail).tagline)
            }

            @Test
            @DisplayName("THEN posterUrl is preserved")
            fun posterUrlIsPreserved() {
                assertEquals(movieDetail.posterUrl, mapper.map(movieDetail).posterUrl)
            }

            @Test
            @DisplayName("THEN backdropUrl is preserved")
            fun backdropUrlIsPreserved() {
                assertEquals(movieDetail.backdropUrl, mapper.map(movieDetail).backdropUrl)
            }

            @Test
            @DisplayName("THEN genres are mapped to GenrePresentationModel ImmutableList")
            fun genresMappedToImmutableList() {
                val expected =
                    persistentListOf(
                        GenrePresentationModel(28, "Action"),
                        GenrePresentationModel(80, "Crime"),
                        GenrePresentationModel(53, "Thriller"),
                    )
                assertEquals(expected, mapper.map(movieDetail).genres)
            }

            @Test
            @DisplayName("THEN overview is preserved")
            fun overviewIsPreserved() {
                assertEquals(movieDetail.overview, mapper.map(movieDetail).overview)
            }

            @Test
            @DisplayName("THEN voteAverage is formatted as a one-decimal rating string")
            fun voteAverageIsFormattedAsRating() {
                assertEquals("9.0", mapper.map(movieDetail).formattedRating)
            }

            @Test
            @DisplayName("THEN voteCount is preserved")
            fun voteCountIsPreserved() {
                assertEquals(movieDetail.voteCount, mapper.map(movieDetail).voteCount)
            }

            @Test
            @DisplayName("THEN budget is formatted as US currency")
            fun budgetIsFormattedAsCurrency() {
                val expected = NumberFormat.getCurrencyInstance(Locale.US).format(185_000_000L)
                assertEquals(expected, mapper.map(movieDetail).formattedBudget)
            }

            @Test
            @DisplayName("THEN revenue is formatted as US currency")
            fun revenueIsFormattedAsCurrency() {
                val expected = NumberFormat.getCurrencyInstance(Locale.US).format(1_004_934_033L)
                assertEquals(expected, mapper.map(movieDetail).formattedRevenue)
            }

            @Test
            @DisplayName("THEN imdbId is preserved")
            fun imdbIdIsPreserved() {
                assertEquals(movieDetail.imdbId, mapper.map(movieDetail).imdbId)
            }

            @Test
            @DisplayName("THEN status is preserved")
            fun statusIsPreserved() {
                assertEquals(movieDetail.status, mapper.map(movieDetail).status)
            }

            @Test
            @DisplayName("THEN runtimeInMinutes is preserved")
            fun runtimeInMinutesIsPreserved() {
                assertEquals(movieDetail.runtimeInMinutes, mapper.map(movieDetail).runtimeInMinutes)
            }

            @Test
            @DisplayName("THEN releaseDate is formatted as MMM d, yyyy")
            fun releaseDateIsFormatted() {
                assertEquals("Jul 18, 2008", mapper.map(movieDetail).releaseDate)
            }
        }
    }

    @Nested
    @DisplayName("GIVEN a movie detail with all nullable fields set to null")
    inner class GivenMovieDetailWithNullableFieldsNull {

        private val movieDetail =
            MovieDetail(
                id = 42,
                title = "Minimal Movie",
                tagline = null,
                posterUrl = null,
                backdropUrl = null,
                genres = emptyList(),
                overview = "Short overview.",
                voteAverage = 5.0,
                voteCount = 10,
                budget = null,
                revenue = null,
                imdbId = null,
                status = "Released",
                runtimeInMinutes = null,
                releaseDate = LocalDate.of(2020, 1, 1),
            )

        @Nested
        @DisplayName("WHEN mapped to presentation model")
        inner class WhenMapped {

            @Test
            @DisplayName("THEN tagline is null")
            fun taglineIsNull() {
                assertNull(mapper.map(movieDetail).tagline)
            }

            @Test
            @DisplayName("THEN posterUrl is null")
            fun posterUrlIsNull() {
                assertNull(mapper.map(movieDetail).posterUrl)
            }

            @Test
            @DisplayName("THEN backdropUrl is null")
            fun backdropUrlIsNull() {
                assertNull(mapper.map(movieDetail).backdropUrl)
            }

            @Test
            @DisplayName("THEN genres list is empty")
            fun genresIsEmpty() {
                assertEquals(
                    persistentListOf<GenrePresentationModel>(),
                    mapper.map(movieDetail).genres,
                )
            }

            @Test
            @DisplayName("THEN formattedBudget is null")
            fun budgetIsNull() {
                assertNull(mapper.map(movieDetail).formattedBudget)
            }

            @Test
            @DisplayName("THEN formattedRevenue is null")
            fun revenueIsNull() {
                assertNull(mapper.map(movieDetail).formattedRevenue)
            }

            @Test
            @DisplayName("THEN imdbId is null")
            fun imdbIdIsNull() {
                assertNull(mapper.map(movieDetail).imdbId)
            }

            @Test
            @DisplayName("THEN runtimeInMinutes is null")
            fun runtimeInMinutesIsNull() {
                assertNull(mapper.map(movieDetail).runtimeInMinutes)
            }
        }
    }

    @Nested
    @DisplayName("GIVEN a movie detail with null releaseDate")
    inner class GivenMovieDetailWithNullReleaseDate {

        private val movieDetail =
            MovieDetail(
                id = 99,
                title = "No Date Movie",
                tagline = null,
                posterUrl = null,
                backdropUrl = null,
                genres = emptyList(),
                overview = "Overview.",
                voteAverage = 0.0,
                voteCount = 0,
                budget = null,
                revenue = null,
                imdbId = null,
                status = "Unknown",
                runtimeInMinutes = null,
                releaseDate = null,
            )

        @Nested
        @DisplayName("WHEN mapped to presentation model")
        inner class WhenMapped {

            @Test
            @DisplayName("THEN releaseDate is null")
            fun releaseDateIsNullWhenInputIsNull() {
                assertNull(mapper.map(movieDetail).releaseDate)
            }
        }
    }

    @Nested
    @DisplayName("GIVEN a movie detail with budget and revenue equal to zero")
    inner class GivenMovieDetailWithZeroBudgetAndRevenue {

        private val movieDetail =
            MovieDetail(
                id = 1,
                title = "Zero Budget Film",
                tagline = null,
                posterUrl = null,
                backdropUrl = null,
                genres = emptyList(),
                overview = "Overview.",
                voteAverage = 5.0,
                voteCount = 1,
                budget = 0L,
                revenue = 0L,
                imdbId = null,
                status = "Released",
                runtimeInMinutes = null,
                releaseDate = LocalDate.of(2000, 1, 1),
            )

        @Nested
        @DisplayName("WHEN mapped to presentation model")
        inner class WhenMapped {

            @Test
            @DisplayName("THEN formattedBudget is null")
            fun formattedBudgetIsNullForZero() {
                assertNull(mapper.map(movieDetail).formattedBudget)
            }

            @Test
            @DisplayName("THEN formattedRevenue is null")
            fun formattedRevenueIsNullForZero() {
                assertNull(mapper.map(movieDetail).formattedRevenue)
            }
        }
    }
}

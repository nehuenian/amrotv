package nl.abnamro.amrotv.feature.movies.data.datasource.remote.tmdb.dto

import nl.abnamro.amrotv.feature.movies.data.MovieDataMocks.Dtos
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("TmdbMovieDtoMapper")
internal class TmdbMovieDtoMapperTest {

    @Nested
    @DisplayName("GIVEN a MovieDto with non-null posterPath and backdropPath")
    inner class GivenMovieDtoWithImages {

        @Nested
        @DisplayName("WHEN toDomain() is called")
        inner class WhenToDomainIsCalled {

            @Test
            @DisplayName("THEN posterUrl is prefixed with the image base URL")
            fun posterUrlPrefixed() {
                assertEquals(
                    "https://image.tmdb.org/t/p/w500/poster.jpg",
                    Dtos.movieDto.toDomain().posterUrl,
                )
            }

            @Test
            @DisplayName("THEN backdropUrl is prefixed with the image base URL")
            fun backdropUrlPrefixed() {
                assertEquals(
                    "https://image.tmdb.org/t/p/w500/backdrop.jpg",
                    Dtos.movieDto.toDomain().backdropUrl,
                )
            }

            @Test
            @DisplayName("THEN passthrough fields are mapped to domain")
            fun passthroughFieldsMapped() {
                val result = Dtos.movieDto.toDomain()
                assertEquals(Dtos.movieDto.id, result.id)
                assertEquals(Dtos.movieDto.title, result.title)
                assertEquals(Dtos.movieDto.genreIds, result.genreIds)
                assertEquals(Dtos.movieDto.popularity, result.popularity)
                assertEquals(Dtos.movieDto.releaseDate, result.releaseDate)
                assertEquals(Dtos.movieDto.voteAverage, result.voteAverage)
            }
        }
    }

    @Nested
    @DisplayName("GIVEN a MovieDto with null posterPath and backdropPath")
    inner class GivenMovieDtoWithNullImages {

        @Nested
        @DisplayName("WHEN toDomain() is called")
        inner class WhenToDomainIsCalled {

            @Test
            @DisplayName("THEN posterUrl is null")
            fun posterUrlIsNull() {
                assertNull(Dtos.movieDtoNullImages.toDomain().posterUrl)
            }

            @Test
            @DisplayName("THEN backdropUrl is null")
            fun backdropUrlIsNull() {
                assertNull(Dtos.movieDtoNullImages.toDomain().backdropUrl)
            }
        }
    }

    @Nested
    @DisplayName("GIVEN a MovieDetailDto with all fields populated")
    inner class GivenMovieDetailDtoAllFieldsPopulated {

        @Nested
        @DisplayName("WHEN toDomain() is called")
        inner class WhenToDomainIsCalled {

            @Test
            @DisplayName("THEN passthrough scalar fields are mapped to domain")
            fun passthroughFieldsMapped() {
                val result = Dtos.movieDetailDto.toDomain()
                assertEquals(Dtos.movieDetailDto.id, result.id)
                assertEquals(Dtos.movieDetailDto.title, result.title)
                assertEquals(Dtos.movieDetailDto.tagline, result.tagline)
                assertEquals(Dtos.movieDetailDto.overview, result.overview)
                assertEquals(Dtos.movieDetailDto.voteAverage, result.voteAverage)
                assertEquals(Dtos.movieDetailDto.voteCount, result.voteCount)
                assertEquals(Dtos.movieDetailDto.status, result.status)
                assertEquals(Dtos.movieDetailDto.runtime, result.runtimeInMinutes)
                assertEquals(Dtos.movieDetailDto.releaseDate, result.releaseDate)
            }

            @Test
            @DisplayName("THEN posterUrl is prefixed with the image base URL")
            fun posterUrlPrefixed() {
                assertEquals(
                    "https://image.tmdb.org/t/p/w500/detail_poster.jpg",
                    Dtos.movieDetailDto.toDomain().posterUrl,
                )
            }

            @Test
            @DisplayName("THEN genres are mapped to domain Genre objects")
            fun genresMapped() {
                val result = Dtos.movieDetailDto.toDomain()
                assertEquals(1, result.genres.size)
                assertEquals(28, result.genres.first().id)
                assertEquals("Action", result.genres.first().name)
            }

            @Test
            @DisplayName("THEN budget is preserved when positive")
            fun budgetPreserved() {
                assertEquals(50_000_000L, Dtos.movieDetailDto.toDomain().budget)
            }

            @Test
            @DisplayName("THEN revenue is preserved when positive")
            fun revenuePreserved() {
                assertEquals(120_000_000L, Dtos.movieDetailDto.toDomain().revenue)
            }

            @Test
            @DisplayName("THEN imdbId is preserved when non-null")
            fun imdbIdPreserved() {
                assertEquals("tt1234567", Dtos.movieDetailDto.toDomain().imdbId)
            }
        }
    }

    @Nested
    @DisplayName("GIVEN a MovieDetailDto with null tagline")
    inner class GivenMovieDetailDtoNullTagline {

        @Nested
        @DisplayName("WHEN toDomain() is called")
        inner class WhenToDomainIsCalled {

            @Test
            @DisplayName("THEN tagline is null")
            fun taglineIsNull() {
                assertNull(Dtos.movieDetailDtoNullTagline.toDomain().tagline)
            }
        }
    }

    @Nested
    @DisplayName("GIVEN a MovieDetailDto with budget = 0")
    inner class GivenMovieDetailDtoBudgetZero {

        @Nested
        @DisplayName("WHEN toDomain() is called")
        inner class WhenToDomainIsCalled {

            @Test
            @DisplayName("THEN budget is null")
            fun budgetIsNull() {
                assertNull(Dtos.movieDetailDtoBudgetZero.toDomain().budget)
            }
        }
    }

    @Nested
    @DisplayName("GIVEN a MovieDetailDto with revenue = 0")
    inner class GivenMovieDetailDtoRevenueZero {

        @Nested
        @DisplayName("WHEN toDomain() is called")
        inner class WhenToDomainIsCalled {

            @Test
            @DisplayName("THEN revenue is null")
            fun revenueIsNull() {
                assertNull(Dtos.movieDetailDtoRevenueZero.toDomain().revenue)
            }
        }
    }

    @Nested
    @DisplayName("GIVEN a MovieDetailDto with null imdbId")
    inner class GivenMovieDetailDtoNullImdbId {

        @Nested
        @DisplayName("WHEN toDomain() is called")
        inner class WhenToDomainIsCalled {

            @Test
            @DisplayName("THEN imdbId is null")
            fun imdbIdIsNull() {
                assertNull(Dtos.movieDetailDtoNullImdbId.toDomain().imdbId)
            }
        }
    }

    @Nested
    @DisplayName("GIVEN a GenreDto")
    inner class GivenGenreDto {

        @Nested
        @DisplayName("WHEN toDomain() is called")
        inner class WhenToDomainIsCalled {

            @Test
            @DisplayName("THEN id and name are mapped to Genre correctly")
            fun idAndNameMapped() {
                val result = Dtos.genreDto.toDomain()
                assertEquals(28, result.id)
                assertEquals("Action", result.name)
            }
        }
    }
}

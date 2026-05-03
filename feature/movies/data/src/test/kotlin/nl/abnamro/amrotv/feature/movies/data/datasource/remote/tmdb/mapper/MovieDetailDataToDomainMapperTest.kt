package nl.abnamro.amrotv.feature.movies.data.datasource.remote.tmdb.mapper

import java.time.LocalDate
import nl.abnamro.amrotv.core.mvi.Mapper
import nl.abnamro.amrotv.feature.movies.data.MovieDataMocks.Dtos
import nl.abnamro.amrotv.feature.movies.data.datasource.remote.tmdb.dto.MovieDetailDto
import nl.abnamro.amrotv.feature.movies.domain.api.model.MovieDetail
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("MovieDetailDataToDomainMapper")
internal class MovieDetailDataToDomainMapperTest {

    private val genreMapperImpl = GenreDataToDomainMapper()
    private val mapper: Mapper<MovieDetailDto, MovieDetail> = MovieDetailDataToDomainMapper(genreMapperImpl)

    @Nested
    @DisplayName("GIVEN a fully-populated MovieDetailDto")
    inner class GivenFullyPopulatedMovieDetailDto {

        @Nested
        @DisplayName("WHEN converting it to a domain MovieDetail")
        inner class WhenConvertingToADomainMovieDetail {

            @Test
            @DisplayName("THEN id is preserved")
            fun idIsPreserved() {
                assertEquals(Dtos.movieDetailDto.id, mapper.map(Dtos.movieDetailDto).id)
            }

            @Test
            @DisplayName("THEN title is preserved")
            fun titleIsPreserved() {
                assertEquals(Dtos.movieDetailDto.title, mapper.map(Dtos.movieDetailDto).title)
            }

            @Test
            @DisplayName("THEN tagline is preserved")
            fun taglineIsPreserved() {
                assertEquals(Dtos.movieDetailDto.tagline, mapper.map(Dtos.movieDetailDto).tagline)
            }

            @Test
            @DisplayName("THEN overview is preserved")
            fun overviewIsPreserved() {
                assertEquals(Dtos.movieDetailDto.overview, mapper.map(Dtos.movieDetailDto).overview)
            }

            @Test
            @DisplayName("THEN voteAverage is preserved")
            fun voteAverageIsPreserved() {
                assertEquals(Dtos.movieDetailDto.voteAverage, mapper.map(Dtos.movieDetailDto).voteAverage)
            }

            @Test
            @DisplayName("THEN voteCount is preserved")
            fun voteCountIsPreserved() {
                assertEquals(Dtos.movieDetailDto.voteCount, mapper.map(Dtos.movieDetailDto).voteCount)
            }

            @Test
            @DisplayName("THEN status is preserved")
            fun statusIsPreserved() {
                assertEquals(Dtos.movieDetailDto.status, mapper.map(Dtos.movieDetailDto).status)
            }

            @Test
            @DisplayName("THEN runtimeInMinutes is preserved")
            fun runtimeInMinutesIsPreserved() {
                assertEquals(Dtos.movieDetailDto.runtime, mapper.map(Dtos.movieDetailDto).runtimeInMinutes)
            }

            @Test
            @DisplayName("THEN release date is parsed to LocalDate")
            fun releaseDateIsParsed() {
                assertEquals(LocalDate.of(2024, 6, 15), mapper.map(Dtos.movieDetailDto).releaseDate)
            }

            @Test
            @DisplayName("THEN posterUrl is prefixed with the image base URL")
            fun posterUrlPrefixed() {
                assertEquals(
                    "https://image.tmdb.org/t/p/w500/detail_poster.jpg",
                    mapper.map(Dtos.movieDetailDto).posterUrl,
                )
            }

            @Test
            @DisplayName("THEN genres are mapped to domain Genre objects")
            fun genresMapped() {
                val result = mapper.map(Dtos.movieDetailDto)
                assertEquals(1, result.genres.size)
                assertEquals(28, result.genres.first().id)
                assertEquals("Action", result.genres.first().name)
            }

            @Test
            @DisplayName("THEN budget is preserved when positive")
            fun budgetPreserved() {
                assertEquals(50_000_000L, mapper.map(Dtos.movieDetailDto).budget)
            }

            @Test
            @DisplayName("THEN revenue is preserved when positive")
            fun revenuePreserved() {
                assertEquals(120_000_000L, mapper.map(Dtos.movieDetailDto).revenue)
            }

            @Test
            @DisplayName("THEN imdbId is preserved when non-null")
            fun imdbIdPreserved() {
                assertEquals("tt1234567", mapper.map(Dtos.movieDetailDto).imdbId)
            }
        }
    }

    @Nested
    @DisplayName("GIVEN a MovieDetailDto with a null tagline")
    inner class GivenMovieDetailDtoWithNullTagline {

        @Nested
        @DisplayName("WHEN converting it to a domain MovieDetail")
        inner class WhenConvertingToADomainMovieDetail {

            @Test
            @DisplayName("THEN tagline is null")
            fun taglineIsNull() {
                assertNull(mapper.map(Dtos.movieDetailDtoNullTagline).tagline)
            }
        }
    }

    @Nested
    @DisplayName("GIVEN a MovieDetailDto with an unknown budget")
    inner class GivenMovieDetailDtoWithUnknownBudget {

        @Nested
        @DisplayName("WHEN converting it to a domain MovieDetail")
        inner class WhenConvertingToADomainMovieDetail {

            @Test
            @DisplayName("THEN budget is null")
            fun budgetIsNull() {
                assertNull(mapper.map(Dtos.movieDetailDtoBudgetZero).budget)
            }
        }
    }

    @Nested
    @DisplayName("GIVEN a MovieDetailDto with an unknown revenue")
    inner class GivenMovieDetailDtoWithUnknownRevenue {

        @Nested
        @DisplayName("WHEN converting it to a domain MovieDetail")
        inner class WhenConvertingToADomainMovieDetail {

            @Test
            @DisplayName("THEN revenue is null")
            fun revenueIsNull() {
                assertNull(mapper.map(Dtos.movieDetailDtoRevenueZero).revenue)
            }
        }
    }

    @Nested
    @DisplayName("GIVEN a MovieDetailDto with a null IMDb ID")
    inner class GivenMovieDetailDtoWithNullImdbId {

        @Nested
        @DisplayName("WHEN converting it to a domain MovieDetail")
        inner class WhenConvertingToADomainMovieDetail {

            @Test
            @DisplayName("THEN imdbId is null")
            fun imdbIdIsNull() {
                assertNull(mapper.map(Dtos.movieDetailDtoNullImdbId).imdbId)
            }
        }
    }
}

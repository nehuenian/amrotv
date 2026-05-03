package nl.abnamro.amrotv.feature.movies.data.datasource.remote.tmdb.mapper

import java.time.LocalDate
import nl.abnamro.amrotv.core.mvi.Mapper
import nl.abnamro.amrotv.feature.movies.data.MovieDataMocks.Dtos
import nl.abnamro.amrotv.feature.movies.data.datasource.remote.tmdb.dto.MovieDto
import nl.abnamro.amrotv.feature.movies.domain.api.model.Movie
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("MovieDataToDomainMapper")
internal class MovieDataToDomainMapperTest {

    private val mapper: Mapper<MovieDto, Movie> = MovieDataToDomainMapper()

    @Nested
    @DisplayName("GIVEN a MovieDto with non-null posterPath and backdropPath")
    inner class GivenMovieDtoWithImages {

        @Nested
        @DisplayName("WHEN converting it to a domain Movie")
        inner class WhenConvertingToADomainMovie {

            private val movie = mapper.map(Dtos.movieDto)

            @Test
            @DisplayName("THEN posterUrl is prefixed with the image base URL")
            fun posterUrlPrefixed() {
                assertEquals(
                    "https://image.tmdb.org/t/p/w500/poster.jpg",
                    movie.posterUrl,
                )
            }

            @Test
            @DisplayName("THEN backdropUrl is prefixed with the image base URL")
            fun backdropUrlPrefixed() {
                assertEquals(
                    "https://image.tmdb.org/t/p/w500/backdrop.jpg",
                    movie.backdropUrl,
                )
            }

            @Test
            @DisplayName("THEN id is preserved")
            fun idIsPreserved() {
                assertEquals(Dtos.movieDto.id, movie.id)
            }

            @Test
            @DisplayName("THEN title is preserved")
            fun titleIsPreserved() {
                assertEquals(Dtos.movieDto.title, movie.title)
            }

            @Test
            @DisplayName("THEN genreIds are preserved")
            fun genreIdsArePreserved() {
                assertEquals(Dtos.movieDto.genreIds, movie.genreIds)
            }

            @Test
            @DisplayName("THEN popularity is preserved")
            fun popularityIsPreserved() {
                assertEquals(Dtos.movieDto.popularity, movie.popularity)
            }

            @Test
            @DisplayName("THEN release date is parsed to LocalDate")
            fun releaseDateIsParsed() {
                assertEquals(LocalDate.of(2024, 1, 1), movie.releaseDate)
            }

            @Test
            @DisplayName("THEN voteAverage is preserved")
            fun voteAverageIsPreserved() {
                assertEquals(Dtos.movieDto.voteAverage, movie.voteAverage)
            }
        }
    }

    @Nested
    @DisplayName("GIVEN a MovieDto with null posterPath and backdropPath")
    inner class GivenMovieDtoWithNullImages {

        @Nested
        @DisplayName("WHEN converting it to a domain Movie")
        inner class WhenConvertingToADomainMovie {

            @Test
            @DisplayName("THEN posterUrl is null")
            fun posterUrlIsNull() {
                assertNull(mapper.map(Dtos.movieDtoNullImages).posterUrl)
            }

            @Test
            @DisplayName("THEN backdropUrl is null")
            fun backdropUrlIsNull() {
                assertNull(mapper.map(Dtos.movieDtoNullImages).backdropUrl)
            }
        }
    }

    @Nested
    @DisplayName("GIVEN a MovieDto with an empty releaseDate")
    inner class GivenMovieDtoWithEmptyReleaseDate {

        @Nested
        @DisplayName("WHEN converting it to a domain Movie")
        inner class WhenConvertingToADomainMovie {

            @Test
            @DisplayName("THEN releaseDate is null")
            fun releaseDateIsNull() {
                assertNull(mapper.map(Dtos.movieDto.copy(releaseDate = "")).releaseDate)
            }
        }
    }

    @Nested
    @DisplayName("GIVEN a MovieDto with a malformed release date")
    inner class GivenMovieDtoWithMalformedReleaseDate {

        @Nested
        @DisplayName("WHEN converting it to a domain Movie")
        inner class WhenConvertingToADomainMovie {

            @Test
            @DisplayName("THEN releaseDate is null")
            fun releaseDateIsNull() {
                assertNull(mapper.map(Dtos.movieDto.copy(releaseDate = "01/31/2024")).releaseDate)
            }
        }
    }
}

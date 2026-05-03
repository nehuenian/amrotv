package nl.abnamro.amrotv.feature.movies.data.datasource.remote.tmdb.dto

import nl.abnamro.amrotv.core.mvi.Mapper
import nl.abnamro.amrotv.feature.movies.data.MovieDataMocks.Dtos
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

            @Test
            @DisplayName("THEN posterUrl is prefixed with the image base URL")
            fun posterUrlPrefixed() {
                assertEquals(
                    "https://image.tmdb.org/t/p/w500/poster.jpg",
                    mapper.map(Dtos.movieDto).posterUrl,
                )
            }

            @Test
            @DisplayName("THEN backdropUrl is prefixed with the image base URL")
            fun backdropUrlPrefixed() {
                assertEquals(
                    "https://image.tmdb.org/t/p/w500/backdrop.jpg",
                    mapper.map(Dtos.movieDto).backdropUrl,
                )
            }

            @Test
            @DisplayName("THEN passthrough fields are mapped to domain")
            fun passthroughFieldsMapped() {
                val result = mapper.map(Dtos.movieDto)
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
}

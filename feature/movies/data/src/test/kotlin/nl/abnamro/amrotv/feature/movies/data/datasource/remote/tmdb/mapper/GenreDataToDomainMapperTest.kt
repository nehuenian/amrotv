package nl.abnamro.amrotv.feature.movies.data.datasource.remote.tmdb.mapper

import nl.abnamro.amrotv.core.mvi.Mapper
import nl.abnamro.amrotv.feature.movies.data.MovieDataMocks.Dtos
import nl.abnamro.amrotv.feature.movies.data.datasource.remote.tmdb.dto.GenreDto
import nl.abnamro.amrotv.feature.movies.domain.api.model.Genre
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("GenreDataToDomainMapper")
internal class GenreDataToDomainMapperTest {

    private val mapper: Mapper<GenreDto, Genre> = GenreDataToDomainMapper()

    @Nested
    @DisplayName("GIVEN a GenreDto")
    inner class GivenAGenreDto {

        @Nested
        @DisplayName("WHEN converting it to a domain Genre")
        inner class WhenConvertingToADomainGenre {

            @Test
            @DisplayName("THEN id is mapped correctly")
            fun idMapped() {
                assertEquals(Dtos.genreDto.id, mapper.map(Dtos.genreDto).id)
            }

            @Test
            @DisplayName("THEN name is mapped correctly")
            fun nameMapped() {
                assertEquals(Dtos.genreDto.name, mapper.map(Dtos.genreDto).name)
            }
        }
    }
}

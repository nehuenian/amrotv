package nl.abnamro.amrotv.feature.movies.presentation.implementation.mapper

import nl.abnamro.amrotv.core.mvi.Mapper
import nl.abnamro.amrotv.feature.movies.domain.api.model.Genre
import nl.abnamro.amrotv.feature.movies.presentation.api.model.GenrePresentationModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GenreDomainToPresentationMapperTest {

    private lateinit var mapper: Mapper<Genre, GenrePresentationModel>

    @BeforeEach
    fun setUp() {
        mapper = GenreDomainToPresentationMapper()
    }

    @Nested
    @DisplayName("GIVEN an action genre")
    inner class GivenAnActionGenre {

        private val genre = Genre(id = 28, name = "Action")

        @Nested
        @DisplayName("WHEN mapped to presentation model")
        inner class WhenMapped {

            @Test
            @DisplayName("THEN id is preserved")
            fun idIsPreserved() {
                val result = mapper.map(genre)
                assertEquals(genre.id, result.id)
            }

            @Test
            @DisplayName("THEN name is preserved")
            fun nameIsPreserved() {
                val result = mapper.map(genre)
                assertEquals(genre.name, result.name)
            }
        }
    }

    @Nested
    @DisplayName("GIVEN a genre with an empty name")
    inner class GivenAGenreWithEmptyName {

        private val genre = Genre(id = 0, name = "")

        @Nested
        @DisplayName("WHEN mapped to presentation model")
        inner class WhenMapped {

            @Test
            @DisplayName("THEN empty name is preserved")
            fun emptyNameIsPreserved() {
                val result = mapper.map(genre)
                assertEquals("", result.name)
            }
        }
    }
}

package nl.abnamro.amrotv.feature.movies.domain.implementation.usecase

import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOption
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOrder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("FilterAndSortMoviesUseCaseImpl")
class FilterAndSortMoviesUseCaseImplTest {

    private lateinit var useCase: FilterAndSortMoviesUseCaseImpl

    @BeforeEach
    fun setUp() {
        useCase = FilterAndSortMoviesUseCaseImpl()
    }

    @Nested
    @DisplayName("GIVEN a list with action, comedy, and action-comedy movies")
    inner class GivenMixedMovieList {

        @Nested
        @DisplayName("WHEN genreId is null")
        inner class WhenGenreIdIsNull {

            @Test
            @DisplayName("THEN all movies are returned")
            fun allMoviesReturned() {
                val result =
                    useCase(
                        MovieDomainMocks.Movies.all,
                        null,
                        SortOption.POPULARITY,
                        SortOrder.DESC,
                    )
                assertEquals(3, result.size)
            }
        }

        @Nested
        @DisplayName("WHEN filtering by action genre")
        inner class WhenFilteringByActionGenre {

            @Test
            @DisplayName("THEN only movies containing the action genre ID are returned")
            fun onlyActionMoviesReturned() {
                val result =
                    useCase(
                        MovieDomainMocks.Movies.all,
                        MovieDomainMocks.Movies.ACTION_GENRE_ID,
                        SortOption.POPULARITY,
                        SortOrder.DESC,
                    )
                assertTrue(result.all { MovieDomainMocks.Movies.ACTION_GENRE_ID in it.genreIds })
                assertEquals(
                    listOf(MovieDomainMocks.Movies.actionComedy, MovieDomainMocks.Movies.action),
                    result,
                )
            }
        }

        @Nested
        @DisplayName("WHEN filtering by an unknown genre")
        inner class WhenFilteringByUnknownGenre {

            @Test
            @DisplayName("THEN an empty list is returned")
            fun emptyListReturned() {
                val result =
                    useCase(
                        MovieDomainMocks.Movies.all,
                        MovieDomainMocks.Movies.UNKNOWN_GENRE_ID,
                        SortOption.POPULARITY,
                        SortOrder.DESC,
                    )
                assertTrue(result.isEmpty())
            }
        }

        @Nested
        @DisplayName("WHEN sorting by POPULARITY DESC")
        inner class WhenSortingByPopularityDesc {

            @Test
            @DisplayName("THEN movies are ordered by popularity descending")
            fun sortedByPopularityDesc() {
                val result =
                    useCase(
                        MovieDomainMocks.Movies.all,
                        null,
                        SortOption.POPULARITY,
                        SortOrder.DESC,
                    )
                assertEquals(
                    listOf(
                        MovieDomainMocks.Movies.actionComedy,
                        MovieDomainMocks.Movies.action,
                        MovieDomainMocks.Movies.comedy,
                    ),
                    result,
                )
            }
        }

        @Nested
        @DisplayName("WHEN sorting by POPULARITY ASC")
        inner class WhenSortingByPopularityAsc {

            @Test
            @DisplayName("THEN movies are ordered by popularity ascending")
            fun sortedByPopularityAsc() {
                val result =
                    useCase(MovieDomainMocks.Movies.all, null, SortOption.POPULARITY, SortOrder.ASC)
                assertEquals(
                    listOf(
                        MovieDomainMocks.Movies.comedy,
                        MovieDomainMocks.Movies.action,
                        MovieDomainMocks.Movies.actionComedy,
                    ),
                    result,
                )
            }
        }

        @Nested
        @DisplayName("WHEN sorting by TITLE DESC")
        inner class WhenSortingByTitleDesc {

            @Test
            @DisplayName("THEN movies are ordered by title descending")
            fun sortedByTitleDesc() {
                val result =
                    useCase(MovieDomainMocks.Movies.all, null, SortOption.TITLE, SortOrder.DESC)
                assertEquals(
                    listOf(
                        MovieDomainMocks.Movies.actionComedy,
                        MovieDomainMocks.Movies.comedy,
                        MovieDomainMocks.Movies.action,
                    ),
                    result,
                )
            }
        }

        @Nested
        @DisplayName("WHEN sorting by RELEASE_DATE ASC")
        inner class WhenSortingByReleaseDateAsc {

            @Test
            @DisplayName("THEN movies are ordered by release date ascending")
            fun sortedByReleaseDateAsc() {
                val result =
                    useCase(
                        MovieDomainMocks.Movies.all,
                        null,
                        SortOption.RELEASE_DATE,
                        SortOrder.ASC,
                    )
                assertEquals(
                    listOf(
                        MovieDomainMocks.Movies.actionComedy,
                        MovieDomainMocks.Movies.comedy,
                        MovieDomainMocks.Movies.action,
                    ),
                    result,
                )
            }
        }

        @Nested
        @DisplayName("WHEN sorting by RELEASE_DATE DESC")
        inner class WhenSortingByReleaseDateDesc {

            @Test
            @DisplayName("THEN movies are ordered by release date descending")
            fun sortedByReleaseDateDesc() {
                val result =
                    useCase(
                        MovieDomainMocks.Movies.all.reversed(),
                        null,
                        SortOption.RELEASE_DATE,
                        SortOrder.DESC,
                    )
                assertEquals(
                    listOf(
                        MovieDomainMocks.Movies.action,
                        MovieDomainMocks.Movies.comedy,
                        MovieDomainMocks.Movies.actionComedy,
                    ),
                    result,
                )
            }
        }

        @Nested
        @DisplayName("WHEN filtering by comedy and sorting by POPULARITY ASC")
        inner class WhenFilterAndSortCombined {

            @Test
            @DisplayName("THEN only comedy movies are returned sorted by popularity ascending")
            fun filteredAndSorted() {
                val result =
                    useCase(
                        MovieDomainMocks.Movies.all,
                        MovieDomainMocks.Movies.COMEDY_GENRE_ID,
                        SortOption.POPULARITY,
                        SortOrder.ASC,
                    )
                assertEquals(
                    listOf(MovieDomainMocks.Movies.comedy, MovieDomainMocks.Movies.actionComedy),
                    result,
                )
            }
        }
    }

    @Nested
    @DisplayName("GIVEN an empty movie list")
    inner class GivenEmptyMovieList {

        @Test
        @DisplayName("THEN an empty list is returned regardless of filter and sort parameters")
        fun emptyListReturned() {
            val result =
                useCase(
                    emptyList(),
                    MovieDomainMocks.Movies.ACTION_GENRE_ID,
                    SortOption.POPULARITY,
                    SortOrder.DESC,
                )
            assertTrue(result.isEmpty())
        }
    }

    @Nested
    @DisplayName("GIVEN a list that includes a movie with no release date")
    inner class GivenListWithNullReleaseDateMovie {

        @Nested
        @DisplayName("WHEN sorting by RELEASE_DATE ASC")
        inner class WhenSortingByReleaseDateAsc {

            @Test
            @DisplayName("THEN the movie with no release date sorts last")
            fun nullReleaseDateSortsLast() {
                val result =
                    useCase(
                        listOf(MovieDomainMocks.Movies.noReleaseDate) + MovieDomainMocks.Movies.all,
                        null,
                        SortOption.RELEASE_DATE,
                        SortOrder.ASC,
                    )
                assertEquals(MovieDomainMocks.Movies.noReleaseDate, result.last())
            }
        }

        @Nested
        @DisplayName("WHEN sorting by RELEASE_DATE DESC")
        inner class WhenSortingByReleaseDateDesc {

            @Test
            @DisplayName("THEN the movie with no release date still sorts last")
            fun nullReleaseDateSortsLastInDesc() {
                val result =
                    useCase(
                        listOf(MovieDomainMocks.Movies.noReleaseDate) + MovieDomainMocks.Movies.all,
                        null,
                        SortOption.RELEASE_DATE,
                        SortOrder.DESC,
                    )
                assertEquals(MovieDomainMocks.Movies.noReleaseDate, result.last())
            }
        }
    }
}

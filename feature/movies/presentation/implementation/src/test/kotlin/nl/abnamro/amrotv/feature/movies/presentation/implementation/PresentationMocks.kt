package nl.abnamro.amrotv.feature.movies.presentation.implementation

import java.time.LocalDate
import kotlinx.collections.immutable.persistentListOf
import nl.abnamro.amrotv.feature.movies.domain.api.model.Genre
import nl.abnamro.amrotv.feature.movies.domain.api.model.Movie
import nl.abnamro.amrotv.feature.movies.domain.api.model.MovieDetail
import nl.abnamro.amrotv.feature.movies.presentation.api.model.GenrePresentationModel
import nl.abnamro.amrotv.feature.movies.presentation.api.model.MoviePresentationModel

object PresentationMocks {

    object Movies {
        const val ACTION_GENRE_ID = 28
        const val COMEDY_GENRE_ID = 35

        val action =
            Movie(
                id = 1,
                title = "Alpha",
                genreIds = listOf(ACTION_GENRE_ID),
                popularity = 80.0,
                releaseDate = LocalDate.of(2024, 1, 1),
                posterUrl = null,
                backdropUrl = null,
                voteAverage = 0.0,
            )
        val comedy =
            Movie(
                id = 2,
                title = "Beta",
                genreIds = listOf(COMEDY_GENRE_ID),
                popularity = 50.0,
                releaseDate = LocalDate.of(2023, 6, 15),
                posterUrl = null,
                backdropUrl = null,
                voteAverage = 0.0,
            )
        val all = listOf(action, comedy)
    }

    object Genres {
        val action = Genre(id = Movies.ACTION_GENRE_ID, name = "Action")
        val comedy = Genre(id = Movies.COMEDY_GENRE_ID, name = "Comedy")
        val all = listOf(action, comedy)
    }

    object PresentationMovies {
        val action =
            MoviePresentationModel(
                id = 1,
                title = "Alpha",
                genreIds = persistentListOf(Movies.ACTION_GENRE_ID),
                popularity = 80.0,
                posterUrl = null,
                backdropUrl = null,
                releaseDate = "Jan 1, 2024",
                formattedRating = "0.0",
                isReleased = true,
            )
        val comedy =
            MoviePresentationModel(
                id = 2,
                title = "Beta",
                genreIds = persistentListOf(Movies.COMEDY_GENRE_ID),
                popularity = 50.0,
                releaseDate = "Jun 15, 2023",
                posterUrl = null,
                backdropUrl = null,
                formattedRating = "0.0",
                isReleased = true,
            )
        val all = listOf(action, comedy)
    }

    object PresentationGenres {
        val action = GenrePresentationModel(id = Movies.ACTION_GENRE_ID, name = "Action")
        val comedy = GenrePresentationModel(id = Movies.COMEDY_GENRE_ID, name = "Comedy")
        val all = listOf(action, comedy)
    }

    object Details {
        const val DEFAULT_ID = 42
        const val IMDB_ID = "tt1234567"

        fun of(id: Int = DEFAULT_ID) =
            MovieDetail(
                id = id,
                title = "Test Movie",
                tagline = null,
                posterUrl = null,
                backdropUrl = null,
                genres = emptyList(),
                overview = "Overview",
                voteAverage = 7.5,
                voteCount = 100,
                budget = null,
                revenue = null,
                imdbId = IMDB_ID,
                status = "Released",
                runtimeInMinutes = 120,
                releaseDate = LocalDate.of(2024, 1, 1),
            )
    }
}

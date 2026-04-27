package nl.abnamro.amrotv.feature.movies.domain.implementation.usecase

import nl.abnamro.amrotv.feature.movies.domain.api.model.Genre
import nl.abnamro.amrotv.feature.movies.domain.api.model.Movie
import nl.abnamro.amrotv.feature.movies.domain.api.model.MovieDetail

object MovieDomainMocks {

    object Movies {
        const val ACTION_GENRE_ID = 28
        const val COMEDY_GENRE_ID = 35
        const val UNKNOWN_GENRE_ID = 999

        val action = Movie(
            id = 1,
            title = "Alpha",
            genreIds = listOf(ACTION_GENRE_ID),
            popularity = 80.0,
            releaseDate = "2024-01-01",
            posterUrl = null,
            backdropUrl = null,
            voteAverage = 0.0,
        )
        val comedy = Movie(
            id = 2,
            title = "Beta",
            genreIds = listOf(COMEDY_GENRE_ID),
            popularity = 50.0,
            releaseDate = "2023-06-15",
            posterUrl = null,
            backdropUrl = null,
            voteAverage = 0.0,
        )
        val actionComedy = Movie(
            id = 3,
            title = "Gamma",
            genreIds = listOf(ACTION_GENRE_ID, COMEDY_GENRE_ID),
            popularity = 95.0,
            releaseDate = "2022-03-10",
            posterUrl = null,
            backdropUrl = null,
            voteAverage = 0.0,
        )
        val all = listOf(action, comedy, actionComedy)
    }

    object Genres {
        val action = Genre(id = Movies.ACTION_GENRE_ID, name = "Action")
        val comedy = Genre(id = Movies.COMEDY_GENRE_ID, name = "Comedy")
        val all = listOf(action, comedy)
    }

    object Details {
        fun of(id: Int = 42) = MovieDetail(
            id = id,
            title = "Test Movie",
            tagline = null,
            posterUrl = null,
            backdropUrl = null,
            genres = emptyList(),
            overview = "",
            voteAverage = 0.0,
            voteCount = 0,
            budget = null,
            revenue = null,
            imdbId = null,
            status = "Released",
            runtimeInMinutes = null,
            releaseDate = "2024-01-01",
        )
    }
}

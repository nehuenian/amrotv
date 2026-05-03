package nl.abnamro.amrotv.feature.movies.data

import java.time.LocalDate
import nl.abnamro.amrotv.feature.movies.data.datasource.remote.tmdb.dto.GenreDto
import nl.abnamro.amrotv.feature.movies.data.datasource.remote.tmdb.dto.GenreListResponseDto
import nl.abnamro.amrotv.feature.movies.data.datasource.remote.tmdb.dto.MovieDetailDto
import nl.abnamro.amrotv.feature.movies.data.datasource.remote.tmdb.dto.MovieDto
import nl.abnamro.amrotv.feature.movies.domain.api.model.Genre
import nl.abnamro.amrotv.feature.movies.domain.api.model.Movie
import nl.abnamro.amrotv.feature.movies.domain.api.model.MovieDetail

internal object MovieDataMocks {

    internal object Domain {
        val genre = Genre(id = 28, name = "Action")
        val genres = listOf(genre)

        val movie =
            Movie(
                id = 1,
                title = "Test Movie",
                posterUrl = "https://image.tmdb.org/t/p/w500/poster.jpg",
                backdropUrl = "https://image.tmdb.org/t/p/w500/backdrop.jpg",
                genreIds = listOf(28, 35),
                popularity = 100.0,
                releaseDate = LocalDate.of(2024, 1, 1),
                voteAverage = 7.5,
            )
        val movies = listOf(movie)

        val detail =
            MovieDetail(
                id = 42,
                title = "Detailed Movie",
                tagline = "A great film",
                posterUrl = "https://image.tmdb.org/t/p/w500/detail_poster.jpg",
                backdropUrl = "https://image.tmdb.org/t/p/w500/detail_backdrop.jpg",
                genres = genres,
                overview = "An overview of the movie.",
                voteAverage = 8.0,
                voteCount = 1500,
                budget = 50_000_000L,
                revenue = 120_000_000L,
                imdbId = "tt1234567",
                status = "Released",
                runtimeInMinutes = 120,
                releaseDate = LocalDate.of(2024, 6, 15),
            )
    }

    internal object Dtos {
        val genreDto = GenreDto(id = 28, name = "Action")

        val movieDto =
            MovieDto(
                id = 1,
                title = "Test Movie",
                posterPath = "/poster.jpg",
                backdropPath = "/backdrop.jpg",
                genreIds = listOf(28, 35),
                popularity = 100.0,
                releaseDate = "2024-01-01",
                voteAverage = 7.5,
            )

        val movieDtoNullImages = movieDto.copy(posterPath = null, backdropPath = null)

        val movieDetailDto =
            MovieDetailDto(
                id = 42,
                title = "Detailed Movie",
                tagline = "A great film",
                posterPath = "/detail_poster.jpg",
                backdropPath = "/detail_backdrop.jpg",
                genres = listOf(genreDto),
                overview = "An overview of the movie.",
                voteAverage = 8.0,
                voteCount = 1500,
                budget = 50_000_000L,
                revenue = 120_000_000L,
                status = "Released",
                imdbId = "tt1234567",
                runtime = 120,
                releaseDate = "2024-06-15",
            )

        val movieDetailDtoNullTagline = movieDetailDto.copy(tagline = null)

        val movieDetailDtoBudgetZero = movieDetailDto.copy(budget = 0L)
        val movieDetailDtoRevenueZero = movieDetailDto.copy(revenue = 0L)
        val movieDetailDtoNullImdbId = movieDetailDto.copy(imdbId = null)

        val genreListResponseDto = GenreListResponseDto(genres = listOf(genreDto))
    }
}

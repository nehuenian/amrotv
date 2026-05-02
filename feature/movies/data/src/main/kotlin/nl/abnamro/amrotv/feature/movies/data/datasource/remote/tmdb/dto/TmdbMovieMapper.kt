package nl.abnamro.amrotv.feature.movies.data.datasource.remote.tmdb.dto

import nl.abnamro.amrotv.feature.movies.domain.api.model.Genre
import nl.abnamro.amrotv.feature.movies.domain.api.model.Movie
import nl.abnamro.amrotv.feature.movies.domain.api.model.MovieDetail

private const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"

internal fun MovieDto.toDomain(): Movie =
    Movie(
        id = id,
        title = title,
        posterUrl = posterPath?.let { "$IMAGE_BASE_URL$it" },
        backdropUrl = backdropPath?.let { "$IMAGE_BASE_URL$it" },
        genreIds = genreIds,
        popularity = popularity,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
    )

internal fun MovieDetailDto.toDomain(): MovieDetail =
    MovieDetail(
        id = id,
        title = title,
        tagline = tagline,
        posterUrl = posterPath?.let { "$IMAGE_BASE_URL$it" },
        backdropUrl = backdropPath?.let { "$IMAGE_BASE_URL$it" },
        genres = genres.map { it.toDomain() },
        overview = overview,
        voteAverage = voteAverage,
        voteCount = voteCount,
        budget = budget.takeIf { it > 0 },
        revenue = revenue.takeIf { it > 0 },
        imdbId = imdbId,
        status = status,
        runtimeInMinutes = runtime,
        releaseDate = releaseDate,
    )

internal fun GenreDto.toDomain(): Genre = Genre(id = id, name = name)

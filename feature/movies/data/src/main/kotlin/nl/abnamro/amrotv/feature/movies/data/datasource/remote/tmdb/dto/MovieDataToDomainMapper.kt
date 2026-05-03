package nl.abnamro.amrotv.feature.movies.data.datasource.remote.tmdb.dto

import javax.inject.Inject
import nl.abnamro.amrotv.core.mvi.Mapper
import nl.abnamro.amrotv.feature.movies.domain.api.model.Movie

internal class MovieDataToDomainMapper @Inject constructor() : Mapper<MovieDto, Movie> {

    override fun map(input: MovieDto): Movie =
        Movie(
            id = input.id,
            title = input.title,
            posterUrl = input.posterPath?.let { "$IMAGE_BASE_URL$it" },
            backdropUrl = input.backdropPath?.let { "$IMAGE_BASE_URL$it" },
            genreIds = input.genreIds,
            popularity = input.popularity,
            releaseDate = input.releaseDate,
            voteAverage = input.voteAverage,
        )
}

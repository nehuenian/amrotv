package nl.abnamro.amrotv.feature.movies.data.datasource.remote.tmdb.mapper

import javax.inject.Inject
import nl.abnamro.amrotv.core.mvi.Mapper
import nl.abnamro.amrotv.feature.movies.data.datasource.remote.tmdb.dto.MovieDetailDto
import nl.abnamro.amrotv.feature.movies.domain.api.model.MovieDetail

internal class MovieDetailDataToDomainMapper
@Inject
constructor(private val genreMapper: GenreDataToDomainMapper) :
    Mapper<MovieDetailDto, MovieDetail> {

    override fun map(input: MovieDetailDto): MovieDetail =
        MovieDetail(
            id = input.id,
            title = input.title,
            tagline = input.tagline,
            posterUrl = input.posterPath?.let { "${IMAGE_BASE_URL}$it" },
            backdropUrl = input.backdropPath?.let { "${IMAGE_BASE_URL}$it" },
            genres = input.genres.map { genreMapper.map(it) },
            overview = input.overview,
            voteAverage = input.voteAverage,
            voteCount = input.voteCount,
            budget = input.budget.takeIf { it > 0 },
            revenue = input.revenue.takeIf { it > 0 },
            imdbId = input.imdbId,
            status = input.status,
            runtimeInMinutes = input.runtime,
            releaseDate = parseTmdbDate(input.releaseDate),
        )
}

package nl.abnamro.amrotv.feature.movies.presentation.implementation.mapper

import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import nl.abnamro.amrotv.core.mvi.Mapper
import nl.abnamro.amrotv.feature.movies.domain.api.model.Movie
import nl.abnamro.amrotv.feature.movies.presentation.api.model.MoviePresentationModel

class MovieDomainToPresentationMapper @Inject constructor() :
    Mapper<Movie, MoviePresentationModel> {

    override fun map(input: Movie) =
        MoviePresentationModel(
            id = input.id,
            title = input.title,
            posterUrl = input.posterUrl,
            backdropUrl = input.backdropUrl,
            genreIds = input.genreIds.toImmutableList(),
            popularity = input.popularity,
            releaseYear = extractReleaseYear(input.releaseDate),
            formattedRating = formatRating(input.voteAverage),
        )
}

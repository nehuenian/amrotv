package nl.abnamro.amrotv.feature.movies.presentation.implementation.mapper

import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import nl.abnamro.amrotv.core.mvi.Mapper
import nl.abnamro.amrotv.feature.movies.domain.api.model.MovieDetail
import nl.abnamro.amrotv.feature.movies.presentation.api.model.MovieDetailPresentationModel

class MovieDetailDomainToPresentationMapper
@Inject
constructor(private val genreMapper: GenreDomainToPresentationMapper) :
    Mapper<MovieDetail, MovieDetailPresentationModel> {

    override fun map(input: MovieDetail) =
        MovieDetailPresentationModel(
            id = input.id,
            title = input.title,
            tagline = input.tagline,
            posterUrl = input.posterUrl,
            backdropUrl = input.backdropUrl,
            genres = input.genres.map { genreMapper.map(it) }.toImmutableList(),
            overview = input.overview,
            formattedRating = formatRating(input.voteAverage),
            voteCount = input.voteCount,
            formattedBudget = formatCurrency(input.budget),
            formattedRevenue = formatCurrency(input.revenue),
            imdbId = input.imdbId,
            status = input.status,
            runtimeInMinutes = input.runtimeInMinutes,
            releaseYear = extractReleaseYear(input.releaseDate),
        )

    // TODO: Replace with a locale-aware currency formatter from an i18n library
    private fun formatCurrency(value: Long?): String? =
        value?.takeIf { it > 0L }?.let { NumberFormat.getCurrencyInstance(Locale.US).format(it) }
}

package nl.abnamro.amrotv.feature.movies.presentation.api.model

import kotlinx.collections.immutable.ImmutableList

data class MovieDetailPresentationModel(
    val id: Int,
    val title: String,
    val tagline: String?,
    val posterUrl: String?,
    val backdropUrl: String?,
    val genres: ImmutableList<GenrePresentationModel>,
    val overview: String,
    val formattedRating: String,
    val voteCount: Int,
    val formattedBudget: String?,
    val formattedRevenue: String?,
    val imdbId: String?,
    val status: String,
    val runtimeInMinutes: Int?,
    val releaseDate: String?,
)

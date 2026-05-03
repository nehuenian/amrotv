package nl.abnamro.amrotv.feature.movies.presentation.api.model

import kotlinx.collections.immutable.ImmutableList

data class MoviePresentationModel(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val genreIds: ImmutableList<Int>,
    val popularity: Double,
    val releaseDate: String?,
    val formattedRating: String,
    val isReleased: Boolean,
)

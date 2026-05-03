package nl.abnamro.amrotv.feature.movies.domain.api.model

import java.time.LocalDate

data class Movie(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val genreIds: List<Int>,
    val popularity: Double,
    val releaseDate: LocalDate?,
    val voteAverage: Double,
)

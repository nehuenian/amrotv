package nl.abnamro.amrotv.feature.movies.domain.api.model

data class Movie(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val genreIds: List<Int>,
    val popularity: Double,
    val releaseDate: String,
    val voteAverage: Double,
)

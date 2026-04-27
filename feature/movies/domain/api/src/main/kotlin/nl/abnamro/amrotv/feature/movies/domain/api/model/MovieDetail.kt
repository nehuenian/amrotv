package nl.abnamro.amrotv.feature.movies.domain.api.model

data class MovieDetail(
    val id: Int,
    val title: String,
    val tagline: String?,
    val posterUrl: String?,
    val backdropUrl: String?,
    val genres: List<Genre>,
    val overview: String,
    val voteAverage: Double,
    val voteCount: Int,
    val budget: Long?,
    val revenue: Long?,
    val imdbId: String?,
    val status: String,
    val runtimeInMinutes: Int?,
    val releaseDate: String,
)

@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package nl.abnamro.amrotv.feature.movies.data.datasource.remote.tmdb.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MovieDetailDto(
    @SerialName("id") val id: Int = 0,
    @SerialName("title") val title: String = "",
    @SerialName("tagline") val tagline: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("genres") val genres: List<GenreDto> = emptyList(),
    @SerialName("overview") val overview: String = "",
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("vote_count") val voteCount: Int = 0,
    @SerialName("budget") val budget: Long = 0L,
    @SerialName("revenue") val revenue: Long = 0L,
    @SerialName("status") val status: String = "",
    @SerialName("imdb_id") val imdbId: String? = null,
    @SerialName("runtime") val runtime: Int? = null,
    @SerialName("release_date") val releaseDate: String = "",
)

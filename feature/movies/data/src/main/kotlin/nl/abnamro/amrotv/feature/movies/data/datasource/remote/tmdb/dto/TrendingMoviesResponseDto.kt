@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package nl.abnamro.amrotv.feature.movies.data.datasource.remote.tmdb.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class TrendingMoviesResponseDto(
    @SerialName("results") val results: List<MovieDto> = emptyList(),
    @SerialName("page") val page: Int = 1,
    @SerialName("total_pages") val totalPages: Int = 1,
)

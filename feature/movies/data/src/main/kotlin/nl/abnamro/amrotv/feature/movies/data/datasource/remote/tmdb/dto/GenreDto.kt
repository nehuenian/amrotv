@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package nl.abnamro.amrotv.feature.movies.data.datasource.remote.tmdb.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GenreDto(
    @SerialName("id") val id: Int = 0,
    @SerialName("name") val name: String = "",
)

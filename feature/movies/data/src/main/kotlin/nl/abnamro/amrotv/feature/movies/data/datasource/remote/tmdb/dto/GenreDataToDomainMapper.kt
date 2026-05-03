package nl.abnamro.amrotv.feature.movies.data.datasource.remote.tmdb.dto

import javax.inject.Inject
import nl.abnamro.amrotv.core.mvi.Mapper
import nl.abnamro.amrotv.feature.movies.domain.api.model.Genre

internal class GenreDataToDomainMapper @Inject constructor() : Mapper<GenreDto, Genre> {

    override fun map(input: GenreDto): Genre = Genre(id = input.id, name = input.name)
}
